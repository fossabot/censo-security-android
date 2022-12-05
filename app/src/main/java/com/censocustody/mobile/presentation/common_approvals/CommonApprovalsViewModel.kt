package com.censocustody.mobile.presentation.common_approvals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.CensoCountDownTimer
import com.censocustody.mobile.common.CensoCountDownTimerImpl
import com.censocustody.mobile.data.ApprovalsRepository
import com.censocustody.mobile.data.KeyRepository
import com.censocustody.mobile.data.models.ApprovalDisposition
import com.censocustody.mobile.data.models.CipherRepository
import com.censocustody.mobile.data.models.InitiationDisposition
import com.censocustody.mobile.data.models.RegisterApprovalDisposition
import com.censocustody.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.censocustody.mobile.data.models.approval.ApprovalRequest
import com.censocustody.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.censocustody.mobile.presentation.durable_nonce.DurableNonceViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher

abstract class CommonApprovalsViewModel(
    private val approvalsRepository: ApprovalsRepository,
    private val cipherRepository: CipherRepository,
    private val keyRepository: KeyRepository,
    private val timer: CensoCountDownTimer
) : ViewModel() {

    //region abstract methods
    abstract fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequest? = null,
        isInitiationRequest: Boolean = false,
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    )

    open fun handleInitialData(approval: ApprovalRequest) {}
    open fun handleEmptyInitialData() {}

    open fun handleScreenBackgrounded() {}
    open fun handleScreenForegrounded() {}
    //endregion

    var state by mutableStateOf(ApprovalsState())
        protected set

    fun onStart(approval: ApprovalRequest? = null) {
        if (state.selectedApproval == null) {
            approval?.let { handleInitialData(it) } ?: handleEmptyInitialData()
        }

        timer.startCountDownTimer(CensoCountDownTimerImpl.Companion.UPDATE_COUNTDOWN) {
            updateTimers()
        }
    }

    fun onStop() {
        timer.stopCountDownTimer()
    }

    private fun updateTimers() {
        state = state.copy(shouldRefreshTimers = !state.shouldRefreshTimers)
    }

    fun setMultipleAccounts(multipleAccounts: DurableNonceViewModel.MultipleAccounts?) {
        state = state.copy(multipleAccounts = multipleAccounts)
        triggerBioPrompt()
    }

    private fun triggerBioPrompt() {
        viewModelScope.launch {
            val cipher = cipherRepository.getCipherForV3RootSeedDecryption()
            if (cipher != null) {
                state = state.copy(bioPromptTrigger = Resource.Success(cipher))
            }
        }
    }

    fun biometryApproved(cipher: Cipher) {
        registerApprovalDisposition(cipher)
    }

    private fun registerApprovalDisposition(cipher: Cipher) {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )

            val approvalDispositionState = retrieveApprovalDispositionFromAPI(
                approvalDispositionState = state.approvalDispositionState,
                approval = state.selectedApproval,
                multipleAccounts = state.multipleAccounts,
                approvalsRepository = approvalsRepository,
                cipher = cipher
            )

            state = state.copy(approvalDispositionState = approvalDispositionState)
        }
    }

    private suspend fun retrieveApprovalDispositionFromAPI(
        approvalDispositionState: ApprovalDispositionState?,
        approval: ApprovalRequest?,
        multipleAccounts: DurableNonceViewModel.MultipleAccounts?,
        approvalsRepository: ApprovalsRepository,
        cipher: Cipher
    ): ApprovalDispositionState? {

        val isInitiationRequest =
            approval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Data retrieval and checks
        val nonces = multipleAccounts?.nonces
            ?: return if (isInitiationRequest) {
                approvalDispositionState?.copy(
                    initiationDispositionResult = Resource.Error()
                )
            } else {
                approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Error()
                )
            }

        val approvalId = approval?.id ?: ""
        val solanaApprovalRequestType = approval?.getApprovalRequestType()
        if (solanaApprovalRequestType == null || approval.id == null) {
            return if (isInitiationRequest) {
                approvalDispositionState?.copy(
                    initiationDispositionResult = Resource.Error()
                )
            } else {
                approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Error()
                )
            }
        }

        val recentApprovalDisposition = approvalDispositionState?.approvalDisposition
        val approvalDisposition =
            if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
                ?: return if (isInitiationRequest) {
                    approvalDispositionState?.copy(
                        initiationDispositionResult = Resource.Error()
                    )
                } else {
                    approvalDispositionState?.copy(
                        registerApprovalDispositionResult = Resource.Error()
                    )
                }

        if (isInitiationRequest) {
            val multiSignOpDetails =
                approval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
            val initiationDisposition = InitiationDisposition(
                approvalDisposition = approvalDisposition,
                nonces = nonces,
                multiSigOpInitiationDetails = multiSignOpDetails,
            )

            val initiationResponseResource = approvalsRepository.approveOrDenyInitiation(
                requestId = approvalId,
                initialDisposition = initiationDisposition,
                cipher = cipher
            )
            return approvalDispositionState.copy(
                initiationDispositionResult = initiationResponseResource
            )
        } else {
            val registerApprovalDisposition = RegisterApprovalDisposition(
                approvalDisposition = approvalDisposition,
                approvalRequestType = solanaApprovalRequestType,
                nonces = nonces,
            )

            val approvalDispositionResponseResource =
                approvalsRepository.approveOrDenyDisposition(
                    requestId = approvalId,
                    registerApprovalDisposition = registerApprovalDisposition,
                    cipher = cipher
                )

            return approvalDispositionState.copy(
                registerApprovalDispositionResult = approvalDispositionResponseResource
            )
        }
    }

    fun dismissApprovalDispositionError() {
        resetApprovalDispositionState()
    }

    fun getDialogDetailsAndApprovalDispositionType(
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    ) : DialogDetailsAndDispositionType {
        val dialogDetails = getDialogDetails(
            isApproving = isApproving, dialogMessages = dialogMessages
        )

        val approvalDisposition = getApprovalDispositionType(isApproving)

        return DialogDetailsAndDispositionType(dialogDetails, approvalDisposition)
    }

    private fun getDialogDetails(isApproving: Boolean, dialogMessages: Pair<String, String>) =
        ConfirmDispositionDialogDetails(
            shouldDisplay = true,
            isApproving = isApproving,
            dialogMessages = dialogMessages
        )

    private fun getApprovalDispositionType(isApproving: Boolean) =
        if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY

    //region reset data
    fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun resetPromptTrigger() {
        state = state.copy(bioPromptTrigger = Resource.Uninitialized)
    }

    fun resetMultipleAccounts() {
        state = state.copy(multipleAccounts = null)
    }

    fun resetShouldDisplayConfirmDisposition() {
        state = state.copy(shouldDisplayConfirmDisposition = null)
    }
    //endregion
}

data class DialogDetailsAndDispositionType(
    val dialogDetails: ConfirmDispositionDialogDetails,
    val approvalDisposition: ApprovalDisposition
)