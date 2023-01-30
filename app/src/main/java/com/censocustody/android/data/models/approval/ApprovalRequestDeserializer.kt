package com.censocustody.android.data.models.approval

import com.google.gson.*
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.DATA_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.DETAILS_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.MULTI_SIG_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.REQUEST_TYPE_JSON_KEY
import com.censocustody.android.data.models.approval.ApprovalTypeMetaData.Companion.TYPE_JSON_KEY
import com.censocustody.android.data.models.approval.MultiSigOpInitiation.Companion.MULTI_SIG_TYPE
import java.lang.reflect.Type

class ApprovalRequestDeserializer : JsonDeserializer<ApprovalRequest> {
    private fun getGson() = GsonBuilder()
        .registerTypeAdapterFactory(TypeFactorySettings.signingDataAdapterFactory)
        .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
        .create()

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ApprovalRequest {
        return parseData(json = json)
    }

    fun toObjectWithParsedDetails(json: String?) : ApprovalRequest {
        val jsonElement = JsonParser.parseString(json)
        var approvalRequest = getGson().fromJson(json, ApprovalRequest::class.java)

        if(jsonElement !is JsonObject) {
            return approvalRequest.unknownApprovalType()
        }

        val jsonObject = jsonElement.asJsonObject.get(DETAILS_JSON_KEY)

        if(jsonObject !is JsonObject) {
            return approvalRequest.unknownApprovalType()
        }

        if(jsonObject.has(MULTI_SIG_JSON_KEY)) {
            val multiSigOpJson = jsonObject.get(MULTI_SIG_JSON_KEY)

            if(multiSigOpJson !is JsonObject) {
                return approvalRequest.unknownApprovalType()
            }
            val multiSigInitiation =
                getGson().fromJson(multiSigOpJson, MultiSigOpInitiation::class.java)

            val requestType = getRequestTypeFromParsedDetails(jsonObject)

            val multiSignOpInitiationDetails =
                SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(
                    requestType = requestType,
                    multisigOpInitiation = multiSigInitiation
                )
            approvalRequest = approvalRequest.copy(details = multiSignOpInitiationDetails)
        } else {
            val requestType = getRequestTypeFromParsedDetails(jsonObject)

            val approvalRequestDetails =
                SolanaApprovalRequestDetails.ApprovalRequestDetails(requestType = requestType)
            approvalRequest = approvalRequest.copy(details = approvalRequestDetails)
        }

        return approvalRequest
    }

    private fun getRequestTypeFromParsedDetails(jsonObject: JsonObject) : ApprovalRequestDetails {
        if (!jsonObject.has(REQUEST_TYPE_JSON_KEY)) {
            return ApprovalRequestDetails.UnknownApprovalType
        }
        val requestTypeJson = jsonObject.get(REQUEST_TYPE_JSON_KEY)

        if (requestTypeJson !is JsonObject) {
            return ApprovalRequestDetails.UnknownApprovalType
        }

        val requestString = requestTypeJson.get(TYPE_JSON_KEY)

        if(requestString !is JsonPrimitive) {
            return ApprovalRequestDetails.UnknownApprovalType
        }

        val approvalType =
            ApprovalType.fromString(requestString.asString)

        return getStandardApprovalType(approvalType, requestTypeJson)
    }

    private fun getRequestTypeFromSignDataJson(jsonObject: JsonObject) : ApprovalRequestDetails {
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

    fun parseData(json: JsonElement?): ApprovalRequest {
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

    private fun getStandardApprovalType(approvalType: ApprovalType, details: JsonElement?) : ApprovalRequestDetails {
        if (details == null) {
            return ApprovalRequestDetails.UnknownApprovalType
        }

        return when (approvalType) {
            ApprovalType.WITHDRAWAL_TYPE ->
                getGson().fromJson(details, ApprovalRequestDetails.WithdrawalRequest::class.java)
            ApprovalType.WALLET_CREATION_TYPE ->
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.WalletCreation::class.java
                )
            ApprovalType.DAPP_TRANSACTION_REQUEST_TYPE ->
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.DAppTransactionRequest::class.java
                )
            ApprovalType.LOGIN_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.LoginApprovalRequest::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_NAME_UPDATE_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.BalanceAccountNameUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.BalanceAccountSettingsUpdate::class.java
                )
            }
            ApprovalType.CREATE_ADDRESS_BOOK_ENTRY_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.CreateAddressBookEntry::class.java
                )
            }
            ApprovalType.DELETE_ADDRESS_BOOK_ENTRY_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.DeleteAddressBookEntry::class.java
                )
            }
            ApprovalType.WALLET_CONFIG_POLICY_UPDATE_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.WalletConfigPolicyUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_POLICY_UPDATE_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.BalanceAccountPolicyUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.BalanceAccountAddressWhitelistUpdate::class.java
                )
            }
            ApprovalType.ACCEPT_VAULT_INVITATION_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.AcceptVaultInvitation::class.java
                )
            }
            ApprovalType.PASSWORD_RESET_TYPE -> {
                getGson().fromJson(
                    details,
                    ApprovalRequestDetails.PasswordReset::class.java
                )
            }
            else -> ApprovalRequestDetails.UnknownApprovalType
        }
    }
}