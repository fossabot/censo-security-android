package com.strikeprotocols.mobile.presentation.approval_disposition

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<ApprovalDispositionRequest.RegisterApprovalDispositionBody> = Resource.Uninitialized,
    val initiationDispositionResult: Resource<InitiationRequest.InitiateRequestBody> = Resource.Uninitialized,
    val selectedApproval: WalletApproval? = null,
    val approvalRetryData: ApprovalRetryData = ApprovalRetryData()
) {
    val loadingData = registerApprovalDispositionResult is Resource.Loading || initiationDispositionResult is Resource.Loading
}
data class ApprovalRetryData(
    val isApproving: Boolean = false,
    val isInitiationRequest: Boolean = false
)