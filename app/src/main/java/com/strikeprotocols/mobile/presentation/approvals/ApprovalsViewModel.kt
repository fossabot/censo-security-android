package com.strikeprotocols.mobile.presentation.approvals

import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.common_approvals.CommonApprovalsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository,
    keyRepository: KeyRepository,
    timer: StrikeCountDownTimer,
) : CommonApprovalsViewModel(
    approvalsRepository = approvalsRepository,
    keyRepository = keyRepository,
    timer = timer
) {
    //region Method Overrides
    override fun setShouldDisplayConfirmDispositionDialog(
        approval: WalletApproval?,
        isInitiationRequest: Boolean,
        isApproving: Boolean,
        dialogTitle: String,
        dialogText: String
    ) {
        val (dialogDetails, approvalDisposition) = getDialogDetailsAndApprovalDispositionType(
            isApproving = isApproving,
            dialogTitle = dialogTitle,
            dialogText = dialogText
        )

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

    override fun handleScreenForegrounded() {
        refreshData()
    }
    //endregion

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

    fun resetShouldShowErrorSnackbar() {
        state = state.copy(shouldShowErrorSnackbar = false)
    }

    fun resetWalletApprovalsResult() {
        state = state.copy(walletApprovalsResult = Resource.Uninitialized)
    }

    fun wipeDataAfterDispositionSuccess() {
        resetApprovalsData()
        resetApprovalDispositionState()

        refreshData()
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
    //endregion

    object Companion {
        const val KEY_SHOULD_REFRESH_DATA = "KEY_SHOULD_REFRESH_DATA"
    }
}