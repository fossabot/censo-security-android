package com.censocustody.android.presentation.approval_detail

import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.data.repository.ApprovalsRepository
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.common_approvals.CommonApprovalsViewModel
import com.censocustody.android.presentation.approval_disposition.ApprovalRetryData
import com.censocustody.android.presentation.common_approvals.ApprovalsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApprovalDetailsViewModel @Inject constructor(
    approvalsRepository: ApprovalsRepository,
    keyRepository: KeyRepository,
    timer: CensoCountDownTimer,
) : CommonApprovalsViewModel(
    approvalsRepository = approvalsRepository,
    keyRepository = keyRepository,
    timer = timer
) {
    //region Method Overrides
    override fun handleInitialData(approval: ApprovalRequestV2) {
        state = state.copy(selectedApproval = approval)
    }

    override fun handleEmptyInitialData() {
        setShouldKickOutUser()
    }

    override fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequestV2?,
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    ) {

        val (dialogDetails, approvalDisposition) = getDialogDetailsAndApprovalDispositionType(
            isApproving = isApproving,
            dialogMessages = dialogMessages
        )

        state = state.copy(
            shouldDisplayConfirmDisposition = dialogDetails,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(approvalDisposition),
                approvalRetryData = ApprovalRetryData(isApproving = isApproving)
            )
        )
    }

    override fun handleScreenBackgrounded() {
        state = state.copy(screenWasBackgrounded = true)
    }

    override fun handleScreenForegrounded() {
        if (state.screenWasBackgrounded) {
            state = state.copy(screenWasBackgrounded = false)
            wipeDataAndKickUserOutToApprovalsScreen()
        }
    }
    //endregion

    fun checkIfApprovalHasBeenCleared(requestId: String) {
        if (requestId.isNotEmpty() && requestId == state.selectedApproval?.id) {
            setShouldKickOutUser()
        }
    }

    fun setShouldKickOutUser() {
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }

    fun resetShouldKickOutUser() {
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = false
        )
    }

    fun wipeDataAndKickUserOutToApprovalsScreen() {
        val cleanState = ApprovalsState()
        state = cleanState.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }
}