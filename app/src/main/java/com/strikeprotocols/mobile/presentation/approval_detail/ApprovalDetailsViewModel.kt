package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.common_approvals.CommonApprovalsViewModel
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalRetryData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApprovalDetailsViewModel @Inject constructor(
    approvalsRepository: ApprovalsRepository,
    keyRepository: KeyRepository,
    timer: StrikeCountDownTimer,
) : CommonApprovalsViewModel(
    approvalsRepository = approvalsRepository,
    keyRepository = keyRepository,
    timer = timer
) {
    //region Method Overrides
    override fun handleInitialData(approval: WalletApproval) {
        state = state.copy(selectedApproval = approval)
    }

    override fun handleEmptyInitialData() {
        setShouldKickOutUser()
    }

    override fun setShouldDisplayConfirmDispositionDialog(
        approval: WalletApproval?,
        isInitiationRequest: Boolean,
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
                approvalRetryData = ApprovalRetryData(
                    isApproving = isApproving,
                    isInitiationRequest = isInitiationRequest
                )
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

    private fun setShouldKickOutUser() {
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
        resetApprovalDispositionState()
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }
}