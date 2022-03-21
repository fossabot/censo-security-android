package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletApprovals

data class ApprovalsState(
    //Async Data
    val walletApprovalsResult: Resource<WalletApprovals> = Resource.Uninitialized
) {
    val loadingData = walletApprovalsResult is Resource.Loading
}