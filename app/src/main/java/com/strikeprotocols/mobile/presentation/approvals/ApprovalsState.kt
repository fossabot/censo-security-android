package com.strikeprotocols.mobile.presentation.approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionDialogDetails

data class ApprovalsState(
    val shouldShowErrorSnackbar: Boolean = false,
    val shouldDisplayConfirmDispositionDialog: ConfirmDispositionDialogDetails? = null,
    val triggerBioPrompt: Boolean = false,

    //Async Data
    val recentBlockhashResult: Resource<Any> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<Any> = Resource.Uninitialized,
    val signingDataResult: Resource<Any> = Resource.Uninitialized,
    val walletApprovalsResult: Resource<List<WalletApproval?>> = Resource.Uninitialized,
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
    val approvals: List<WalletApproval?> = emptyList(),
) {
    val loadingData = walletApprovalsResult is Resource.Loading ||
            logoutResult is Resource.Loading ||
            recentBlockhashResult is Resource.Loading ||
            registerApprovalDispositionResult is Resource.Loading ||
            signingDataResult is Resource.Loading
}