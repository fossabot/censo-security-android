package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel

data class ApprovalDetailsState(
    val shouldKickOutUserToApprovalsScreen: Boolean = false,
    val shouldDisplayConfirmDisposition: ConfirmDispositionDialogDetails? = null,
    val shouldDisplayApprovalDispositionError: Boolean = false,
    val screenWasBackgrounded: Boolean = false,
    val approval: WalletApproval? = null,
    val blockHash: BlockHashViewModel.BlockHash? = null,

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