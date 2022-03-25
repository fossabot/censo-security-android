package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletApproval

data class ApprovalsState(
    val shouldShowErrorSnackbar: Boolean = false,

    //Async Data
    val walletApprovalsResult: Resource<List<WalletApproval?>> = Resource.Uninitialized,
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
    val approvals: List<WalletApproval?> = emptyList(),
) {
    val loadingData = walletApprovalsResult is Resource.Loading || logoutResult is Resource.Loading
}