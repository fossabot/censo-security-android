package com.strikeprotocols.mobile.presentation.approval_detail

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel.Companion.UPDATE_COUNTDOWN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

        state = state.copy(shouldDisplayConfirmDispositionDialog = dialogDetails)
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

    private fun startCountDown() {
        timer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_COUNTDOWN) {
            override fun onTick(millisecs: Long) {
                countdownApprovalOneSecond()
            }

            override fun onFinish() {}
        }
        timer?.start()
    }

    fun countdownApprovalOneSecond() {
        val approval = state.approval
        approval?.approvalTimeoutInSeconds?.let { safeApprovalTimeoutInSeconds ->
            if (safeApprovalTimeoutInSeconds > 0) {
                val countdownApproval =
                    approval.copy(approvalTimeoutInSeconds = approval.approvalTimeoutInSeconds - 1)
                state = state.copy(approval = countdownApproval)
            } else if (safeApprovalTimeoutInSeconds == 0) {
                onStop()
            }
        }
    }

    fun resetApprovalDispositionAPICalls() {
        resetRecentBlockHashResult()
        resetRegisterApprovalDispositionResult()
    }

    fun resetRecentBlockHashResult() {
        state = state.copy(recentBlockhashResult = Resource.Uninitialized)
    }

    fun resetRegisterApprovalDispositionResult() {
        state = state.copy(registerApprovalDispositionResult = Resource.Uninitialized)
    }

    //region API calls
    private fun retrieveRecentBlockhash() {
        viewModelScope.launch {
            state = state.copy(recentBlockhashResult = Resource.Loading())
            state = try {
                val recentBlockhash = approvalsRepository.getRecentBlockHash()
                state.copy(
                    recentBlockhashResult = Resource.Success(recentBlockhash)
                )
            } catch (e: Exception) {
                state.copy(
                    recentBlockhashResult = Resource.Error(e.message ?: "")
                )
            }
        }
    }

    private fun signData() {
        viewModelScope.launch {
            state = state.copy(signingDataResult = Resource.Loading())
            delay(3000)
            state = state.copy(signingDataResult = Resource.Success(Any()))
        }
    }

    fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(registerApprovalDispositionResult = Resource.Loading())
            strikeLog(message = "Starting approval disposition work")
            state = try {
                val recentBlockhash = approvalsRepository.getRecentBlockHash()
                state = state.copy(
                    recentBlockhashResult = Resource.Success(recentBlockhash)
                )
                strikeLog(message = "Signing data")
                //signing data delay
                delay(3000)

                strikeLog(message = "registering disposition")
                val approvalDispositionResponse = approvalsRepository.registerApprovalDisposition()
                state.copy(
                    registerApprovalDispositionResult = Resource.Success(approvalDispositionResponse)
                )
            } catch (e: Exception) {
                state.copy(
                    registerApprovalDispositionResult = Resource.Error(e.message ?: "")
                )
            }
        }
    }
    //endregion

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}

