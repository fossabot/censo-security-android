package com.strikeprotocols.mobile.presentation.approval_disposition

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.ApprovalsRepositoryImpl.RegisterApprovalDispositionBody
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.data.models.RegisterApprovalDisposition
import com.strikeprotocols.mobile.data.models.WalletApproval

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<RegisterApprovalDispositionBody> = Resource.Uninitialized,
    val selectedApproval: WalletApproval? = null,
    val approvalDispositionError: ApprovalDispositionError = ApprovalDispositionError.NONE
) {
    val loadingData = registerApprovalDispositionResult is Resource.Loading
}

enum class ApprovalDispositionError(val error: String) {
    NONE("NONE"),
    BLOCKHASH_FAILURE("BLOCKHASH_FAILURE"),
    SIGNING_DATA_FAILURE("SIGNING_DATA_FAILURE"),
    SUBMIT_FAILURE("SUBMIT_FAILURE"),
    APPROVAL_DISPOSITION_FAILURE("APPROVAL_DISPOSITION_FAILURE"),
    GENERIC_FAILURE("GENERIC_FAILURE")
}