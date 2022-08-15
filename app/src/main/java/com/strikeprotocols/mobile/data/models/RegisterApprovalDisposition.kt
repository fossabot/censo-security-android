package com.strikeprotocols.mobile.data.models

import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType

data class RegisterApprovalDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val nonces: List<Nonce>? = emptyList(),
    val solanaApprovalRequestType: SolanaApprovalRequestType?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || nonces == null || solanaApprovalRequestType == null
}

enum class ApprovalDisposition(val value: String) {
    @SerializedName("Approve") APPROVE("Approve"),
    @SerializedName("Deny") DENY("Deny");
}