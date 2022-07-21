package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel

data class ApprovalsState(

    val email: String = "",
    val name: String = "",

    val shouldShowErrorSnackbar: Boolean = false,
    val shouldDisplayConfirmDisposition: ConfirmDispositionDialogDetails? = null,
    val shouldDisplayApprovalDispositionError: Boolean = false,
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,

    //Async Data
    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState(),
    val walletApprovalsResult: Resource<List<WalletApproval?>> = Resource.Uninitialized,
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
    val approvals: List<WalletApproval?> = emptyList(),
    val selectedApproval: WalletApproval? = null,
    val shouldRefreshTimers: Boolean = false
) {
    val loadingData = walletApprovalsResult is Resource.Loading ||
            logoutResult is Resource.Loading ||
            approvalDispositionState?.loadingData == true
}