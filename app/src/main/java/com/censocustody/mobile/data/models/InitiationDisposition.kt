package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails

data class InitiationDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val nonces: List<Nonce>? = emptyList(),
    val multiSigOpInitiationDetails : SolanaApprovalRequestDetails.MultiSignOpInitiationDetails?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || nonces == null || multiSigOpInitiationDetails == null
}