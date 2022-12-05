package com.censocustody.mobile.presentation.approval_disposition

import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.models.ApprovalDisposition
import com.censocustody.mobile.data.models.approval.ApprovalDispositionRequest
import com.censocustody.mobile.data.models.approval.InitiationRequest
import com.censocustody.mobile.data.models.approval.ApprovalRequest

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<ApprovalDispositionRequest.RegisterApprovalDispositionBody> = Resource.Uninitialized,
    val initiationDispositionResult: Resource<InitiationRequest.InitiateRequestBody> = Resource.Uninitialized,
    val selectedApproval: ApprovalRequest? = null,
    val approvalRetryData: ApprovalRetryData = ApprovalRetryData()
) {
    val loadingData = registerApprovalDispositionResult is Resource.Loading || initiationDispositionResult is Resource.Loading
}
data class ApprovalRetryData(
    val isApproving: Boolean = false,
    val isInitiationRequest: Boolean = false
)