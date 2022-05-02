package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.*
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
    @Transient val details: SolanaApprovalRequestDetails?
) {

    fun getSolanaApprovalRequestType() =
        when(details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> details.requestType
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> details.requestType
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }

    fun isInitiationRequest() : Boolean =
        when(details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> false
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> true
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
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
