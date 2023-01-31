package com.censocustody.android.data.models.approval.v2

import com.censocustody.android.data.models.approval.*

import com.google.gson.*
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.DATA_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.DETAILS_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.MULTI_SIG_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.REQUEST_TYPE_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.TYPE_JSON_KEY
import com.censocustody.android.data.models.approval.MultiSigOpInitiation.Companion.MULTI_SIG_TYPE
import java.lang.reflect.Type

class V2ApprovalDeserializer : JsonDeserializer<ApprovalRequestV2> {
    private fun getGson() = GsonBuilder()
        .registerTypeAdapterFactory(TypeFactorySettings.signingDataAdapterFactory)
        .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
        .create()

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ApprovalRequestV2 {
        return parseData(json = json)
    }

    fun toObjectWithParsedDetails(json: String?) : ApprovalRequestV2 {
        val jsonElement = JsonParser.parseString(json)
        var approvalRequest = getGson().fromJson(json, ApprovalRequestV2::class.java)

        if (jsonElement !is JsonObject) {
            return approvalRequest.unknownApprovalType()
        }

        val detailsJsonObject = jsonElement.asJsonObject.get(DETAILS_JSON_KEY)

        if (detailsJsonObject !is JsonObject) {
            return approvalRequest.unknownApprovalType()
        }

        //todo: Check with brendan on multi sig op and the key
//        if(detailsJsonObject.has(MULTI_SIG_JSON_KEY)) {
//            val multiSigOpJson = detailsJsonObject.get(MULTI_SIG_JSON_KEY)
//
//            if(multiSigOpJson !is JsonObject) {
//                return approvalRequest.unknownApprovalType()
//            }
//            val multiSigInitiation =
//                getGson().fromJson(multiSigOpJson, MultiSigOpInitiation::class.java)
//
//            val requestType = getRequestTypeFromParsedDetails(detailsJsonObject)
//
//            val multiSignOpInitiationDetails =
//                SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(
//                    requestType = requestType,
//                    multisigOpInitiation = multiSigInitiation
//                )
//            approvalRequest = approvalRequest.copy(details = multiSignOpInitiationDetails)
//        } else {
        val requestType = getRequestTypeFromParsedDetails(detailsJsonObject)

        val approvalRequestDetails =
            SolanaApprovalRequestDetails.ApprovalRequestDetails(requestType = requestType)
        approvalRequest = approvalRequest.copy(details = approvalRequestDetails)
        //}

        return approvalRequest
    }

    private fun getRequestTypeFromParsedDetails(jsonObject: JsonObject) : ApprovalRequestDetailsV2 {
        if (!jsonObject.has(REQUEST_TYPE_JSON_KEY)) {
            return ApprovalRequestDetailsV2.UnknownV2ApprovalType
        }
        val requestTypeJson = jsonObject.get(REQUEST_TYPE_JSON_KEY)

        if (requestTypeJson !is JsonObject) {
            return ApprovalRequestDetailsV2.UnknownV2ApprovalType
        }

        val requestString = requestTypeJson.get(TYPE_JSON_KEY)

        if (requestString !is JsonPrimitive) {
            return ApprovalRequestDetailsV2.UnknownV2ApprovalType
        }

        val approvalType =
            ApprovalType.fromString(requestString.asString)

        return getStandardApprovalType(approvalType, requestTypeJson)
    }

    private fun getRequestTypeFromSignDataJson(jsonObject: JsonObject) : V2ApprovalType {
        if (!jsonObject.has(DATA_JSON_KEY)) {
            return ApprovalRequestDetails.UnknownApprovalType
        }
        val dataJson = jsonObject.get(DATA_JSON_KEY)

        if (dataJson !is JsonObject) {
            return ApprovalRequestDetails.UnknownApprovalType
        }

        val requestString = dataJson.get(TYPE_JSON_KEY)

        if(requestString !is JsonPrimitive) {
            return ApprovalRequestDetails.UnknownApprovalType
        }

        val approvalType =
            ApprovalType.fromString(requestString.asString)

        return getStandardApprovalType(approvalType, dataJson)
    }

    fun parseData(json: JsonElement?): ApprovalRequestV2 {
        try {
            val approvalTypeAndDetails =
                SolanaApprovalRequestDetails.getTypeAndDetailsFromJson(json)
            val approvalType = ApprovalType.fromString(approvalTypeAndDetails.type)

            val solanaApprovalRequestDetails: SolanaApprovalRequestDetails =
                if (approvalTypeAndDetails.type == MULTI_SIG_TYPE) {
                    val multiSigOp =
                        getGson().fromJson(
                            approvalTypeAndDetails.details,
                            MultiSigOpInitiation::class.java
                        )

                    val innerTypeAndDetails =
                        SolanaApprovalRequestDetails.getTypeAndDetailsFromJson(
                            approvalTypeAndDetails.details
                        )
                    val innerApprovalType = ApprovalType.fromString(innerTypeAndDetails.type)
                    val innerRequestDetails = getStandardApprovalType(
                        approvalType = innerApprovalType,
                        details = innerTypeAndDetails.details
                    )

                    SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(
                        multisigOpInitiation = multiSigOp,
                        requestType = innerRequestDetails
                    )
                } else {
                    SolanaApprovalRequestDetails.ApprovalRequestDetails(
                        requestType =
                        getStandardApprovalType(
                            approvalType = approvalType,
                            details = approvalTypeAndDetails.details
                        )
                    )
                }

            val approvalRequest = getGson().fromJson(json, ApprovalRequest::class.java)
            return approvalRequest.copy(details = solanaApprovalRequestDetails)
        } catch (e: Exception) {
            val approvalRequest = getGson().fromJson(json, ApprovalRequest::class.java)
            return approvalRequest.copy(
                details = SolanaApprovalRequestDetails.ApprovalRequestDetails(
                    ApprovalRequestDetails.UnknownApprovalType
                )
            )
        }
    }

    private fun getStandardApprovalType(approvalType: V2ApprovalType, details: JsonElement?) : ApprovalRequestDetailsV2 {
        if (details == null) {
            return ApprovalRequestDetailsV2.UnknownV2ApprovalType
        }

        return when (approvalType) {
            V2ApprovalType.VaultPolicyUpdateType ->
                getGson().fromJson(details, ApprovalRequestDetailsV2.VaultPolicyUpdate::class.java)
            V2ApprovalType.BitcoinWalletCreationType ->
                getGson().fromJson(details, ApprovalRequestDetailsV2.BitcoinWalletCreation::class.java)
            V2ApprovalType.EthereumWalletCreationType ->
                getGson().fromJson(details, ApprovalRequestDetailsV2.EthereumWalletCreation::class.java)
            V2ApprovalType.PolygonWalletCreationType ->
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonWalletCreation::class.java
                )
            V2ApprovalType.EthereumWalletNameUpdateType ->
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.EthereumWalletNameUpdate::class.java
                )
            V2ApprovalType.PolygonWalletNameUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonWalletNameUpdate::class.java
                )
            }
            V2ApprovalType.EthereumWalletWhitelistUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate::class.java
                )
            }
            V2ApprovalType.PolygonWalletWhitelistUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate::class.java
                )
            }
            V2ApprovalType.EthereumWalletSettingsUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate::class.java
                )
            }
            V2ApprovalType.PolygonWalletSettingsUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate::class.java
                )
            }
            V2ApprovalType.EthereumTransferPolicyUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate::class.java
                )
            }
            V2ApprovalType.PolygonTransferPolicyUpdateType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate::class.java
                )
            }
            V2ApprovalType.CreateAddressBookEntryType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.CreateAddressBookEntry::class.java
                )
            }
            V2ApprovalType.DeleteAddressBookEntryType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.DeleteAddressBookEntry::class.java
                )
            }
            V2ApprovalType.BitcoinWithdrawalRequestType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.BitcoinWithdrawalRequest::class.java
                )
            }
            V2ApprovalType.EthereumWithdrawalRequestType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.EthereumWithdrawalRequest::class.java
                )
            }
            V2ApprovalType.PolygonWithdrawalRequestType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PolygonWithdrawalRequest::class.java
                )
            }
            V2ApprovalType.PasswordResetType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.PasswordReset::class.java
                )
            }
            V2ApprovalType.LoginType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.Login::class.java
                )
            }
            V2ApprovalType.VaultInvitationType -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetailsV2.VaultInvitation::class.java
                )
            }
            else -> ApprovalRequestDetailsV2.UnknownV2ApprovalType
        }
    }
}