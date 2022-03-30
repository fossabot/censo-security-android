package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.data.models.WalletApproval

data class ApprovalDetailsState(
    val approval: WalletApproval? = null,
    val shouldDisplayConfirmDispositionDialog: ConfirmDispositionDialogDetails? = null,
    val triggerBioPrompt: Boolean = false
)

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogTitle: String,
    val dialogText: String
)