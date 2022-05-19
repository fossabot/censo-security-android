package com.strikeprotocols.mobile.presentation.approval_disposition

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.InitiationDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<ApprovalDispositionRequest.RegisterApprovalDispositionBody> = Resource.Uninitialized,
    val initiationDispositionResult: Resource<InitiationRequest.InitiateRequestBody> = Resource.Uninitialized,
    val selectedApproval: WalletApproval? = null,
    val approvalDispositionError: ApprovalDispositionError = ApprovalDispositionError.NONE
) {
    val loadingData = registerApprovalDispositionResult is Resource.Loading || initiationDispositionResult is Resource.Loading
}

enum class ApprovalDispositionError(val error: String) {
    NONE("NONE"),
    DURABLE_NONCE_FAILURE("DURABLE_NONCE_FAILURE"),
    SIGNING_DATA_FAILURE("SIGNING_DATA_FAILURE"),
    SUBMIT_FAILURE("SUBMIT_FAILURE"),
    APPROVAL_DISPOSITION_FAILURE("APPROVAL_DISPOSITION_FAILURE"),
    GENERIC_FAILURE("GENERIC_FAILURE")
}