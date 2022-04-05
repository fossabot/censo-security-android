package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.strikeprotocols.mobile.data.models.approval.MultiSigOpInitiation.Companion.MULTI_SIG_KEY
import java.lang.reflect.Type

class WalletApprovalDeserializer : JsonDeserializer<WalletApproval> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): WalletApproval {
        return parseData(json = json)
    }

    fun parseData(json: JsonElement?): WalletApproval {
        try {
            val approvalTypeAndDetails =
                SolanaApprovalRequestDetails.getTypeAndDetailsFromJson(json)
            val approvalType = ApprovalType.fromString(approvalTypeAndDetails.type)

            val solanaApprovalRequestDetails: SolanaApprovalRequestDetails =
                if (approvalTypeAndDetails.type == MULTI_SIG_KEY) {
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
            else -> SolanaApprovalRequestType.UnknownApprovalType
        }
    }
}