package com.strikeprotocols.mobile.presentation.approval_disposition

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.WalletApproval

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val recentBlockhashResult: Resource<RecentBlockHashResponse> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<RegisterApprovalDisposition> = Resource.Uninitialized,
    val signingDataResult: Resource<String> = Resource.Uninitialized,
    val selectedApproval: WalletApproval? = null,
    val approvalDispositionError: ApprovalDispositionError = ApprovalDispositionError.NONE
) {
    val loadingData = recentBlockhashResult is Resource.Loading ||
            registerApprovalDispositionResult is Resource.Loading ||
            signingDataResult is Resource.Loading
}

enum class ApprovalDispositionError(val error: String) {
    NONE("NONE"),
    BLOCKHASH_FAILURE("BLOCKHASH_FAILURE"),
    SIGNING_DATA_FAILURE("SIGNING_DATA_FAILURE"),
    SUBMIT_FAILURE("SUBMIT_FAILURE"),
    APPROVAL_DISPOSITION_FAILURE("APPROVAL_DISPOSITION_FAILURE")
}