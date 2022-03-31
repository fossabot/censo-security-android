package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState

data class ApprovalDetailsState(
    val approval: WalletApproval? = null,
    val shouldDisplayConfirmDispositionDialog: ConfirmDispositionDialogDetails? = null,
    val triggerBioPrompt: Boolean = false,

    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState()
) {
    val loadingData = approvalDispositionState?.loadingData == true
}

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogTitle: String,
    val dialogText: String
)