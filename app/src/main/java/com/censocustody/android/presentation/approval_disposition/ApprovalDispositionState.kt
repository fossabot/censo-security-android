package com.censocustody.android.presentation.approval_disposition

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2

data class ApprovalDispositionState(
    val approvalDisposition: Resource<ApprovalDisposition> = Resource.Uninitialized,
    val registerApprovalDispositionResult: Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body> = Resource.Uninitialized,
    val selectedApproval: ApprovalRequestV2? = null,
    val approvalRetryData: ApprovalRetryData = ApprovalRetryData()
) {
    val loadingData = registerApprovalDispositionResult is Resource.Loading
}
data class ApprovalRetryData(val isApproving: Boolean = false)