package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.WalletApproval

data class ApprovalDetailsState(
    val approval: WalletApproval? = null,
    val shouldDisplayConfirmDispositionDialog: ConfirmDispositionDialogDetails? = null,
    val triggerBioPrompt: Boolean = false,

    //Async Data
    //TODO swap out the data types when ready
    val recentBlockhashResult: Resource<String> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<Boolean> = Resource.Uninitialized,
    val signingDataResult: Resource<Any> = Resource.Uninitialized
) {
    val loadingData = recentBlockhashResult is Resource.Loading ||
            registerApprovalDispositionResult is Resource.Loading ||
            signingDataResult is Resource.Loading
}

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogTitle: String,
    val dialogText: String
)