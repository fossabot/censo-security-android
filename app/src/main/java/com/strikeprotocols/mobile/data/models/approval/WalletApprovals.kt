package com.strikeprotocols.mobile.data.models.approval

import android.content.Context
import com.google.gson.*
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.UriWrapper
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import java.lang.reflect.Modifier

data class WalletApproval(
    val approvalTimeoutInSeconds: Int?,
    val id: String?,
    val numberOfApprovalsReceived: Int?,
    val numberOfDeniesReceived: Int?,
    val numberOfDispositionsRequired: Int?,
    val submitDate: String?,
    val submitterEmail: String?,
    val submitterName: String?,
    val walletType: String?,
    val vaultName: String?,
    @Transient val details: SolanaApprovalRequestDetails?
) {

    fun getSolanaApprovalRequestType() =
        when(details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> details.requestType
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> details.requestType
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }

    fun approveButtonCaption(context: Context) =
        if (details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails && !details.multisigOpInitiation.initiatorIsApprover) {
            context.getString(R.string.initiate)
        } else {
            context.getString(R.string.approve)
        }

    fun retrieveAccountAddresses(): List<String> =
        when (details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> details.requestType.nonceAccountAddresses()
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> details.requestType.nonceAccountAddresses()
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }

    fun isInitiationRequest(): Boolean =
        when (details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> false
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> true
            else -> false
        }

    fun unknownApprovalType() : WalletApproval {
        return copy(
            details = SolanaApprovalRequestDetails.ApprovalRequestDetails(
                requestType = SolanaApprovalRequestType.UnknownApprovalType
            )
        )
    }

    companion object {
        fun toJson(approval: WalletApproval, uriWrapper: UriWrapper): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(approval)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): WalletApproval {
            val walletApprovalDeserializer = WalletApprovalDeserializer()
            return walletApprovalDeserializer.toObjectWithParsedDetails(json)
        }
    }
}
