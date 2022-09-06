package com.strikeprotocols.mobile.presentation.common_approvals

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.common.StrikeCountDownTimerImpl
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import kotlinx.coroutines.launch

abstract class CommonApprovalsViewModel(
    private val approvalsRepository: ApprovalsRepository,
    private val keyRepository: KeyRepository,
    private val timer: StrikeCountDownTimer
) : ViewModel() {

    //region abstract methods
    abstract fun setShouldDisplayConfirmDispositionDialog(
        approval: WalletApproval? = null,
        isInitiationRequest: Boolean = false,
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    )

    open fun handleInitialData(approval: WalletApproval) {}
    open fun handleEmptyInitialData() {}

    open fun handleScreenBackgrounded() {}
    open fun handleScreenForegrounded() {}
    //endregion

    var state by mutableStateOf(ApprovalsState())
        protected set

    fun onStart(approval: WalletApproval? = null) {
        if (state.selectedApproval == null) {
            approval?.let { handleInitialData(it) } ?: handleEmptyInitialData()
        }

        timer.startCountDownTimer(StrikeCountDownTimerImpl.Companion.UPDATE_COUNTDOWN) {
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
            val cipher = keyRepository.getCipherForDecryption()
            if (cipher != null) {
                state = state.copy(bioPromptTrigger = Resource.Success(cipher))
            }
        }
    }

    fun biometryApproved(cryptoObject: BiometricPrompt.CryptoObject) {
        registerApprovalDisposition(cryptoObject)
    }

    private fun registerApprovalDisposition(cryptoObject: BiometricPrompt.CryptoObject) {
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
                cryptoObject = cryptoObject
            )

            state = state.copy(approvalDispositionState = approvalDispositionState)
        }
    }

    private suspend fun retrieveApprovalDispositionFromAPI(
        approvalDispositionState: ApprovalDispositionState?,
        approval: WalletApproval?,
        multipleAccounts: DurableNonceViewModel.MultipleAccounts?,
        approvalsRepository: ApprovalsRepository,
        cryptoObject: BiometricPrompt.CryptoObject
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
        val solanaApprovalRequestType = approval?.getSolanaApprovalRequestType()
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
                cryptoObject = cryptoObject
            )
            return approvalDispositionState.copy(
                initiationDispositionResult = initiationResponseResource
            )
        } else {
            val registerApprovalDisposition = RegisterApprovalDisposition(
                approvalDisposition = approvalDisposition,
                solanaApprovalRequestType = solanaApprovalRequestType,
                nonces = nonces,
            )

            val approvalDispositionResponseResource =
                approvalsRepository.approveOrDenyDisposition(
                    requestId = approvalId,
                    registerApprovalDisposition = registerApprovalDisposition,
                    cryptoObject = cryptoObject
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