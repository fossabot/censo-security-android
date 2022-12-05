package com.censocustody.mobile.presentation.approval_detail

import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.CensoCountDownTimer
import com.censocustody.mobile.data.ApprovalsRepository
import com.censocustody.mobile.data.KeyRepository
import com.censocustody.mobile.data.models.approval.ApprovalRequest
import com.censocustody.mobile.data.models.CipherRepository
import com.censocustody.mobile.presentation.common_approvals.CommonApprovalsViewModel
import com.censocustody.mobile.presentation.approval_disposition.ApprovalRetryData
import com.censocustody.mobile.presentation.common_approvals.ApprovalsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApprovalDetailsViewModel @Inject constructor(
    approvalsRepository: ApprovalsRepository,
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
    override fun handleInitialData(approval: ApprovalRequest) {
        state = state.copy(selectedApproval = approval)
    }

    override fun handleEmptyInitialData() {
        setShouldKickOutUser()
    }

    override fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequest?,
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