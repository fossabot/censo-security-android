package com.strikeprotocols.mobile.data.models

import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError

data class RegisterApprovalDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val recentBlockhash: String?,
    val solanaApprovalRequestType: SolanaApprovalRequestType?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || recentBlockhash == null || solanaApprovalRequestType == null

    fun getError() =
        when {
            approvalDisposition == null -> ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE
            recentBlockhash == null -> ApprovalDispositionError.BLOCKHASH_FAILURE
            solanaApprovalRequestType == null -> ApprovalDispositionError.SIGNING_DATA_FAILURE
            else -> ApprovalDispositionError.GENERIC_FAILURE
        }
}

enum class ApprovalDisposition(val value: String) {
    @SerializedName("Approve") APPROVE("Approve"),
    @SerializedName("Deny") DENY("Deny");
}