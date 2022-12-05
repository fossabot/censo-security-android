package com.censocustody.mobile.presentation.approvals

import androidx.lifecycle.viewModelScope
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.CensoCountDownTimer
import com.censocustody.mobile.data.ApprovalsRepository
import com.censocustody.mobile.data.KeyRepository
import com.censocustody.mobile.data.models.approval.ApprovalRequest
import com.censocustody.mobile.data.models.CipherRepository
import com.censocustody.mobile.presentation.common_approvals.CommonApprovalsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository,
    keyRepository: KeyRepository,
    cipherRepository: CipherRepository,
    timer: CensoCountDownTimer,
) : CommonApprovalsViewModel(
    approvalsRepository = approvalsRepository,
    keyRepository = keyRepository,
    cipherRepository = cipherRepository,
    timer = timer
) {
    //region Method Overrides
    override fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequest?,
        isInitiationRequest: Boolean,
        isApproving: Boolean,
        dialogMessages: Pair<String, String>,
    ) {
        val (dialogDetails, approvalDisposition) = getDialogDetailsAndApprovalDispositionType(
            isApproving = isApproving,
            dialogMessages = dialogMessages
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

    fun resetApprovalsData() {
        state = state.copy(
            approvals = emptyList(),
            approvalsResultRequest = Resource.Uninitialized,
            selectedApproval = null,
            multipleAccounts = null
        )
    }

    fun resetWalletApprovalsResult() {
        state = state.copy(approvalsResultRequest = Resource.Uninitialized)
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

            state = state.copy(approvalsResultRequest = Resource.Loading())
            delay(250)

            val walletApprovalsResource = approvalsRepository.getApprovalRequests()

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
                approvalsResultRequest = walletApprovalsResource,
                approvals = approvals
            )
        }
    }
    //endregion

    object Companion {
        const val KEY_SHOULD_REFRESH_DATA = "KEY_SHOULD_REFRESH_DATA"
    }
}