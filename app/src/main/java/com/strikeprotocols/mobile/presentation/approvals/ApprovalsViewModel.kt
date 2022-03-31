package com.strikeprotocols.mobile.presentation.approvals

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.generateWalletApprovalsDummyData
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel.Companion.UPDATE_COUNTDOWN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var timer: CountDownTimer? = null

    var state by mutableStateOf(ApprovalsState())
        private set

    init {
        refreshData()
    }

    fun refreshData() {
        retrieveWalletApprovals()
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
                countdownApprovalsOneSecond()
            }
            override fun onFinish() {}
        }
        timer?.start()
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
            shouldDisplayConfirmDispositionDialog = dialogDetails,
            selectedApproval = approval,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(
                    approvalDisposition
                )
            )
        )
    }

    fun resetShouldDisplayConfirmDispositionDialog() {
        state = state.copy(shouldDisplayConfirmDispositionDialog = null)
    }

    fun setPromptTrigger() {
        state = state.copy(triggerBioPrompt = true)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = false)
    }

    fun resetShouldShowErrorSnackbar() {
        state = state.copy(shouldShowErrorSnackbar = false)
    }

    fun logout() {
        viewModelScope.launch {
            state = state.copy(logoutResult = Resource.Loading())
            try {
                val loggedOut = userRepository.logOut()
                state = state.copy(logoutResult = Resource.Success(loggedOut))
            } catch (e: Exception) {
                state = state.copy(logoutResult = Resource.Success(false))
            }
        }
    }

    fun resetLogoutResource() {
        state = state.copy(logoutResult = Resource.Uninitialized)
    }

    //region API Calls
    private fun retrieveWalletApprovals() {
        viewModelScope.launch {
            state = state.copy(walletApprovalsResult = Resource.Loading())
            delay(500)
            state = try {
                val walletApprovals = approvalsRepository.getWalletApprovals()
                state.copy(
                    walletApprovalsResult = Resource.Success(walletApprovals),
                    //TODO Revert this back to real data after flow is done
                    approvals = listOf(generateWalletApprovalsDummyData())//walletApprovals
                )
            } catch (e: Exception) {
                state.copy(
                    walletApprovalsResult = Resource.Error(e.message ?: ""),
                    shouldShowErrorSnackbar = true
                )
            }
        }
    }

    fun countdownApprovalsOneSecond() {
        if (state.approvals.isNotEmpty()) {
            val countdownList = mutableListOf<WalletApproval>()
            for (approval in state.approvals) {
                if (approval?.approvalTimeoutInSeconds != null) {
                    val countdownApproval = approval.copy(
                        approvalTimeoutInSeconds = approval.approvalTimeoutInSeconds - 1
                    )
                    countdownList.add(countdownApproval)
                }
            }
            state = state.copy(approvals = countdownList)
        }
    }
    //endregion
    fun resetApprovalDispositionAPICalls() {
        resetDispositionState()
    }

    private fun resetDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    private suspend fun retrieveRecentBlockHash() {
        state = state.copy(
            approvalDispositionState = state.approvalDispositionState?.copy(
                recentBlockhashResult = Resource.Loading()
            )
        )
        state = try {
            val recentBlockhash = approvalsRepository.getRecentBlockHash()
            state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    recentBlockhashResult = Resource.Success(recentBlockhash)
                )
            )
        } catch (e: Exception) {
            state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    recentBlockhashResult = Resource.Error(e.message ?: "")
                )
            )
        }
    }

    private suspend fun signData() {
        state = state.copy(
            approvalDispositionState = state.approvalDispositionState?.copy(
                signingDataResult = Resource.Loading()
            )
        )
        delay(250)
        state = state.copy(
            approvalDispositionState = state.approvalDispositionState?.copy(
                signingDataResult = Resource.Success("I am signed data")
            )
        )
    }

    fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )
            try {
                //Data retrieval and checks
                retrieveRecentBlockHash()
                val blockHashResult = state.approvalDispositionState?.recentBlockhashResult
                val recentBlockHash = if (blockHashResult is Resource.Success) blockHashResult.data?.result?.value?.blockhash else null
                if (recentBlockHash == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.BLOCKHASH_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.BLOCKHASH_FAILURE.error)
                        )
                    )
                    return@launch
                }

                signData()
                val signResult = state.approvalDispositionState?.signingDataResult
                val signature = if (signResult is Resource.Success) signResult.data else null
                if (signature == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.SIGNING_DATA_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
                        )
                    )
                    return@launch
                }

                val recentApprovalDisposition = state.approvalDispositionState?.approvalDisposition
                val approvalDisposition = if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
                if (approvalDisposition == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE.error)
                        )
                    )
                    return@launch
                }

                val registerApprovalDisposition = RegisterApprovalDisposition(
                    approvalDisposition = approvalDisposition,
                    signature = signature,
                    recentBlockhash = recentBlockHash
                )

                val approvalDispositionResponse =
                    approvalsRepository.approveOrDenyDisposition(
                        state.selectedApproval?.id, registerApprovalDisposition
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

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }

}