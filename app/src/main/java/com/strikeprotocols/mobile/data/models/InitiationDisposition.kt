package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError

data class InitiationDisposition(
    val approvalDisposition: ApprovalDisposition?,
    val nonces: List<Nonce>? = emptyList(),
    val multiSigOpInitiationDetails : SolanaApprovalRequestDetails.MultiSignOpInitiationDetails?
) {

    fun anyItemNull() : Boolean =
        approvalDisposition == null || nonces == null || multiSigOpInitiationDetails == null

    fun getError() =
        when {
            approvalDisposition == null -> ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE
            nonces == null -> ApprovalDispositionError.DURABLE_NONCE_FAILURE
            multiSigOpInitiationDetails == null -> ApprovalDispositionError.SIGNING_DATA_FAILURE
            else -> ApprovalDispositionError.GENERIC_FAILURE
        }
}