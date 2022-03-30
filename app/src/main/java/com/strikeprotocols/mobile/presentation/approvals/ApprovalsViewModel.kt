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
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
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

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }

}