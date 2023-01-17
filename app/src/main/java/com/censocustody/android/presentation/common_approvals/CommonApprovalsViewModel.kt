package com.censocustody.android.presentation.common_approvals

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.CensoCountDownTimerImpl
import com.censocustody.android.data.ApprovalsRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.InitiationDisposition
import com.censocustody.android.data.models.RegisterApprovalDisposition
import com.censocustody.android.data.models.approval.SolanaApprovalRequestDetails
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.UnknownApprovalType.isDeviceKeyApprovalType
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel
import kotlinx.coroutines.launch

abstract class  CommonApprovalsViewModel(
    private val approvalsRepository: ApprovalsRepository,
    private val userRepository: UserRepository,
    private val cipherRepository: CipherRepository,
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
            val isInitiationRequest =
                state.selectedApproval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
            val selectedApproval = state.selectedApproval

            if (selectedApproval == null) {
                state = state.copy(
                    approvalDispositionState =
                    errorRegisteringResult(state.approvalDispositionState, isInitiationRequest)
                )
                return@launch
            }

            if (selectedApproval.getApprovalRequestType().isDeviceKeyApprovalType()) {
                val email = userRepository.retrieveUserEmail()
                val deviceId = userRepository.retrieveUserDeviceId(email)
                val signature = cipherRepository.getSignatureForDeviceSigning(deviceId)
                if (signature != null) {
                    state =
                        state.copy(bioPromptTrigger = Resource.Success(CryptoObject(signature)))
                }
            } else {
                val cipher = cipherRepository.getCipherForV3RootSeedDecryption()
                if (cipher != null) {
                    state =
                        state.copy(bioPromptTrigger = Resource.Success(CryptoObject(cipher)))
                }
            }
        }
    }

    fun biometryApproved(cryptoObject: CryptoObject) {
        val isInitiationRequest =
            state.selectedApproval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
        val selectedApproval = state.selectedApproval

        if (selectedApproval == null) {
            state = state.copy(
                approvalDispositionState =
                errorRegisteringResult(state.approvalDispositionState, isInitiationRequest)
            )
            return
        }

        val isDeviceKeyType = selectedApproval.getApprovalRequestType().isDeviceKeyApprovalType()

        if (isDeviceKeyType && cryptoObject.signature != null) {
            registerApprovalDisposition(cryptoObject)
        } else if (!isDeviceKeyType && cryptoObject.cipher != null) {
            registerApprovalDisposition(cryptoObject)
        }
    }

    private fun registerApprovalDisposition(cryptoObject: CryptoObject) {
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
                cryptoObject = cryptoObject,
            )

            state = state.copy(approvalDispositionState = approvalDispositionState)
        }
    }

    private suspend fun retrieveApprovalDispositionFromAPI(
        approvalDispositionState: ApprovalDispositionState?,
        approval: ApprovalRequest?,
        multipleAccounts: DurableNonceViewModel.MultipleAccounts?,
        approvalsRepository: ApprovalsRepository,
        cryptoObject: CryptoObject
    ): ApprovalDispositionState? {

        val isInitiationRequest =
            approval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Data retrieval and checks
        val nonces = multipleAccounts?.nonces
            ?: return errorRegisteringResult(approvalDispositionState, isInitiationRequest)

        val approvalId = approval?.id ?: ""
        val solanaApprovalRequestType = approval?.getApprovalRequestType()
        if (solanaApprovalRequestType == null || approval.id == null) {
            return errorRegisteringResult(approvalDispositionState, isInitiationRequest)
        }

        val recentApprovalDisposition = approvalDispositionState?.approvalDisposition
        val approvalDisposition =
            if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
                ?: return errorRegisteringResult(approvalDispositionState, isInitiationRequest)

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
                approvalRequestType = solanaApprovalRequestType,
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

    private fun errorRegisteringResult(
        approvalDispositionState: ApprovalDispositionState?,
        isInitiationRequest: Boolean
    ): ApprovalDispositionState? {
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