package com.censocustody.android.data.models

import com.google.gson.annotations.SerializedName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2

data class RegisterApprovalDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val approvalRequestType: ApprovalRequestDetailsV2?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || approvalRequestType == null
}

enum class ApprovalDisposition(val value: String) {
    @SerializedName("Approve") APPROVE("Approve"),
    @SerializedName("Deny") DENY("Deny");
}