package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.*
import com.strikeprotocols.mobile.data.models.approval.ApprovalTypeMetaData.Companion.DETAILS_JSON_KEY
import com.strikeprotocols.mobile.data.models.approval.ApprovalTypeMetaData.Companion.MULTI_SIG_JSON_KEY
import com.strikeprotocols.mobile.data.models.approval.ApprovalTypeMetaData.Companion.REQUEST_TYPE_JSON_KEY
import com.strikeprotocols.mobile.data.models.approval.ApprovalTypeMetaData.Companion.TYPE_JSON_KEY
import com.strikeprotocols.mobile.data.models.approval.MultiSigOpInitiation.Companion.MULTI_SIG_TYPE
import java.lang.reflect.Type

class WalletApprovalDeserializer : JsonDeserializer<WalletApproval> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): WalletApproval {
        return parseData(json = json)
    }

    fun toObjectWithParsedDetails(json: String?) : WalletApproval {
        val jsonElement = JsonParser.parseString(json)
        var walletApproval = Gson().fromJson(json, WalletApproval::class.java)

        if(jsonElement !is JsonObject) {
            return walletApproval.unknownApprovalType()
        }

        val jsonObject = jsonElement.asJsonObject.get(DETAILS_JSON_KEY)

        if(jsonObject !is JsonObject) {
            return walletApproval.unknownApprovalType()
        }

        if(jsonObject.has(MULTI_SIG_JSON_KEY)) {
            val multiSigOpJson = jsonObject.get(MULTI_SIG_JSON_KEY)

            if(multiSigOpJson !is JsonObject) {
                return walletApproval.unknownApprovalType()
            }
            val multiSigInitiation =
                Gson().fromJson(multiSigOpJson, MultiSigOpInitiation::class.java)

            val requestType = getRequestTypeFromParsedDetails(jsonObject)

            val multiSignOpInitiationDetails =
                SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(
                    requestType = requestType,
                    multisigOpInitiation = multiSigInitiation
                )
            walletApproval = walletApproval.copy(details = multiSignOpInitiationDetails)
        } else {
            val requestType = getRequestTypeFromParsedDetails(jsonObject)

            val approvalRequestDetails =
                SolanaApprovalRequestDetails.ApprovalRequestDetails(requestType = requestType)
            walletApproval = walletApproval.copy(details = approvalRequestDetails)
        }

        return walletApproval
    }

    private fun getRequestTypeFromParsedDetails(jsonObject: JsonObject) : SolanaApprovalRequestType {
        if (!jsonObject.has(REQUEST_TYPE_JSON_KEY)) {
            return SolanaApprovalRequestType.UnknownApprovalType
        }
        val requestTypeJson = jsonObject.get(REQUEST_TYPE_JSON_KEY)

        if (requestTypeJson !is JsonObject) {
            return SolanaApprovalRequestType.UnknownApprovalType
        }

        val requestString = requestTypeJson.get(TYPE_JSON_KEY)

        if(requestString !is JsonPrimitive) {
            return SolanaApprovalRequestType.UnknownApprovalType
        }

        val approvalType =
            ApprovalType.fromString(requestString.asString)

        return getStandardApprovalType(approvalType, requestTypeJson)
    }

    fun parseData(json: JsonElement?): WalletApproval {
        try {
            val approvalTypeAndDetails =
                SolanaApprovalRequestDetails.getTypeAndDetailsFromJson(json)
            val approvalType = ApprovalType.fromString(approvalTypeAndDetails.type)

            val solanaApprovalRequestDetails: SolanaApprovalRequestDetails =
                if (approvalTypeAndDetails.type == MULTI_SIG_TYPE) {
                    val multiSigOp =
                        Gson().fromJson(
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

            val walletApproval = Gson().fromJson(json, WalletApproval::class.java)
            return walletApproval.copy(details = solanaApprovalRequestDetails)
        } catch (e: Exception) {
            val walletApproval = Gson().fromJson(json, WalletApproval::class.java)
            return walletApproval.copy(
                details = SolanaApprovalRequestDetails.ApprovalRequestDetails(
                    SolanaApprovalRequestType.UnknownApprovalType
                )
            )
        }
    }

    private fun getStandardApprovalType(approvalType: ApprovalType, details: JsonElement?) : SolanaApprovalRequestType {
        if (details == null) {
            return SolanaApprovalRequestType.UnknownApprovalType
        }

        return when (approvalType) {
            ApprovalType.WITHDRAWAL_TYPE ->
                Gson().fromJson(details, SolanaApprovalRequestType.WithdrawalRequest::class.java)
            ApprovalType.CONVERSION_REQUEST_TYPE ->
                Gson().fromJson(details, SolanaApprovalRequestType.ConversionRequest::class.java)
            ApprovalType.SIGNERS_UPDATE_TYPE ->
                Gson().fromJson(details, SolanaApprovalRequestType.SignersUpdate::class.java)
            ApprovalType.BALANCE_ACCOUNT_CREATION_TYPE ->
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.BalanceAccountCreation::class.java
                )
            ApprovalType.DAPP_TRANSACTION_REQUEST_TYPE ->
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.DAppTransactionRequest::class.java
                )
            ApprovalType.LOGIN_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.LoginApprovalRequest::class.java
                )
            }
            ApprovalType.WRAP_CONVERSION_REQUEST_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.WrapConversionRequest::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_NAME_UPDATE_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.BalanceAccountNameUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.BalanceAccountSettingsUpdate::class.java
                )
            }
            ApprovalType.ADDRESS_BOOK_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.AddressBookUpdate::class.java
                )
            }
            ApprovalType.DAPP_BOOK_UPDATE_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.DAppBookUpdate::class.java
                )
            }
            ApprovalType.SPL_TOKEN_ACCOUNT_CREATION_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.SPLTokenAccountCreation::class.java
                )
            }
            ApprovalType.WALLET_CONFIG_POLICY_UPDATE_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.WalletConfigPolicyUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_POLICY_UPDATE_TYPE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.BalanceAccountPolicyUpdate::class.java
                )
            }
            ApprovalType.BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE -> {
                Gson().fromJson(
                    details,
                    SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate::class.java
                )
            }
            else -> SolanaApprovalRequestType.UnknownApprovalType
        }
    }
}