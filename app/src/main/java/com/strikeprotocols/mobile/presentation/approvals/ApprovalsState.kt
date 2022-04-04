package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel.BlockHash

data class ApprovalsState(
    val shouldShowErrorSnackbar: Boolean = false,
    val shouldDisplayConfirmDispositionDialog: ConfirmDispositionDialogDetails? = null,
    val blockHash: BlockHash? = null,

    //Async Data
    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState(),
    val walletApprovalsResult: Resource<List<WalletApproval?>> = Resource.Uninitialized,
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
    val approvals: List<WalletApproval?> = emptyList(),
    val selectedApproval: WalletApproval? = null
) {
    val loadingData = walletApprovalsResult is Resource.Loading ||
            logoutResult is Resource.Loading ||
            approvalDispositionState?.loadingData == true
}