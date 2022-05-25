package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog
import org.web3j.abi.datatypes.Bool
import java.io.BufferedOutputStream
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
        var approvalPolicy: ApprovalPolicy,
        var whitelistEnabled: BooleanSetting,
        var dappsEnabled: BooleanSetting,
        var addressBookSlot: Byte,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(byteArrayOf(accountSlot))
            buffer.write(accountInfo.name.sha256HashBytes())
            buffer.write(approvalPolicy.combinedBytes())
            buffer.write(byteArrayOf(whitelistEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(dappsEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(addressBookSlot))

            return buffer.toByteArray()
        }
    }

    data class DAppTransactionRequest(
        val type: String,
        var account: AccountInfo,
        var dappInfo: SolanaDApp,
        var balanceChanges: List<SymbolAndAmountInfo>,
        var instructions: List<SolanaInstructionBatch>,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class WrapConversionRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destinationSymbolInfo: SymbolInfo,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class WalletConfigPolicyUpdate(
        val type: String,
        val approvalPolicy: ApprovalPolicy,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType()

    data class BalanceAccountSettingsUpdate(
        val type: String,
        val account: AccountInfo,
        val whitelistEnabled: BooleanSetting?,
        val dappsEnabled: BooleanSetting?,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        val change: SettingsChange

        init {
            if (whitelistEnabled != null && dappsEnabled == null) {
                change = SettingsChange.WhitelistEnabled(whiteListEnabled = whitelistEnabled == BooleanSetting.On)
            } else if (dappsEnabled != null && whitelistEnabled == null) {
                change = SettingsChange.DAppsEnabled(dappsEnabled = dappsEnabled == BooleanSetting.On)
            } else {
                throw Exception("Only one setting should be changed")
            }
        }

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()
            buffer.write(account.identifier.sha256HashBytes())

            when (change) {
                is SettingsChange.WhitelistEnabled -> {
                    buffer.write(byteArrayOf(1.toByte()))
                    val programValue =
                        if (change.whiteListEnabled) BooleanSetting.On.toSolanaProgramValue() else BooleanSetting.Off.toSolanaProgramValue()
                    buffer.write(byteArrayOf(programValue))
                    buffer.write(byteArrayOf(0.toByte()))
                    buffer.write(byteArrayOf(0.toByte()))
                }
                is SettingsChange.DAppsEnabled -> {
                    buffer.write(byteArrayOf(0.toByte()))
                    buffer.write(byteArrayOf(0.toByte()))
                    buffer.write(byteArrayOf(1.toByte()))
                    val programValue =
                        if (change.dappsEnabled) BooleanSetting.On.toSolanaProgramValue() else BooleanSetting.Off.toSolanaProgramValue()
                    buffer.write(byteArrayOf(programValue))
                }
            }

            return buffer.toByteArray()
        }
    }

    data class DAppBookUpdate(
        val type: String,
        val entriesToAdd: List<SlotDAppInfo>,
        val entriesToRemove: List<SlotDAppInfo>,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(entriesToAdd.size.toByte()))
            buffer.write(entriesToAdd.flatMap { it.combinedBytes().toList() }.toByteArray())
            buffer.write(byteArrayOf(entriesToRemove.size.toByte()))
            buffer.write(entriesToRemove.flatMap { it.combinedBytes().toList() }.toByteArray())

            return buffer.toByteArray()
        }
    }

    data class AddressBookUpdate(
        val type: String,
        val entriesToAdd: List<SlotDestinationInfo>,
        val entriesToRemove: List<SlotDestinationInfo>,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        val change : AddRemoveChange
        val entry: SlotDestinationInfo

        init {
            if(entriesToAdd.size == 1 && entriesToRemove.isEmpty()) {
                change = AddRemoveChange.ADD
                entry = entriesToAdd[0]
            } else if (entriesToAdd.isEmpty() && entriesToRemove.size == 1) {
                change = AddRemoveChange.REMOVE
                entry = entriesToRemove[0]
            } else {
                throw Exception("Only 1 entry is accepted for either added or removed")
            }
        }
        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            when (change) {
                AddRemoveChange.ADD -> {
                    buffer.write(byteArrayOf(1.toByte()))
                    buffer.write(entry.combinedBytes())
                    buffer.write(byteArrayOf(0.toByte()))
                    buffer.write(byteArrayOf(0.toByte()))

                }
                AddRemoveChange.REMOVE -> {
                    buffer.write(byteArrayOf(0.toByte()))
                    buffer.write(byteArrayOf(1.toByte()))
                    buffer.write(entry.combinedBytes())
                    buffer.write(byteArrayOf(0.toByte()))
                }
            }

            return buffer.toByteArray()
        }
    }

    enum class AddRemoveChange {
        ADD, REMOVE
    }

    sealed class SettingsChange(val value: Boolean) {
        data class WhitelistEnabled(val whiteListEnabled: Boolean) : SettingsChange(value = whiteListEnabled)
        data class DAppsEnabled(val dappsEnabled: Boolean) : SettingsChange(value = dappsEnabled)
    }

    data class BalanceAccountNameUpdate(
        val type: String,
        val accountInfo: AccountInfo,
        val newAccountName: String,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(newAccountName.sha256HashBytes())

            return buffer.toByteArray()
        }
    }

    data class BalanceAccountPolicyUpdate(
        val type: String,
        val accountInfo: AccountInfo,
        val approvalPolicy: ApprovalPolicy,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(approvalPolicy.combinedBytes())

            return buffer.toByteArray()
        }

    }

    data class SPLTokenAccountCreation(
        val type: String,
        val payerBalanceAccount: AccountInfo,
        val balanceAccounts: List<AccountInfo>,
        val tokenSymbolInfo: SymbolInfo,
        var signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(payerBalanceAccount.identifier.sha256HashBytes())
            buffer.write(byteArrayOf(balanceAccounts.size.toByte()))
            buffer.write(balanceAccounts.flatMap { it.identifier.sha256HashBytes().toList() }.toByteArray())
            buffer.write(tokenSymbolInfo.tokenMintAddress.base58Bytes())

            return buffer.toByteArray()
        }
    }

    data class BalanceAccountAddressWhitelistUpdate(
        val type: String,
        val accountInfo: AccountInfo,
        val destinations: List<SlotDestinationInfo>,
        val signingData: SolanaSigningData
    ) : SolanaApprovalRequestType() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(byteArrayOf(destinations.size.toByte()))
            buffer.write(destinations.map { it.slotId }.toByteArray())
            buffer.write(destinationsData())

            return buffer.toByteArray()
        }

        private fun destinationsData() : ByteArray {
            val destinationsData = destinations.flatMap {
                it.value.name.sha256HashBytes().toList()
            }.toByteArray()

            return destinationsData.sha256HashBytes()
        }
    }

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

    fun nonceAccountAddresses() : List<String> {
        return when(this) {
            is WithdrawalRequest -> signingData.nonceAccountAddresses
            is ConversionRequest -> signingData.nonceAccountAddresses
            is SignersUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountCreation -> signingData.nonceAccountAddresses
            is DAppTransactionRequest -> signingData.nonceAccountAddresses
            is WrapConversionRequest -> signingData.nonceAccountAddresses
            is WalletConfigPolicyUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountSettingsUpdate -> signingData.nonceAccountAddresses
            is DAppBookUpdate -> signingData.nonceAccountAddresses
            is AddressBookUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountNameUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountPolicyUpdate -> signingData.nonceAccountAddresses
            is SPLTokenAccountCreation -> signingData.nonceAccountAddresses
            is BalanceAccountAddressWhitelistUpdate -> signingData.nonceAccountAddresses
            is LoginApprovalRequest, UnknownApprovalType -> emptyList()
        }
    }
}

enum class ApprovalType(val value: String) {
    WITHDRAWAL_TYPE("WithdrawalRequest"),
    CONVERSION_REQUEST_TYPE("ConversionRequest"),
    SIGNERS_UPDATE_TYPE("SignersUpdate"),
    BALANCE_ACCOUNT_CREATION_TYPE("BalanceAccountCreation"),
    DAPP_TRANSACTION_REQUEST_TYPE("DAppTransactionRequest"),
    LOGIN_TYPE("LoginApproval"),
    WRAP_CONVERSION_REQUEST_TYPE("WrapConversionRequest"),
    BALANCE_ACCOUNT_NAME_UPDATE_TYPE("BalanceAccountNameUpdate"),
    BALANCE_ACCOUNT_POLICY_UPDATE_TYPE("BalanceAccountPolicyUpdate"),
    BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE("BalanceAccountSettingsUpdate"),
    ADDRESS_BOOK_TYPE("AddressBookUpdate"),
    DAPP_BOOK_UPDATE_TYPE("DAppBookUpdate"),
    WALLET_CONFIG_POLICY_UPDATE_TYPE("WalletConfigPolicyUpdate"),
    SPL_TOKEN_ACCOUNT_CREATION_TYPE("SPLTokenAccountCreation"),
    BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE("BalanceAccountAddressWhitelistUpdate"),

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
                WRAP_CONVERSION_REQUEST_TYPE.value -> WRAP_CONVERSION_REQUEST_TYPE
                BALANCE_ACCOUNT_NAME_UPDATE_TYPE.value -> BALANCE_ACCOUNT_NAME_UPDATE_TYPE
                BALANCE_ACCOUNT_POLICY_UPDATE_TYPE.value -> BALANCE_ACCOUNT_POLICY_UPDATE_TYPE
                BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE.value -> BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE
                ADDRESS_BOOK_TYPE.value -> ADDRESS_BOOK_TYPE
                DAPP_BOOK_UPDATE_TYPE.value -> DAPP_BOOK_UPDATE_TYPE
                WALLET_CONFIG_POLICY_UPDATE_TYPE.value -> WALLET_CONFIG_POLICY_UPDATE_TYPE
                SPL_TOKEN_ACCOUNT_CREATION_TYPE.value -> SPL_TOKEN_ACCOUNT_CREATION_TYPE
                BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE.value -> BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE
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

data class ApprovalPolicy(
    val approvalsRequired: Byte,
    val approvalTimeout: Long,
    val approvers: List<SlotSignerInfo>
) {
    fun combinedBytes(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(approvalsRequired))
        buffer.writeLongLE(approvalTimeout.convertToSeconds())
        buffer.write(byteArrayOf(approvers.size.toByte()))
        buffer.write(approvers.map { it.slotId }.toByteArray())
        buffer.write(
            approvers.flatMap { it.value.publicKey.base58Bytes().toList() }.toByteArray().sha256HashBytes()
        )

        return buffer.toByteArray()
    }
}

data class SlotDestinationInfo(
    val slotId: Byte,
    val value: DestinationAddress
) {

    fun combinedBytes() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.write(byteArrayOf(slotId))
        buffer.write(value.address.base58Bytes())
        buffer.write(value.name.sha256HashBytes())

        return buffer.toByteArray()
    }
}

data class SlotDAppInfo(
    val slotId: Byte,
    val value: SolanaDApp
) {

    fun combinedBytes() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.write(byteArrayOf(slotId))
        buffer.write(value.address.base58Bytes())
        buffer.write(value.name.sha256HashBytes())

        return buffer.toByteArray()
    }

}

data class WhitelistUpdate(
    val account: AccountInfo,
    val destinationsToAdd: List<SlotDestinationInfo>,
    val destinationsToRemove: List<SlotDestinationInfo>,
) {
    fun combinedBytes() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.write(account.identifier.sha256HashBytes())
        buffer.write(byteArrayOf(destinationsToAdd.size.toByte()))
        buffer.write(destinationsToAdd.map { it.slotId }.toByteArray())
        buffer.write(byteArrayOf(destinationsToRemove.size.toByte()))
        buffer.write(destinationsToRemove.map { it.slotId }.toByteArray())
        buffer.write(bothDestinationsData().sha256HashBytes())

        return buffer.toByteArray()
    }

    fun bothDestinationsData() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.write(destinationsToAdd.flatMap { it.value.name.sha256HashBytes().toList() }.toByteArray())
        buffer.write(byteArrayOf(1.toByte()))
        buffer.write(destinationsToRemove.flatMap { it.value.name.sha256HashBytes().toList() }.toByteArray())

        return buffer.toByteArray()
    }
}

data class SolanaSigningData(
    val feePayer: String,
    val walletProgramId: String,
    val multisigOpAccountAddress: String,
    val walletAddress: String,
    val nonceAccountAddresses: List<String>,
    val initiator: String
) {
    fun commonOpHashBytes() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.write(initiator.base58Bytes())
        buffer.write(feePayer.base58Bytes())
        buffer.writeLongLE(0)
        buffer.write(ByteArray(size = 32))

        return buffer.toByteArray()
    }

    fun commonInitiationBytes() : ByteArray {
        val buffer = ByteArrayOutputStream()

        buffer.writeLongLE(0)
        buffer.write(byteArrayOf(0))
        buffer.write(ByteArray(size = 32))

        return buffer.toByteArray()
    }
}

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

    fun formattedUSDEquivalent(hideSymbol: Boolean = true): String {
        if (usdEquivalent == null) {
            return ""
        }

        val decimal = usdEquivalent.toBigDecimal()
        val usdEquivalent = usdFormatter(hideSymbol).format(decimal)
        return usdEquivalent
    }

    private fun formatSeparator(number: Int): String {
        return String.format("%,d", number)
    }

    private fun usdFormatter(hideSymbol: Boolean = true): DecimalFormat {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US) as DecimalFormat
        if (hideSymbol) {
            val symbols: DecimalFormatSymbols = formatter.decimalFormatSymbols
            symbols.currencySymbol = ""
            formatter.decimalFormatSymbols = symbols
        }
        return formatter
    }

    fun isAmountPositive(): Boolean {
        return try {
            val amountAsDecimal = amount.toBigDecimal()

            amountAsDecimal >= 0.toBigDecimal()
        } catch (e: Exception) {
            try {
                !amount.contains("-")
            } catch (innerException: Exception) {
                true
            }
        }
    }
}

data class SymbolInfo(
    val symbol: String,
    val symbolDescription: String,
    val tokenMintAddress: String
) {
    fun getSOLProgramValue() : Byte = if (symbol == "SOL") 0 else 1
}

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
            SetIfEmpty -> 0.toByte()
            Clear -> 1.toByte()
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

        return buffer.toByteArray()
    }
}

data class SolanaAccountMeta(
    val address: String,
    val signer: Boolean,
    val writable: Boolean
) {
    fun combinedBytes(): ByteArray {
        return byteArrayOf(flags()) + address.base58Bytes()
    }

    private fun flags(): Byte {
        val writeValue : Byte = if (writable) 1 else 0
        val signValue : Byte = if (signer) 2 else 0
        return (writeValue + signValue).toByte()
    }
}