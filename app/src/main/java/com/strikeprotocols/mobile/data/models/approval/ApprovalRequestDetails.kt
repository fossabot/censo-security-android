package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import com.strikeprotocols.mobile.data.models.Chain
import java.io.ByteArrayOutputStream
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

sealed class SolanaApprovalRequestDetails {

    data class ApprovalRequestDetails(val requestType: com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails) :
        SolanaApprovalRequestDetails()

    data class MultiSignOpInitiationDetails(
        val multisigOpInitiation: MultiSigOpInitiation,
        val requestType: com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails
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
        const val DATA_JSON_KEY = "data"
    }
}

data class MultiSigOpInitiation(
    val opAccountCreationInfo: MultiSigAccountCreationInfo,
    val initiatorIsApprover: Boolean
) {
    companion object {
        const val MULTI_SIG_TYPE = "MultisigOpInitiation"
    }
}

sealed class ApprovalRequestDetails {
    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapterFactory(TypeFactorySettings.signingDataAdapterFactory)
            .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
            .create()
            .toJson(this)

    data class WithdrawalRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val signingData: SigningData
    ) : ApprovalRequestDetails()

    data class ConversionRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destination: DestinationAddress,
        val destinationSymbolInfo: SymbolInfo,
        val signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails()

    data class SignersUpdate(
        val type: String,
        val slotUpdateType: SlotUpdateType,
        val signer: SlotSignerInfo,
        val signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails()

    data class WalletCreation(
        val type: String,
        var accountSlot: Byte,
        var accountInfo: AccountInfo,
        var approvalPolicy: ApprovalPolicy,
        var whitelistEnabled: BooleanSetting,
        var dappsEnabled: BooleanSetting,
        var addressBookSlot: Byte,
        var signingData: SigningData.SolanaSigningData?
    ) : ApprovalRequestDetails() {

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
        var instructions: List<SolanaInstructionChunk>,
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails()

    data class WrapConversionRequest(
        val type: String,
        val account: AccountInfo,
        val symbolAndAmountInfo: SymbolAndAmountInfo,
        val destinationSymbolInfo: SymbolInfo,
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails()

    data class WalletConfigPolicyUpdate(
        val type: String,
        val approvalPolicy: ApprovalPolicy,
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails()

    data class BalanceAccountSettingsUpdate(
        val type: String,
        val account: AccountInfo,
        val whitelistEnabled: BooleanSetting?,
        val dappsEnabled: BooleanSetting?,
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails() {

        fun changeValue() : SettingsChange? {
            return if (whitelistEnabled != null && dappsEnabled == null) {
                SettingsChange.WhitelistEnabled(whiteListEnabled = whitelistEnabled == BooleanSetting.On)
            } else if (dappsEnabled != null && whitelistEnabled == null) {
                SettingsChange.DAppsEnabled(dappsEnabled = dappsEnabled == BooleanSetting.On)
            } else {
                null
            }
        }

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()
            buffer.write(account.identifier.sha256HashBytes())

            val change : SettingsChange = changeValue() ?: throw Exception("Only one setting should be changed")

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
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(entriesToAdd.size.toByte()))
            buffer.write(entriesToAdd.flatMap { it.combinedBytes().toList() }.toByteArray())
            buffer.write(byteArrayOf(entriesToRemove.size.toByte()))
            buffer.write(entriesToRemove.flatMap { it.combinedBytes().toList() }.toByteArray())

            return buffer.toByteArray()
        }
    }

    data class CreateAddressBookEntry(
        val type: String,
        val chain: Chain,
        val slotId: Byte,
        val address: String,
        val name: String,
        var signingData: SigningData.SolanaSigningData?
    ) : ApprovalRequestDetails() {
        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()
            buffer.write(byteArrayOf(1.toByte()))
            buffer.write(byteArrayOf(slotId))
            buffer.write(address.base58Bytes())
            buffer.write(name.sha256HashBytes())
            buffer.write(byteArrayOf(0.toByte()))
            buffer.write(byteArrayOf(0.toByte()))
            return buffer.toByteArray()
        }
    }

    data class DeleteAddressBookEntry(
        val type: String,
        val chain: Chain,
        val slotId: Byte,
        val address: String,
        val name: String,
        var signingData: SigningData.SolanaSigningData?
    ) : ApprovalRequestDetails() {
        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()
            buffer.write(byteArrayOf(0.toByte()))
            buffer.write(byteArrayOf(1.toByte()))
            buffer.write(byteArrayOf(slotId))
            buffer.write(address.base58Bytes())
            buffer.write(name.sha256HashBytes())
            buffer.write(byteArrayOf(0.toByte()))
            return buffer.toByteArray()
        }
    }

    open class SettingsChange() {
        data class WhitelistEnabled(val whiteListEnabled: Boolean) : SettingsChange()
        data class DAppsEnabled(val dappsEnabled: Boolean) : SettingsChange()
    }

    data class BalanceAccountNameUpdate(
        val type: String,
        val accountInfo: AccountInfo,
        val newAccountName: String,
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails() {

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
        var signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails() {

        fun combinedBytes() : ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(accountInfo.identifier.sha256HashBytes())
            buffer.write(approvalPolicy.combinedBytes())

            return buffer.toByteArray()
        }

    }

    data class BalanceAccountAddressWhitelistUpdate(
        val type: String,
        val accountInfo: AccountInfo,
        val destinations: List<SlotDestinationInfo>,
        val signingData: SigningData.SolanaSigningData
    ) : ApprovalRequestDetails() {

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
        val jwtToken: String,
        val email: String?,
        val name: String?
    ) : ApprovalRequestDetails()

    data class AcceptVaultInvitation(
        val type: String,
        val vaultGuid: String,
        val vaultName: String
    ) : ApprovalRequestDetails()

    data class PasswordReset(
        val type: String
    ) : ApprovalRequestDetails()

    data class SignData(
        val base64Data: String,
        val signingData: SigningData.SolanaSigningData,
    ) : ApprovalRequestDetails()

    object UnknownApprovalType : ApprovalRequestDetails()

    companion object {
        const val INVALID_REQUEST_APPROVAL = "Invalid request for Approval"
        const val UNKNOWN_REQUEST_APPROVAL = "Unknown Approval"
        const val UNKNOWN_INITIATION = "Unknown Initiation"
    }

    fun nonceAccountAddresses() : List<String> {
        return when(this) {
            is WithdrawalRequest ->  when(signingData()) {
                is SigningData.SolanaSigningData -> (signingData as SigningData.SolanaSigningData).nonceAccountAddresses
                else -> emptyList()
            }
            is ConversionRequest -> signingData.nonceAccountAddresses
            is SignersUpdate -> signingData.nonceAccountAddresses
            is WalletCreation -> signingData?.nonceAccountAddresses ?: emptyList()
            is DAppTransactionRequest -> signingData.nonceAccountAddresses
            is WrapConversionRequest -> signingData.nonceAccountAddresses
            is WalletConfigPolicyUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountSettingsUpdate -> signingData.nonceAccountAddresses
            is DAppBookUpdate -> signingData.nonceAccountAddresses
            is CreateAddressBookEntry -> signingData?.nonceAccountAddresses ?: emptyList()
            is DeleteAddressBookEntry -> signingData?.nonceAccountAddresses ?: emptyList()
            is BalanceAccountNameUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountPolicyUpdate -> signingData.nonceAccountAddresses
            is BalanceAccountAddressWhitelistUpdate -> signingData.nonceAccountAddresses
            is SignData -> signingData.nonceAccountAddresses
            is LoginApprovalRequest, is UnknownApprovalType, is AcceptVaultInvitation, is PasswordReset -> emptyList()
        }
    }

    fun nonceAccountAddressesSlot() : Int {
        return when(this) {
            is WithdrawalRequest, is ConversionRequest, is SignersUpdate,
            is WalletCreation, is DAppTransactionRequest, is WrapConversionRequest,
            is WalletConfigPolicyUpdate, is BalanceAccountSettingsUpdate, is DAppBookUpdate,
            is CreateAddressBookEntry, is DeleteAddressBookEntry,
            is BalanceAccountNameUpdate, is BalanceAccountPolicyUpdate,
            is BalanceAccountAddressWhitelistUpdate, is SignData -> {
                when (val signingData = signingData()) {
                    is SigningData.SolanaSigningData -> signingData.nonceAccountAddressesSlot
                    else -> 0
                }
            }
            is LoginApprovalRequest, is UnknownApprovalType,
            is AcceptVaultInvitation, is PasswordReset -> 0
        }
    }

    private fun signingData(): SigningData? =
        when (this) {
            is WithdrawalRequest -> signingData
            is ConversionRequest -> signingData
            is SignersUpdate -> signingData
            is WalletCreation -> signingData
            is DAppTransactionRequest -> signingData
            is WrapConversionRequest -> signingData
            is WalletConfigPolicyUpdate -> signingData
            is BalanceAccountSettingsUpdate -> signingData
            is DAppBookUpdate -> signingData
            is CreateAddressBookEntry -> signingData
            is DeleteAddressBookEntry -> signingData
            is BalanceAccountNameUpdate -> signingData
            is BalanceAccountPolicyUpdate -> signingData
            is BalanceAccountAddressWhitelistUpdate -> signingData
            is SignData -> signingData
            is LoginApprovalRequest, is UnknownApprovalType,
            is AcceptVaultInvitation, is PasswordReset -> null
        }

}

enum class ApprovalType(val value: String) {
    WITHDRAWAL_TYPE("WithdrawalRequest"),
    CONVERSION_REQUEST_TYPE("ConversionRequest"),
    SIGNERS_UPDATE_TYPE("SignersUpdate"),
    WALLET_CREATION_TYPE("WalletCreation"),
    DAPP_TRANSACTION_REQUEST_TYPE("DAppTransactionRequest"),
    LOGIN_TYPE("LoginApproval"),
    WRAP_CONVERSION_REQUEST_TYPE("WrapConversionRequest"),
    BALANCE_ACCOUNT_NAME_UPDATE_TYPE("BalanceAccountNameUpdate"),
    BALANCE_ACCOUNT_POLICY_UPDATE_TYPE("BalanceAccountPolicyUpdate"),
    BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE("BalanceAccountSettingsUpdate"),
    CREATE_ADDRESS_BOOK_ENTRY_TYPE("CreateAddressBookEntry"),
    DELETE_ADDRESS_BOOK_ENTRY_TYPE("DeleteAddressBookEntry"),
    DAPP_BOOK_UPDATE_TYPE("DAppBookUpdate"),
    WALLET_CONFIG_POLICY_UPDATE_TYPE("WalletConfigPolicyUpdate"),
    BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE("BalanceAccountAddressWhitelistUpdate"),
    ACCEPT_VAULT_INVITATION_TYPE("AcceptVaultInvitation"),
    PASSWORD_RESET_TYPE("PasswordReset"),
    SIGN_DATA_TYPE("SignData"),

    UNKNOWN_TYPE("");

    companion object {
        fun fromString(type: String?): ApprovalType =
            when (type) {
                WITHDRAWAL_TYPE.value -> WITHDRAWAL_TYPE
                CONVERSION_REQUEST_TYPE.value -> CONVERSION_REQUEST_TYPE
                SIGNERS_UPDATE_TYPE.value -> SIGNERS_UPDATE_TYPE
                WALLET_CREATION_TYPE.value -> WALLET_CREATION_TYPE
                DAPP_TRANSACTION_REQUEST_TYPE.value -> DAPP_TRANSACTION_REQUEST_TYPE
                LOGIN_TYPE.value -> LOGIN_TYPE
                WRAP_CONVERSION_REQUEST_TYPE.value -> WRAP_CONVERSION_REQUEST_TYPE
                BALANCE_ACCOUNT_NAME_UPDATE_TYPE.value -> BALANCE_ACCOUNT_NAME_UPDATE_TYPE
                BALANCE_ACCOUNT_POLICY_UPDATE_TYPE.value -> BALANCE_ACCOUNT_POLICY_UPDATE_TYPE
                BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE.value -> BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE
                CREATE_ADDRESS_BOOK_ENTRY_TYPE.value -> CREATE_ADDRESS_BOOK_ENTRY_TYPE
                DELETE_ADDRESS_BOOK_ENTRY_TYPE.value -> DELETE_ADDRESS_BOOK_ENTRY_TYPE
                DAPP_BOOK_UPDATE_TYPE.value -> DAPP_BOOK_UPDATE_TYPE
                WALLET_CONFIG_POLICY_UPDATE_TYPE.value -> WALLET_CONFIG_POLICY_UPDATE_TYPE
                BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE.value -> BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE
                ACCEPT_VAULT_INVITATION_TYPE.value -> ACCEPT_VAULT_INVITATION_TYPE
                PASSWORD_RESET_TYPE.value -> PASSWORD_RESET_TYPE
                SIGN_DATA_TYPE.value -> SIGN_DATA_TYPE
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
    val address: String?,
    val chain: Chain? = null
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

val b64Encoder: Base64.Encoder = Base64.getEncoder()
val b64Decoder: Base64.Decoder = Base64.getDecoder()
val emptyHash: String = b64Encoder.encodeToString(ByteArray(size = 32))

data class TransactionInput(
    val txId: String,
    val index: Int,
    val amount: Long,
    val inputScriptHex: String,
    val base64HashForSignature: String,
)

data class TransactionOutput(
    val index: Int,
    val amount: Long,
    val pubKeyScriptHex: String,
    val address: String,
    val isChange: Boolean
)

data class BitcoinTransaction(
    val version: Int,
    val txIns: List<TransactionInput>,
    val txOuts: List<TransactionOutput>,
    val totalFee: Long,
)

data class EthereumTransaction(
    val chainId: Long,
    val safeNonce: Long,
)

sealed class SigningData {
    data class SolanaSigningData(
        val feePayer: String,
        val walletProgramId: String,
        val multisigOpAccountAddress: String,
        val walletAddress: String,
        val nonceAccountAddresses: List<String>,
        val nonceAccountAddressesSlot: Int,
        val initiator: String,
        val strikeFeeAmount: Long,
        val feeAccountGuidHash: String,
        val walletGuidHash: String,
        val base64DataToSign: String? = null,
    ) : SigningData() {

        fun commonOpHashBytes(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.write(initiator.base58Bytes())
            buffer.write(feePayer.base58Bytes())
            buffer.writeLongLE(strikeFeeAmount)
            buffer.write(b64Decoder.decode(feeAccountGuidHash))
            buffer.write(walletAddress.base58Bytes())

            return buffer.toByteArray()
        }

        fun commonInitiationBytes(): ByteArray {
            val buffer = ByteArrayOutputStream()

            buffer.writeLongLE(strikeFeeAmount)
            buffer.write(
                byteArrayOf(
                    if (feeAccountGuidHash == emptyHash) 0 else 1
                )
            )
            buffer.write(b64Decoder.decode(feeAccountGuidHash))

            return buffer.toByteArray()
        }
    }

    data class BitcoinSigningData(
        val childKeyIndex: Int,
        val transaction: BitcoinTransaction
    ) : SigningData()

    data class EthereumSigningData(
        val transaction: EthereumTransaction
    ) : SigningData()
}

enum class AccountType(val value: String) {
    @SerializedName("BalanceAccount") BalanceAccount("BalanceAccount"),
    @SerializedName("StakeAccount") StakeAccount("StakeAccount")
}

data class SymbolAndAmountInfo(
    val symbolInfo: SymbolInfo,
    val amount: String,
    val usdEquivalent: String?,
    val fee: Fee? = null,
    val replacementFee: Fee? = null,
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

    fun fundamentalAmountAsBigInteger(): BigInteger {
        return BigInteger(amount.replace(".", ""), 10)
    }

    fun formattedAmount(): String = formattedAmount(amount)

    fun formattedAmountWithSymbol(): String =
        "${formattedAmount(amount)} ${symbolInfo.symbol}"

    fun formattedUSDEquivalent(hideSymbol: Boolean = true): String {
        if (usdEquivalent == null) {
            return ""
        }

        val decimal = usdEquivalent.toBigDecimal()
        val usdEquivalent = usdFormatter(hideSymbol).format(decimal)
        return usdEquivalent
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
                !amount.startsWith("-")
            } catch (innerException: Exception) {
                true
            }
        }
    }
}

fun formattedAmount(amount: String): String {
    fun formatSeparator(number: Int): String {
        return String.format("%,d", number)
    }

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

data class Fee(
    val symbolInfo: SymbolInfo,
    val amount: String,
    val usdEquivalent: String? = null,
) {
    fun formattedAmountWithSymbol(): String =
        "${formattedAmount(amount)} ${symbolInfo.symbol}"
}

data class NftMetadata(
    val name: String
)

data class SymbolInfo(
    val symbol: String,
    val symbolDescription: String,
    val tokenMintAddress: String?,
    val imageUrl: String? = null,
    val nftMetadata: NftMetadata? = null
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
    val email: String,
    val nameHashIsEmpty: Boolean
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

data class SolanaInstructionChunk(
    val offset: Short,
    val instructionData: String) {

    fun decodedData(): ByteArray = Base64.getDecoder().decode(instructionData)

    fun combinedBytes(): ByteArray {
        val buffer = ByteArrayOutputStream()
        val decodedData = decodedData()
        buffer.write(byteArrayOf(28))
        buffer.writeShortLE(offset)
        buffer.writeShortLE(decodedData.size.toShort())
        buffer.write(decodedData)

        return buffer.toByteArray()
    }
}