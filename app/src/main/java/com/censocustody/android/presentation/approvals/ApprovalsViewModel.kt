package com.censocustody.android.presentation.approvals

import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.data.repository.ApprovalsRepository
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.repository.PushRepository
import com.censocustody.android.presentation.common_approvals.CommonApprovalsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val approvalsRepository: ApprovalsRepository,
    private val pushRepository: PushRepository,
    keyRepository: KeyRepository,
    timer: CensoCountDownTimer,
) : CommonApprovalsViewModel(
    approvalsRepository = approvalsRepository,
    keyRepository = keyRepository,
    timer = timer
) {
    //region Method Overrides
    override fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequestV2?,
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

    fun refreshFromAPush() {
        viewModelScope.launch {
            if (!userRepository.isTokenEmailVerified()) {
                refreshApprovalsData()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            refreshApprovalsData()
        }
    }

    fun userHasSeenPushDialog() = pushRepository.userHasSeenPushDialog()

    fun setUserSeenPushDialog(seenDialog: Boolean) =
        pushRepository.setUserSeenPushDialog(seenDialog)

    fun resetApprovalsData() {
        state = state.copy(
            approvals = emptyList(),
            approvalsResultRequest = Resource.Uninitialized,
            selectedApproval = null,
        )
    }

    fun triggerPushNotificationDialog() {
        state = state.copy(showPushNotificationsDialog = Resource.Success(Unit))
    }

    fun resetPushNotificationDialog() {
        state = state.copy(showPushNotificationsDialog = Resource.Uninitialized)
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
    private suspend fun refreshApprovalsData() {
        val cachedApprovals = state.approvals.toList()

        state = state.copy(approvalsResultRequest = Resource.Loading())
        delay(500)

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
    //endregion

    object Companion {
        const val KEY_SHOULD_REFRESH_DATA = "KEY_SHOULD_REFRESH_DATA"
    }
}