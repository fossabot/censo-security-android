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
import com.strikeprotocols.mobile.data.StrikeUserData
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
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
    private val approvalsRepository: ApprovalsRepository,
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val strikeUserData: StrikeUserData,
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
        setUserInfo()
    }

    fun onStop() {
        timer?.cancel()
    }

    private fun setUserInfo() {
        state = state.copy(
            email = strikeUserData.getEmail(),
            name = strikeUserData.getStrikeUser()?.fullName ?: ""
        )
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

    fun setShouldDisplayApprovalDispositionError() {
        state = state.copy(shouldDisplayApprovalDispositionError = true)
    }

    private fun resetShouldDisplayApprovalDispositionError() {
        state = state.copy(shouldDisplayApprovalDispositionError = false)
    }

    fun resetShouldShowErrorSnackbar() {
        state = state.copy(shouldShowErrorSnackbar = false)
    }

    fun logout() {
        viewModelScope.launch {
            state = state.copy(logoutResult = Resource.Loading())
            try {
                pushRepository.removePushNotification()
            } catch(e: Exception) {
                //continue logging out
            }
            state = try {
                val loggedOut = userRepository.logOut()
                state.copy(logoutResult = Resource.Success(loggedOut))
            } catch (e: Exception) {
                state.copy(logoutResult = Resource.Success(false))
            }
        }
    }

    fun resetLogoutResource() {
        state = state.copy(logoutResult = Resource.Uninitialized)
    }

    fun setMultipleAccounts(multipleAccounts: DurableNonceViewModel.MultipleAccounts) {
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
        resetShouldDisplayApprovalDispositionError()
        resetApprovalDispositionState()
    }


    fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }


    //region API Calls
    private fun retrieveWalletApprovals() {
        viewModelScope.launch {
            state = state.copy(walletApprovalsResult = Resource.Loading())
            delay(250)
            state = try {
                val walletApprovals = approvalsRepository.getWalletApprovals()
                state.copy(
                    walletApprovalsResult = Resource.Success(walletApprovals),
                    approvals = walletApprovals
                )
            } catch (e: Exception) {
                state.copy(
                    walletApprovalsResult = Resource.Error(e.message ?: ""),
                    shouldShowErrorSnackbar = true
                )
            }
        }
    }

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

            val solanaApprovalRequestType =
                state.selectedApproval?.getSolanaApprovalRequestType()
            val approvalId = state.selectedApproval?.id ?: ""
            if (solanaApprovalRequestType == null) {
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

            if (state.selectedApproval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                try {
                    val multiSignOpDetails =
                        state.selectedApproval?.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                    val initiationDisposition = InitiationDisposition(
                        approvalDisposition = approvalDisposition,
                        nonces = nonces,
                        multiSigOpInitiationDetails = multiSignOpDetails
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

        const val KEY_SHOULD_REFRESH_DATA = "KEY_SHOULD_REFRESH_DATA"
    }

}