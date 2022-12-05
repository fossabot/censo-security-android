package com.strikeprotocols.mobile.data.models

import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails

data class RegisterApprovalDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val nonces: List<Nonce>? = emptyList(),
    val approvalRequestType: ApprovalRequestDetails?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || nonces == null || approvalRequestType == null
}

enum class ApprovalDisposition(val value: String) {
    @SerializedName("Approve") APPROVE("Approve"),
    @SerializedName("Deny") DENY("Deny");
}