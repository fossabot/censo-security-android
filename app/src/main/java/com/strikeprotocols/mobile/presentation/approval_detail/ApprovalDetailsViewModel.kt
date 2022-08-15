package com.strikeprotocols.mobile.presentation.approval_detail

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.calculateSecondsLeftUntilCountdownIsOver
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel.Companion.UPDATE_COUNTDOWN
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalRetryData
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalDetailsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository
) : ViewModel() {

    private var timer: CountDownTimer? = null

    var state by mutableStateOf(ApprovalDetailsState())
        private set

    fun onStart(approval: WalletApproval?) {
        if (state.approval == null) {
            approval?.let { setArgsToState(it) } ?: setShouldKickOutUser()
        }

        startCountDown()
    }

    fun onStop() {
        timer?.cancel()
    }

    private fun setArgsToState(approval: WalletApproval) {
        state = state.copy(approval = approval)
    }

    fun setShouldDisplayConfirmDispositionDialog(
        isApproving: Boolean,
        isInitiationRequest: Boolean,
        dialogTitle: String,
        dialogText: String
    ) {
        val dialogDetails = ConfirmDispositionDialogDetails(
            shouldDisplay = true,
            isApproving = isApproving,
            dialogTitle = dialogTitle,
            dialogText = dialogText
        )

        val approvalDisposition =
            if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY

        state = state.copy(
            shouldDisplayConfirmDisposition = dialogDetails,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(approvalDisposition),
                approvalRetryData = ApprovalRetryData(
                    isApproving = isApproving,
                    isInitiationRequest = isInitiationRequest
                )
            )
        )
    }

    fun resetShouldDisplayConfirmDisposition() {
        state = state.copy(shouldDisplayConfirmDisposition = null)
    }

    fun setDetailsScreenWasBackgrounded() {
        state = state.copy(screenWasBackgrounded = true)
    }

    fun resetDetailsScreenWasBackgrounded() {
        state = state.copy(screenWasBackgrounded = false)
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_COUNTDOWN) {
            override fun onTick(millisecs: Long) {
                updateRemainingTimeInSeconds()
            }

            override fun onFinish() {}
        }
        timer?.start()
    }

    fun updateRemainingTimeInSeconds() {
        state = state.copy(
            remainingTimeInSeconds = calculateSecondsLeftUntilCountdownIsOver(
                submitDate = state.submitDate,
                totalTimeInSeconds = state.approvalTimeoutInSeconds
            )
        )
    }

    fun setShouldKickOutUser() {
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }

    fun resetShouldKickOutUser() {
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = false
        )
    }

    fun wipeDataAndKickUserOutToApprovalsScreen() {
        resetApprovalDispositionState()
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }

    fun dismissApprovalDispositionError() {
        resetApprovalDispositionState()
    }

    private fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun setMultipleAccounts(multipleAccounts: DurableNonceViewModel.MultipleAccounts?) {
        state = state.copy(multipleAccounts = multipleAccounts)
        registerApprovalDisposition()
    }

    fun resetMultipleAccounts() {
        state = state.copy(multipleAccounts = null)
    }

    //region API calls
    private fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )

            val isInitiationRequest =
                state.approval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

            //Data retrieval and checks
            val nonces = state.multipleAccounts?.nonces
            if (nonces == null) {
                state = if (isInitiationRequest) {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            initiationDispositionResult = Resource.Error()
                        )
                    )
                } else {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            registerApprovalDispositionResult = Resource.Error()
                        )
                    )
                }
                return@launch
            }

            val approvalId = state.approval?.id ?: ""
            val solanaApprovalRequestType = state.approval?.getSolanaApprovalRequestType()
            if (solanaApprovalRequestType == null || state.approval?.id == null) {
                state = if (isInitiationRequest) {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            initiationDispositionResult = Resource.Error()
                        )
                    )
                } else {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            registerApprovalDispositionResult = Resource.Error()
                        )
                    )
                }
                return@launch
            }

            val recentApprovalDisposition = state.approvalDispositionState?.approvalDisposition
            val approvalDisposition =
                if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
            if (approvalDisposition == null) {
                state = if (isInitiationRequest) {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            initiationDispositionResult = Resource.Error()
                        )
                    )
                } else {
                    state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            registerApprovalDispositionResult = Resource.Error()
                        )
                    )
                }
                return@launch
            }

            if (isInitiationRequest) {
                val multiSignOpDetails =
                    state.approval?.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                val initiationDisposition = InitiationDisposition(
                    approvalDisposition = approvalDisposition,
                    nonces = nonces,
                    multiSigOpInitiationDetails = multiSignOpDetails,
                )

                val initiationResponseResource = approvalsRepository.approveOrDenyInitiation(
                    requestId = approvalId,
                    initialDisposition = initiationDisposition
                )

                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        initiationDispositionResult = initiationResponseResource
                    )
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
                        registerApprovalDisposition = registerApprovalDisposition
                    )

                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        registerApprovalDispositionResult = approvalDispositionResponseResource
                    )
                )
            }
        }
    }
    //endregion

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}

