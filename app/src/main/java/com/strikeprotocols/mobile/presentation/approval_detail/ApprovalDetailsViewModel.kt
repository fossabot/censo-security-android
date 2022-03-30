package com.strikeprotocols.mobile.presentation.approval_detail

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel.Companion.UPDATE_COUNTDOWN
import dagger.hilt.android.lifecycle.HiltViewModel
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

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}

