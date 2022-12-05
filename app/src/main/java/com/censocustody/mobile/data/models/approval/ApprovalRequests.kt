package com.censocustody.mobile.data.models.approval

import android.content.Context
import com.google.gson.*
import com.censocustody.mobile.R
import com.censocustody.mobile.common.UriWrapper
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails.Companion.UNKNOWN_REQUEST_APPROVAL
import java.lang.reflect.Modifier

data class ApprovalRequest(
    val approvalTimeoutInSeconds: Int?,
    val id: String?,
    val numberOfApprovalsReceived: Int?,
    val numberOfDeniesReceived: Int?,
    val numberOfDispositionsRequired: Int?,
    val submitDate: String?,
    val submitterEmail: String?,
    val submitterName: String?,
    val vaultName: String?,
    @Transient val details: SolanaApprovalRequestDetails?
) {

    fun getApprovalRequestType() =
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

    fun retrieveAccountAddressesSlot(): Int =
        when (details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> details.requestType.nonceAccountAddressesSlot()
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> details.requestType.nonceAccountAddressesSlot()
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }

    fun isInitiationRequest(): Boolean =
        when (details) {
            is SolanaApprovalRequestDetails.ApprovalRequestDetails -> false
            is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails -> true
            else -> false
        }

    fun unknownApprovalType() : ApprovalRequest {
        return copy(
            details = SolanaApprovalRequestDetails.ApprovalRequestDetails(
                requestType = ApprovalRequestDetails.UnknownApprovalType
            )
        )
    }

    companion object {
        fun toJson(approval: ApprovalRequest, uriWrapper: UriWrapper): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .registerTypeAdapterFactory(TypeFactorySettings.signingDataAdapterFactory)
                .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
                .create()
                .toJson(approval)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): ApprovalRequest {
            val approvalRequestDeserializer = ApprovalRequestDeserializer()
            return approvalRequestDeserializer.toObjectWithParsedDetails(json)
        }
    }
}
