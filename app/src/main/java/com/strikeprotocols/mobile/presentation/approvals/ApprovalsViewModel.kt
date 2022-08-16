package com.strikeprotocols.mobile.presentation.approvals

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.PushRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel.Companion.UPDATE_COUNTDOWN
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository
) : ViewModel() {

    private var timer: CountDownTimer? = null

    var state by mutableStateOf(ApprovalsState())
        private set

    fun refreshData() {
        retrieveWalletApprovals()
    }

    private fun resetApprovalsData() {
        state = state.copy(
            approvals = emptyList(),
            walletApprovalsResult = Resource.Uninitialized,
            selectedApproval = null,
            multipleAccounts = null
        )
    }

    fun onStart() {
        startCountDown()
    }

    fun onStop() {
        timer?.cancel()
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_COUNTDOWN) {
            override fun onTick(millisecs: Long) {
                updateShouldRefreshTimers()
            }
            override fun onFinish() {}
        }
        timer?.start()
    }

    fun updateShouldRefreshTimers() {
        state = state.copy(shouldRefreshTimers = !state.shouldRefreshTimers)
    }

    fun setShouldDisplayConfirmDispositionDialog(
        approval: WalletApproval?,
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
            selectedApproval = approval,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(
                    approvalDisposition
                )
            )
        )
    }

    fun resetShouldDisplayConfirmDisposition() {
        state = state.copy(shouldDisplayConfirmDisposition = null)
    }

    fun resetShouldShowErrorSnackbar() {
        state = state.copy(shouldShowErrorSnackbar = false)
    }

    fun setMultipleAccounts(multipleAccounts: DurableNonceViewModel.MultipleAccounts?) {
        state = state.copy(multipleAccounts = multipleAccounts)
        registerApprovalDisposition()
    }

    fun resetMultipleAccounts() {
        state = state.copy(multipleAccounts = null)
    }

    fun wipeDataAfterDispositionSuccess() {
        resetApprovalsData()
        resetApprovalDispositionState()

        refreshData()
    }

    fun dismissApprovalDispositionError() {
        resetApprovalDispositionState()
    }


    private fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun resetWalletApprovalsResult() {
        state = state.copy(walletApprovalsResult = Resource.Uninitialized)
    }


    //region API Calls
    private fun retrieveWalletApprovals() {
        viewModelScope.launch {
            val cachedApprovals = state.approvals.toList()

            state = state.copy(walletApprovalsResult = Resource.Loading())
            delay(250)

            val walletApprovalsResource = approvalsRepository.getWalletApprovals()

            val approvals =
                when (walletApprovalsResource) {
                    is Resource.Success -> {
                        walletApprovalsResource.data ?: emptyList()
                    }
                    is Resource.Error -> {
                        cachedApprovals
                    }
                    else -> {
                        emptyList()
                    }
                }

            state = state.copy(
                walletApprovalsResult = walletApprovalsResource,
                approvals = approvals
            )
        }
    }

    private fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )

            val isInitiationRequest =
                state.selectedApproval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
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

            val solanaApprovalRequestType =
                state.selectedApproval?.getSolanaApprovalRequestType()
            val approvalId = state.selectedApproval?.id ?: ""
            if (solanaApprovalRequestType == null) {
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
                    state.selectedApproval?.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                val initiationDisposition = InitiationDisposition(
                    approvalDisposition = approvalDisposition,
                    nonces = nonces,
                    multiSigOpInitiationDetails = multiSignOpDetails
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

                val approvalDispositionResource =
                    approvalsRepository.approveOrDenyDisposition(
                        requestId = approvalId,
                        registerApprovalDisposition = registerApprovalDisposition
                    )
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        registerApprovalDispositionResult = approvalDispositionResource
                    )
                )
            }
        }
    }
    //endregion

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L

        const val KEY_SHOULD_REFRESH_DATA = "KEY_SHOULD_REFRESH_DATA"
    }

}