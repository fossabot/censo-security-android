package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.common.BaseWrapper
import java.io.ByteArrayOutputStream
import org.web3j.crypto.Hash
import java.lang.NumberFormatException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

sealed class SolanaApprovalRequestDetails {
    data class ApprovalRequestDetails(val requestType: SolanaApprovalRequestType) :
        SolanaApprovalRequestDetails()

    data class MultiSignOpInitiationDetails(
        val multisigOpInitiation: MultiSigOpInitiation,
        val requestType: SolanaApprovalRequestType
    ) : SolanaApprovalRequestDetails()

    companion object {
        fun getTypeAndDetailsFromJson(json: JsonElement?): ApprovalTypeMetaData {

            if (json == null || json !is JsonObject || !json.asJsonObject.has(ApprovalTypeMetaData.Companion.DETAILS_KEY)) {
                return ApprovalTypeMetaData(type = "", details = null)
            }

            val detailsData : JsonElement? =
                json.asJsonObject?.get(ApprovalTypeMetaData.Companion.DETAILS_KEY)

            val details : JsonObject? =
                if (detailsData is JsonObject) detailsData.asJsonObject else null

            val type : String =
                if (details != null &&
                    details.asJsonObject.has(ApprovalTypeMetaData.Companion.TYPE_KEY)) {
                    val typeData =
                        details.asJsonObject?.get(ApprovalTypeMetaData.Companion.TYPE_KEY)
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
        const val DETAILS_KEY = "details"
        const val TYPE_KEY = "type"
    }
}

data class MultiSigOpInitiation(
    val opAccountCreationInfo: OpAccountCreationInfo,
    val dataAccountCreationInfo: DataAccountCreationInfo?,
) {
    companion object {
        const val MULTI_SIG_KEY = "MultisigOpInitiation"
    }
}

sealed class SolanaApprovalRequestType {
    abstract fun serialize(): ByteArray

    data class WithdrawalRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {
        override fun serialize(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(signingData.walletAddress.base58Bytes())
            buffer.write(account.identifier.sha256HashBytes())
            buffer.write(destination.address.base58Bytes())
            buffer.write(symbolAndAmountInfo.fundamentalAmount().bytes())
            buffer.write(symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

            return buffer.toByteArray()
        }
    }

    data class ConversionRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val destinationSymbolInfo: SymbolInfo,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {
        override fun serialize(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(account.identifier.sha256HashBytes())
            buffer.write(destination.address.base58Bytes())
            buffer.write(symbolAndAmountInfo.fundamentalAmount().bytes())
            buffer.write(symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

            return buffer.toByteArray()
        }
    }

    data class SignersUpdate(
        val type: String,
        val slotUpdateType: SlotUpdateType,
        val signer: SlotSignerInfo,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {
        override fun serialize(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(signingData.walletAddress.base58Bytes())
            buffer.write(byteArrayOf(slotUpdateType.toSolanaProgramValue()))
            buffer.write(signer.combinedBytes())

            return buffer.toByteArray()

        }
    }

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
    ) : SolanaApprovalRequestType() {
        override fun serialize(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(signingData.walletAddress.base58Bytes())
            buffer.write(byteArrayOf(accountSlot))
            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(byteArrayOf(approvalsRequired))
            buffer.write(approvalTimeout.convertToSeconds().bytes())
            buffer.write(byteArrayOf(approvers.size.toByte()))
            buffer.write(approvers.flatMap { it.combinedBytes().toList() }.toByteArray())
            buffer.write(byteArrayOf(whitelistEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(dappsEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(addressBookSlot))

            return buffer.toByteArray()
        }
    }

    data class DAppTransactionRequest(
        val type: String,
        var account: AccountInfo,
        var dAppInfo: SolanaDApp,
        var balanceChanges: List<SymbolAndAmountInfo>,
        var instructions: List<SolanaInstructionBatch>,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {
        override fun serialize(): ByteArray {
            throw Exception(INVALID_REQUEST_APPROVAL)
        }
    }

    object UnknownApprovalType : SolanaApprovalRequestType() {
        override fun serialize() : ByteArray {
            throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }
    }

    fun retrieveOpCode(): Byte {
        return when (this) {
            is BalanceAccountCreation -> 1
            is WithdrawalRequest, is ConversionRequest -> 3
            is SignersUpdate -> 5
            else -> 0
        }
    }

    companion object {
        const val INVALID_REQUEST_APPROVAL = "Invalid request for Approval"
        const val UNKNOWN_REQUEST_APPROVAL = "Unknown Approval"
    }
}

enum class ApprovalType(val value: String) {
    WITHDRAWAL_TYPE("WithdrawalRequest"),
    CONVERSION_REQUEST_TYPE("ConversionRequest"),
    SIGNERS_UPDATE_TYPE("SignersUpdate"),
    BALANCE_ACCOUNT_CREATION_TYPE("BalanceAccountCreation"),
    DAPP_TRANSACTION_REQUEST_TYPE("DAppTransactionRequest"),
    UNKNOWN_TYPE("");

    companion object {
        fun fromString(type: String?): ApprovalType =
            when (type) {
                WITHDRAWAL_TYPE.value -> WITHDRAWAL_TYPE
                CONVERSION_REQUEST_TYPE.value -> CONVERSION_REQUEST_TYPE
                SIGNERS_UPDATE_TYPE.value -> SIGNERS_UPDATE_TYPE
                BALANCE_ACCOUNT_CREATION_TYPE.value -> BALANCE_ACCOUNT_CREATION_TYPE
                DAPP_TRANSACTION_REQUEST_TYPE.value -> DAPP_TRANSACTION_REQUEST_TYPE
                else -> UNKNOWN_TYPE
            }
    }
}

data class OpAccountCreationInfo(
    val accountSize: Int,
    val minBalanceForRentExemption: Int
)

data class DataAccountCreationInfo(
    val missing: String?
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

    //todo: need verify this with data coming down. Get some examples.
    fun fundamentalAmount(): Long {
        val amountAsFloat = amount.toFloatOrNull() ?: throw NumberFormatException()

        return if (symbolInfo.symbol == "SOL") {
            (amountAsFloat * 1_000_000_000).toLong()
        } else {
            val parts = amount.split(".")
            val decimals = if (parts.size == 1) 0 else parts[1].count()

            val calculatedAmount = amountAsFloat * (10.toDouble().pow(decimals.toDouble()))
            calculatedAmount.toLong()
        }
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
    val instructions: List<SolanaInstruction>
)

data class SolanaInstruction(
    val programId: String,
    val accountMetas: List<SolanaAccountMeta>,
    val data: String
)

data class SolanaAccountMeta(
    val address: String,
    val signer: Boolean,
    val writeable: Boolean
)