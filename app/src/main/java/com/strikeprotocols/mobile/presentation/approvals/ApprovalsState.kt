package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.data.models.WalletApprovals

data class ApprovalsState(
    val shouldShowErrorSnackbar: Boolean = false,

    //Async Data
    val walletApprovalsResult: Resource<WalletApprovals> = Resource.Uninitialized,
    val approvals: List<WalletApproval?> = emptyList(),
) {
    val loadingData = walletApprovalsResult is Resource.Loading
}