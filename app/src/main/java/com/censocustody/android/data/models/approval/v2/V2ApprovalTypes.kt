package com.censocustody.android.data.models.approval.v2

import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.approval.BooleanSetting
import com.censocustody.android.data.models.approval.DestinationAddress
import com.censocustody.android.data.models.approval.TypeFactorySettings
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import java.math.BigInteger

data class ApprovalRequestV2(
    val id: String,
    val submitDate: String,
    val submitterName: String,
    val submitterEmail: String,
    val approvalTimeoutInSeconds: Long?,
    val numberOfDispositionsRequired: Int = 0,
    val numberOfApprovalsReceived: Int = 0,
    val numberOfDeniesReceived: Int = 0,
    val details: ApprovalRequestDetailsV2,
    val vaultName: String?,
    val initiationOnly: Boolean = false,
)

enum class V2ApprovalType(val value: String) {
    VaultPolicyUpdate("VaultPolicyUpdate"),
    BitcoinWalletCreation("BitcoinWalletCreation"),
    EthereumWalletCreation("EthereumWalletCreation"),
    PolygonWalletCreation("PolygonWalletCreation"),
    EthereumWalletNameUpdate("EthereumWalletNameUpdate"),
    PolygonWalletNameUpdate("PolygonWalletNameUpdate"),
    EthereumWalletWhitelistUpdate("EthereumWalletWhitelistUpdate"),
    PolygonWalletWhitelistUpdate("PolygonWalletWhitelistUpdate"),
    EthereumWalletSettingsUpdate("EthereumWalletSettingsUpdate"),
    PolygonWalletSettingsUpdate("PolygonWalletSettingsUpdate"),
    EthereumTransferPolicyUpdate("EthereumTransferPolicyUpdate"),
    PolygonTransferPolicyUpdate("PolygonTransferPolicyUpdate"),
    CreateAddressBookEntry("CreateAddressBookEntry"),
    DeleteAddressBookEntry("DeleteAddressBookEntry"),
    BitcoinWithdrawalRequest("BitcoinWithdrawalRequest"),
    EthereumWithdrawalRequest("EthereumWithdrawalRequest"),
    PolygonWithdrawalRequest("PolygonWithdrawalRequest"),
    PasswordReset("PasswordReset"),
    Login("Login"),
    VaultInvitation("VaultInvitation"),
    Unknown("");

    companion object {
        fun fromString(type: String?): V2ApprovalType =
            when (type) {
                VaultPolicyUpdate.value -> VaultPolicyUpdate
                BitcoinWalletCreation.value -> BitcoinWalletCreation
                EthereumWalletCreation.value -> EthereumWalletCreation
                PolygonWalletCreation.value -> PolygonWalletCreation
                EthereumWalletNameUpdate.value -> EthereumWalletNameUpdate
                PolygonWalletNameUpdate.value -> PolygonWalletNameUpdate
                EthereumWalletWhitelistUpdate.value -> EthereumWalletWhitelistUpdate
                PolygonWalletWhitelistUpdate.value -> PolygonWalletWhitelistUpdate
                EthereumWalletSettingsUpdate.value -> EthereumWalletSettingsUpdate
                PolygonWalletSettingsUpdate.value -> PolygonWalletSettingsUpdate
                EthereumTransferPolicyUpdate.value -> EthereumTransferPolicyUpdate
                PolygonTransferPolicyUpdate.value -> PolygonTransferPolicyUpdate
                CreateAddressBookEntry.value -> CreateAddressBookEntry
                DeleteAddressBookEntry.value -> DeleteAddressBookEntry
                BitcoinWithdrawalRequest.value -> BitcoinWithdrawalRequest
                EthereumWithdrawalRequest.value -> EthereumWithdrawalRequest
                PolygonWithdrawalRequest.value -> PolygonWithdrawalRequest
                PasswordReset.value -> PasswordReset
                Login.value -> Login
                VaultInvitation.value -> VaultInvitation
                else -> Unknown
            }
    }
}

sealed class ApprovalRequestDetailsV2 {
//    companion object {
//        fun fromBase64EncodedJson(base64EncodedJson: String): ApprovalRequestDetailsV2? =
//            try {
//                jsonMapper.readValue(Utils.base64Decode(base64EncodedJson))
//            } catch (e: RuntimeException) {
//                null
//            }
//    }

    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapterFactory(TypeFactorySettings.signingDataAdapterFactory)
            .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
            .create()
            .toJson(this)

    open fun signedEquals(other: ApprovalRequestDetailsV2): Boolean =
        this == other

    open fun isInitiation(): Boolean = false

    data class VaultPolicyUpdate(
        val approvalPolicy: VaultApprovalPolicy,
        val currentOnChainPolicies: List<OnChainPolicy>,
        val signingData: List<SigningData>
    ) : ApprovalRequestDetailsV2() {

        override fun signedEquals(other: ApprovalRequestDetailsV2): Boolean =
            when (other) {
                is VaultPolicyUpdate -> {
                    // we exclude signing data here because if Solana op is denied before we verify Censo op signature,
                    // the multisigOpMapper would return signing data without nonce accounts as are unassigned
                    this.copy(signingData = emptyList()) == other.copy(signingData = emptyList())
                }
                else -> false
            }
    }

    data class BitcoinWalletCreation(
        val identifier: String,
        val name: String,
        val approvalPolicy: WalletApprovalPolicy
    ) : ApprovalRequestDetailsV2()

    data class EthereumWalletCreation(
        val identifier: String,
        val name: String,
        val approvalPolicy: WalletApprovalPolicy,
        val whitelistEnabled: BooleanSetting,
        val dappsEnabled: BooleanSetting,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo
    ) : ApprovalRequestDetailsV2()

    data class PolygonWalletCreation(
        val identifier: String,
        val name: String,
        val approvalPolicy: WalletApprovalPolicy,
        val whitelistEnabled: BooleanSetting,
        val dappsEnabled: BooleanSetting,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo
    ) : ApprovalRequestDetailsV2()

    data class EthereumWalletNameUpdate(
        val wallet: WalletInfo,
        val newName: String
    ) : ApprovalRequestDetailsV2()

    data class PolygonWalletNameUpdate(
        val wallet: WalletInfo,
        val newName: String
    ) : ApprovalRequestDetailsV2()

    data class EthereumWalletWhitelistUpdate(
        val wallet: WalletInfo,
        val destinations: List<Destination>,
        val currentOnChainWhitelist: List<String>,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val signingData: SigningData.EthereumSigningData
    ) : ApprovalRequestDetailsV2()

    data class PolygonWalletWhitelistUpdate(
        val wallet: WalletInfo,
        val destinations: List<Destination>,
        val currentOnChainWhitelist: List<String>,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val signingData: SigningData.PolygonSigningData
    ) : ApprovalRequestDetailsV2()

    data class EthereumWalletSettingsUpdate(
        val wallet: WalletInfo,
        val whitelistEnabled: BooleanSetting?,
        val dappsEnabled: BooleanSetting?,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val currentGuardAddress: String,
        val signingData: SigningData.EthereumSigningData
    ) : ApprovalRequestDetailsV2()

    data class PolygonWalletSettingsUpdate(
        val wallet: WalletInfo,
        val whitelistEnabled: BooleanSetting?,
        val dappsEnabled: BooleanSetting?,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val currentGuardAddress: String,
        val signingData: SigningData.PolygonSigningData
    ) : ApprovalRequestDetailsV2()

    data class EthereumTransferPolicyUpdate(
        val wallet: WalletInfo,
        val currentOnChainPolicy: OnChainPolicy.Ethereum,
        val approvalPolicy: WalletApprovalPolicy,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val signingData: SigningData.EthereumSigningData
    ) : ApprovalRequestDetailsV2()

    data class PolygonTransferPolicyUpdate(
        val wallet: WalletInfo,
        val currentOnChainPolicy: OnChainPolicy.Polygon,
        val approvalPolicy: WalletApprovalPolicy,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val signingData: SigningData.PolygonSigningData
    ) : ApprovalRequestDetailsV2()

    data class CreateAddressBookEntry(
        val chain: Chain,
        val address: String,
        val name: String
    ) : ApprovalRequestDetailsV2()

    data class DeleteAddressBookEntry(
        val chain: Chain,
        val address: String,
        val name: String
    ) : ApprovalRequestDetailsV2()

    data class BitcoinWithdrawalRequest(
        val wallet: WalletInfo,
        val amount: Amount,
        val symbolInfo: BitcoinSymbolInfo,
        val fee: Amount,
        val replacementFee: Amount?,
        val destination: DestinationAddress,
        val signingData: SigningData.BitcoinSigningData
    ) : ApprovalRequestDetailsV2()

    data class EthereumWithdrawalRequest(
        val wallet: WalletInfo,
        val amount: Amount,
        val symbolInfo: EvmSymbolInfo,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val destination: DestinationAddress,
        val signingData: SigningData.EthereumSigningData
    ) : ApprovalRequestDetailsV2()

    data class PolygonWithdrawalRequest(
        val wallet: WalletInfo,
        val amount: Amount,
        val symbolInfo: EvmSymbolInfo,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val destination: DestinationAddress,
        val signingData: SigningData.PolygonSigningData
    ) : ApprovalRequestDetailsV2()

    data class Login(
        val jwtToken: String,
        val email: String,
        val name: String,
    ) : ApprovalRequestDetailsV2()

    object PasswordReset : ApprovalRequestDetailsV2()

    data class VaultInvitation(
        val vaultGuid: String,
        val vaultName: String,
    ) : ApprovalRequestDetailsV2()

    data class SampleRequest(
        val type: String = "SampleRequest",
        val signingData: SigningData,
    ) : ApprovalRequestDetailsV2()

    data class Signer(
        val name: String,
        val email: String,
        val publicKey: String,
        val nameHashIsEmpty: Boolean,
        val jpegThumbnail: String?
    )

    sealed class OnChainPolicy {
        data class Ethereum(
            val owners: List<String>,
            val threshold: Int
        ) : OnChainPolicy()

        data class Polygon(
            val owners: List<String>,
            val threshold: Int
        ) : OnChainPolicy()
    }

    data class VaultApprovalPolicy(
        val approvalsRequired: Int,
        val approvalTimeout: Long,
        val approvers: List<Slot<VaultSigner>>,
    )

    data class WalletApprovalPolicy(
        val approvalsRequired: Int,
        val approvalTimeout: Long,
        val approvers: List<Slot<Signer>>,
    )

    data class VaultSigner(
        val name: String,
        val email: String,
        val publicKeys: List<ChainPubkey>,
        val nameHashIsEmpty: Boolean,
        val jpegThumbnail: String?
    )

    data class ChainPubkey(
        val chain: Chain,
        val key: String
    )

    data class WalletInfo(
        val identifier: String,
        val name: String,
        val address: String,
    )

    data class NftMetadata(
        val name: String
    )

    data class Amount(
        val value: String,
        val nativeValue: String,
        val usdEquivalent: String? = null
    )

    data class BitcoinSymbolInfo(
        val symbol: String,
        val description: String,
        val imageUrl: String? = null
    )

    data class EvmSymbolInfo(
        val symbol: String,
        val description: String,
        val imageUrl: String? = null,
        val tokenInfo: EvmTokenInfo? = null,
        val nftMetadata: NftMetadata? = null
    )

    data class EvmTokenInfo(val contractAddress: String, val tokenId: String?) {

        companion object {
            fun fromName(name: String): EvmName =
                when (name) {
                    EvmName.ERC20.value -> EvmName.ERC20
                    EvmName.ERC721.value -> EvmName.ERC721
                    EvmName.ERC1155.value -> EvmName.ERC1155
                    else -> throw Exception("Unnamed EVM type")
                }
        }

        enum class EvmName(val value: String) {
            ERC20("ERC20"),
            ERC721("ERC721"),
            ERC1155("ERC1155")
        }
    }

    sealed class SigningData {
        data class BitcoinSigningData(
            val childKeyIndex: Int,
            val transaction: BitcoinMultisigOpTransaction
        ) : SigningData()

        data class EthereumSigningData(
            val transaction: EthereumMultisigOpTransaction
        ) : SigningData()

        data class PolygonSigningData(
            val transaction: EthereumMultisigOpTransaction
        ) : SigningData()

        // to keep swagger happy
        data class SampleSigningData(
            val type: String = "sample"
        ) : SigningData()
    }
}

data class Destination(
    val address: String,
    val name: String
)

data class BitcoinMultisigOpTransaction(
    val version: Int,
    val txIns: List<TransactionInput>,
    val txOuts: List<TransactionOutput>,
    val feePerVByte: Long,
    val totalFee: Long,
) {
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
}

data class EthereumMultisigOpTransaction(
    val safeNonce: BigInteger,
    val chainId: Long,
    val priorityFee: BigInteger? = null,
    val vaultAddress: String? = null,
    val contractAddresses: List<ContractNameAndAddress> = listOf()
)

data class ContractNameAndAddress(
    val name: String,
    val address: String
)

data class ApprovalDispositionRequestV2(
    val approvalDisposition: ApprovalDisposition,
    val signatures: List<Signature>
)

data class Slot<A>(val slotId: Byte, val value: A)

sealed class Signature {
    abstract val type: String

    data class BitcoinSignatures(
        val signatures: List<String>,
        override val type: String = "bitcoin"
    ) : Signature()

    data class EthereumSignature(
        val signature: String,
        val offchainSignature: NoChainSignature?,
        override val type: String = "ethereum"
    ) : Signature()

    data class PolygonSignature(
        val signature: String,
        val offchainSignature: NoChainSignature?,
        override val type: String = "polygon"
    ) : Signature()

    data class NoChainSignature(
        val signature: String,
        val signedData: String,
        override val type: String = "nochain"
    ) : Signature()
}