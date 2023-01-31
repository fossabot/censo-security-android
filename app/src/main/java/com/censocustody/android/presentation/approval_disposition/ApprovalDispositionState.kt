package com.censocustody.android.presentation.approval_disposition

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approval.ApprovalDispositionRequest
import com.censocustody.android.data.models.approval.InitiationRequest
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body> = Resource.Uninitialized,
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