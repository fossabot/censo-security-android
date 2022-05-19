package com.strikeprotocols.mobile.presentation.approval_detail

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.calculateSecondsLeftUntilCountdownIsOver
import com.strikeprotocols.mobile.common.formatISO8601IntoSeconds
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel.Companion.UPDATE_COUNTDOWN
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
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
            approval?.let { setArgsToState(it) }
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
            if(isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY

        state = state.copy(
            shouldDisplayConfirmDisposition = dialogDetails,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(approvalDisposition)
            )
        )
    }

    fun resetShouldDisplayConfirmDisposition() {
        state = state.copy(shouldDisplayConfirmDisposition = null)
    }

    fun setShouldDisplayApprovalDispositionError() {
        state = state.copy(shouldDisplayApprovalDispositionError = true)
    }

    private fun resetShouldDisplayApprovalDispositionError() {
        state = state.copy(shouldDisplayApprovalDispositionError = false)
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
                totalTimeInSeconds = state.approvalTimeoutInSeconds ?: 0
            )
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
        resetShouldDisplayApprovalDispositionError()
        resetApprovalDispositionState()
    }

    fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun setMultipleAccounts(multipleAccounts: DurableNonceViewModel.MultipleAccounts) {
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
            //Data retrieval and checks
            val nonces = state.multipleAccounts?.nonces
            if (nonces == null) {
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        approvalDispositionError = ApprovalDispositionError.DURABLE_NONCE_FAILURE,
                        registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.DURABLE_NONCE_FAILURE.error)
                    )
                )
                return@launch
            }

            val approvalId = state.approval?.id ?: ""
            val solanaApprovalRequestType = state.approval?.getSolanaApprovalRequestType()
            if (solanaApprovalRequestType == null || state.approval?.id == null) {
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        approvalDispositionError = ApprovalDispositionError.SIGNING_DATA_FAILURE,
                        registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
                    )
                )
                return@launch
            }

            val recentApprovalDisposition = state.approvalDispositionState?.approvalDisposition
            val approvalDisposition =
                if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
            if (approvalDisposition == null) {
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        approvalDispositionError = ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE,
                        registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE.error)
                    )
                )
                return@launch
            }

            if (state.approval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                try {
                    val multiSignOpDetails =
                        state.approval?.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                    val initiationDisposition = InitiationDisposition(
                        approvalDisposition = approvalDisposition,
                        nonces = nonces,
                        multiSigOpInitiationDetails = multiSignOpDetails,
                    )

                    val initiationResponse = approvalsRepository.approveOrDenyInitiation(
                        requestId = approvalId,
                        initialDisposition = initiationDisposition
                    )

                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            initiationDispositionResult = Resource.Success(initiationResponse)
                        )
                    )
                } catch (e: Exception) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.SUBMIT_FAILURE,
                            initiationDispositionResult = Resource.Error(e.message ?: "")
                        )
                    )
                }
            } else {
                try {
                    val registerApprovalDisposition = RegisterApprovalDisposition(
                        approvalDisposition = approvalDisposition,
                        solanaApprovalRequestType = solanaApprovalRequestType,
                        nonces = nonces,
                    )

                    val approvalDispositionResponse =
                        approvalsRepository.approveOrDenyDisposition(
                            requestId = approvalId,
                            registerApprovalDisposition = registerApprovalDisposition
                        )
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            registerApprovalDispositionResult = Resource.Success(
                                approvalDispositionResponse
                            )
                        )
                    )
                } catch (e: Exception) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.SUBMIT_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(e.message ?: "")
                        )
                    )
                }
            }
        }
    }
    //endregion

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}

