package com.strikeprotocols.mobile.data.models

import com.google.gson.annotations.SerializedName

data class RegisterApprovalDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val recentBlockhash: String?,
    val signature: String?
) {
}

enum class ApprovalDisposition(val value: String) {
    @SerializedName("None") NONE("None"),
    @SerializedName("Approve") APPROVE("Approve"),
    @SerializedName("Deny") DENY("Deny")
}