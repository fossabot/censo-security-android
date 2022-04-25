package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

sealed class SolanaApprovalRequestDetails {
    data class ApprovalRequestDetails(val requestType: SolanaApprovalRequestType) :
        SolanaApprovalRequestDetails()

    data class MultiSignOpInitiationDetails(
        val multisigOpInitiation: MultiSigOpInitiation,
        val requestType: SolanaApprovalRequestType
    ) : SolanaApprovalRequestDetails()

    companion object {
        fun getTypeAndDetailsFromJson(json: JsonElement?): ApprovalTypeMetaData {

            if (json == null || json !is JsonObject || !json.asJsonObject.has(ApprovalTypeMetaData.Companion.DETAILS_JSON_KEY)) {
                return ApprovalTypeMetaData(type = "", details = null)
            }

            val detailsData : JsonElement? =
                json.asJsonObject?.get(ApprovalTypeMetaData.Companion.DETAILS_JSON_KEY)

            val details : JsonObject? =
                if (detailsData is JsonObject) detailsData.asJsonObject else null

            val type : String =
                if (details != null &&
                    details.asJsonObject.has(ApprovalTypeMetaData.Companion.TYPE_JSON_KEY)) {
                    val typeData =
                        details.asJsonObject?.get(ApprovalTypeMetaData.Companion.TYPE_JSON_KEY)
                    if (typeData is JsonPrimitive && typeData.isString) {
                        typeData.asString
                    } else {
                        ""
                    }
                } else {
                    ""
                }

            return ApprovalTypeMetaData(type = type, details = details)
        }
    }
}

data class ApprovalTypeMetaData(
    val type: String,
    val details: JsonObject?
) {
    object Companion {
        const val DETAILS_JSON_KEY = "details"
        const val TYPE_JSON_KEY = "type"
        const val MULTI_SIG_JSON_KEY = "multisigOpInitiation"
        const val REQUEST_TYPE_JSON_KEY = "requestType"
    }
}

data class MultiSigOpInitiation(
    val opAccountCreationInfo: MultiSigAccountCreationInfo,
    val dataAccountCreationInfo: MultiSigAccountCreationInfo?,
) {
    companion object {
        const val MULTI_SIG_TYPE = "MultisigOpInitiation"
    }
}

sealed class SolanaApprovalRequestType {
    data class WithdrawalRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class ConversionRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val destinationSymbolInfo: SymbolInfo,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class SignersUpdate(
        val type: String,
        val slotUpdateType: SlotUpdateType,
        val signer: SlotSignerInfo,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class BalanceAccountCreation(
        val type: String,
        var accountSlot: Byte,
        var accountInfo: AccountInfo,
        var approvalsRequired: Byte,
        var approvalTimeout: Long,
        var approvers: List<SlotSignerInfo>,
        var whitelistEnabled: BooleanSetting,
        var dappsEnabled: BooleanSetting,
        var addressBookSlot: Byte,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class DAppTransactionRequest(
        val type: String,
        var account: AccountInfo,
        var dappInfo: SolanaDApp,
        var balanceChanges: List<SymbolAndAmountInfo>,
        var instructions: List<SolanaInstructionBatch>,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class LoginApprovalRequest(
        val type: String,
        var jwtToken: String
    ) : SolanaApprovalRequestType()

    object UnknownApprovalType : SolanaApprovalRequestType()

    companion object {
        const val INVALID_REQUEST_APPROVAL = "Invalid request for Approval"
        const val UNKNOWN_REQUEST_APPROVAL = "Unknown Approval"
        const val UNKNOWN_INITIATION = "Unknown Initiation"
    }
}

enum class ApprovalType(val value: String) {
    WITHDRAWAL_TYPE("WithdrawalRequest"),
    CONVERSION_REQUEST_TYPE("ConversionRequest"),
    SIGNERS_UPDATE_TYPE("SignersUpdate"),
    BALANCE_ACCOUNT_CREATION_TYPE("BalanceAccountCreation"),
    DAPP_TRANSACTION_REQUEST_TYPE("DAppTransactionRequest"),
    LOGIN_TYPE("LoginApproval"),
    UNKNOWN_TYPE("");

    companion object {
        fun fromString(type: String?): ApprovalType =
            when (type) {
                WITHDRAWAL_TYPE.value -> WITHDRAWAL_TYPE
                CONVERSION_REQUEST_TYPE.value -> CONVERSION_REQUEST_TYPE
                SIGNERS_UPDATE_TYPE.value -> SIGNERS_UPDATE_TYPE
                BALANCE_ACCOUNT_CREATION_TYPE.value -> BALANCE_ACCOUNT_CREATION_TYPE
                DAPP_TRANSACTION_REQUEST_TYPE.value -> DAPP_TRANSACTION_REQUEST_TYPE
                LOGIN_TYPE.value -> LOGIN_TYPE
                else -> UNKNOWN_TYPE
            }
    }
}

data class MultiSigAccountCreationInfo(
    val accountSize: Long,
    val minBalanceForRentExemption: Long
)

data class AccountInfo(
    val name: String,
    val identifier: String,
    val accountType: AccountType,
    val address: String?
)

data class SolanaSigningData(
    val feePayer: String,
    val walletProgramId: String,
    val multisigOpAccountAddress: String,
    val walletAddress: String,
)

enum class AccountType(val value: String) {
    @SerializedName("BalanceAccount") BalanceAccount("BalanceAccount"),
    @SerializedName("StakeAccount") StakeAccount("StakeAccount")
}

data class SymbolAndAmountInfo(
    val symbolInfo: SymbolInfo,
    val amount: String,
    val usdEquivalent: String?
) {

    fun fundamentalAmount(): Long {
        val amountAsBigDecimal = amount.toBigDecimalOrNull() ?: throw NumberFormatException()

        return if (symbolInfo.symbol == "SOL") {
            (amountAsBigDecimal * 1_000_000_000.toBigDecimal()).toLong()
        } else {
            val parts = amount.split(".")
            val decimals = if (parts.size == 1) 0 else parts[1].count()
            val calculatedAmount = amountAsBigDecimal * (10.toBigDecimal().pow(decimals))
            calculatedAmount.toLong()
        }
    }

    fun formattedAmount(): String {
        val split = amount.split(".").toMutableList()

        val wholePart =
            if (split.isNotEmpty() && split.size > 1) {
                split.removeAt(0)
            } else {
                amount
            }

        val wholePartString = formatSeparator(wholePart.toInt())
        split.add(0, wholePartString)
        return split.joinToString(separator = ".")
    }

    fun formattedUSDEquivalent(): String {
        if (usdEquivalent == null) {
            return ""
        }

        val decimal = usdEquivalent.toBigDecimal()
        val usdEquivalent = usdFormatter().format(decimal)
        return usdEquivalent
    }

    private fun formatSeparator(number: Int): String {
        return String.format("%,d", number)
    }

    private fun usdFormatter(): DecimalFormat {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US) as DecimalFormat
        val symbols: DecimalFormatSymbols = formatter.decimalFormatSymbols
        symbols.currencySymbol = ""
        formatter.decimalFormatSymbols = symbols
        return formatter
    }
}

data class SymbolInfo(
    val symbol: String,
    val symbolDescription: String,
    val tokenMintAddress: String
)

data class DestinationAddress(
    val name: String,
    val subName: String?,
    val address: String,
    val tag: String?
)

data class SlotSignerInfo(
    val slotId: Byte,
    val value: SignerInfo
)

data class SignerInfo(
    val publicKey: String,
    val name: String,
    val email: String
)

enum class SlotUpdateType(val value: String) {
    @SerializedName("SetIfEmpty")  SetIfEmpty("SetIfEmpty"),
    @SerializedName("Clear") Clear("Clear");

    fun toSolanaProgramValue() : Byte =
        when (this) {
            SetIfEmpty -> 0
            Clear -> 1
        }
}

enum class BooleanSetting(val value: String) {
    @SerializedName("Off")  Off("Off"),
    @SerializedName("On") On("On");

    fun toSolanaProgramValue() : Byte =
        when (this) {
            Off -> 0
            On -> 1
        }
}

data class SolanaDApp(
    val address: String,
    val name: String,
    val logo: String
)

data class SolanaInstructionBatch(
    val from: Byte,
    val instructions: List<SolanaInstruction>) {

    fun combinedBytes(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(28))
        buffer.write(byteArrayOf(from))
        buffer.writeShortLE(instructions.size.toShort())
        buffer.write(instructions.flatMap { it.combinedBytes().toList() }.toByteArray())

        return buffer.toByteArray()
    }
}

data class SolanaInstruction(
    val programId: String,
    val accountMetas: List<SolanaAccountMeta>,
    val data: String
) {
    fun combinedBytes(): ByteArray {
        val b64DecodedData: ByteArray =
            if (data.isEmpty()) byteArrayOf() else BaseWrapper.decodeFromBase64(data)

        val buffer = ByteArrayOutputStream()
        buffer.write(programId.base58Bytes())
        buffer.writeShortLE(accountMetas.size.toShort())
        buffer.write(accountMetas.flatMap { it.combinedBytes().toList() }.toByteArray())
        buffer.writeShortLE(b64DecodedData.size.toShort())
        buffer.write(b64DecodedData)

        return byteArrayOf()
    }
}

data class SolanaAccountMeta(
    val address: String,
    val signer: Boolean,
    val writeable: Boolean
) {
    fun combinedBytes(): ByteArray {
        return byteArrayOf(flags()) + address.base58Bytes()
    }

    private fun flags(): Byte {
        val writeValue = if (writeable) 1 else 0
        val signValue = if (signer) 2 else 0
        return (writeValue + signValue).toByte()
    }
}