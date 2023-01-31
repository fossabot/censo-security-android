package com.censocustody.android.data.models.approvalV2

import com.censocustody.android.common.UriWrapper
import com.censocustody.android.common.evm.EvmAddress
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.approval.*
import com.censocustody.android.data.models.approval.ApprovalSignature.Companion.approvalSignatureAdapterFactory
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2.EvmTokenInfo.Companion.evmTokenInfoAdapterFactory
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2.OnChainPolicy.Companion.onChainPolicyAdapterFactory
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2.SigningData.Companion.signingDataAdapterFactory
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import java.lang.reflect.Modifier
import java.math.BigInteger

typealias OwnerAddress = String
data class Slot<A>(val slotId: Byte, val value: A)

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
) {
    companion object {
        fun toJson(approval: ApprovalRequestV2, uriWrapper: UriWrapper): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .registerTypeAdapterFactory(ApprovalRequestDetailsV2.approvalRequestDetailsV2AdapterFactory)
                .registerTypeAdapterFactory(onChainPolicyAdapterFactory)
                .registerTypeAdapterFactory(evmTokenInfoAdapterFactory)
                .registerTypeAdapterFactory(signingDataAdapterFactory)
                .registerTypeAdapterFactory(approvalSignatureAdapterFactory)
                .create()
                .toJson(approval)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): ApprovalRequestV2 {
            val approvalRequestV2Deserializer = ApprovalRequestV2Deserializer()
            return approvalRequestV2Deserializer.toObjectWithParsedDetails(json)
        }
    }

    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapterFactory(ApprovalRequestDetailsV2.approvalRequestDetailsV2AdapterFactory)
            .registerTypeAdapterFactory(onChainPolicyAdapterFactory)
            .registerTypeAdapterFactory(evmTokenInfoAdapterFactory)
            .registerTypeAdapterFactory(signingDataAdapterFactory)
            .registerTypeAdapterFactory(approvalSignatureAdapterFactory)
            .create()
            .toJson(this)
}

sealed class ApprovalRequestDetailsV2 {
    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapterFactory(approvalRequestDetailsV2AdapterFactory)
            .registerTypeAdapterFactory(onChainPolicyAdapterFactory)
            .registerTypeAdapterFactory(evmTokenInfoAdapterFactory)
            .registerTypeAdapterFactory(signingDataAdapterFactory)
            .registerTypeAdapterFactory(approvalSignatureAdapterFactory)
            .create()
            .toJson(this)

    fun isDeviceKeyApprovalType() =
        this is Login || this is VaultInvitation || this is PasswordReset

    companion object {
        val approvalRequestDetailsV2AdapterFactory: RuntimeTypeAdapterFactory<ApprovalRequestDetailsV2> = RuntimeTypeAdapterFactory.of(
            ApprovalRequestDetailsV2::class.java, "type"
        ).registerSubtype(
            VaultPolicyUpdate::class.java, "VaultPolicyUpdate"
        ).registerSubtype(
            BitcoinWalletCreation::class.java, "BitcoinWalletCreation"
        ).registerSubtype(
            EthereumWalletCreation::class.java, "EthereumWalletCreation"
        ).registerSubtype(
            PolygonWalletCreation::class.java, "PolygonWalletCreation"
        ).registerSubtype(
            EthereumWalletWhitelistUpdate::class.java, "EthereumWalletWhitelistUpdate"
        ).registerSubtype(
            PolygonWalletWhitelistUpdate::class.java, "PolygonWalletWhitelistUpdate"
        ).registerSubtype(
            EthereumWalletNameUpdate::class.java, "EthereumWalletNameUpdate"
        ).registerSubtype(
            PolygonWalletNameUpdate::class.java, "PolygonWalletNameUpdate"
        ).registerSubtype(
            EthereumWalletSettingsUpdate::class.java, "EthereumWalletSettingsUpdate"
        ).registerSubtype(
            PolygonWalletSettingsUpdate::class.java, "PolygonWalletSettingsUpdate"
        ).registerSubtype(
            EthereumTransferPolicyUpdate::class.java, "EthereumTransferPolicyUpdate"
        ).registerSubtype(
            PolygonTransferPolicyUpdate::class.java, "PolygonTransferPolicyUpdate"
        ).registerSubtype(
            CreateAddressBookEntry::class.java, "CreateAddressBookEntry"
        ).registerSubtype(
            DeleteAddressBookEntry::class.java, "DeleteAddressBookEntry"
        ).registerSubtype(
            BitcoinWithdrawalRequest::class.java, "BitcoinWithdrawalRequest"
        ).registerSubtype(
            EthereumWithdrawalRequest::class.java, "EthereumWithdrawalRequest"
        ).registerSubtype(
            PolygonWithdrawalRequest::class.java, "PolygonWithdrawalRequest"
        ).registerSubtype(
            PasswordReset::class.java, "PasswordReset"
        ).registerSubtype(
            Login::class.java, "Login"
        ).registerSubtype(
            VaultInvitation::class.java, "VaultInvitation"
        )
    }

    object UnknownApprovalType : ApprovalRequestDetailsV2()

    data class VaultPolicyUpdate(
        val approvalPolicy: VaultApprovalPolicy,
        val currentOnChainPolicies: List<OnChainPolicy>,
        val signingData: List<SigningData>
    ) : ApprovalRequestDetailsV2()

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
        val destinations: List<DestinationAddress>,
        val currentOnChainWhitelist: List<EvmAddress>,
        val fee: Amount,
        val feeSymbolInfo: EvmSymbolInfo,
        val signingData: SigningData.EthereumSigningData
    ) : ApprovalRequestDetailsV2()

    data class PolygonWalletWhitelistUpdate(
        val wallet: WalletInfo,
        val destinations: List<DestinationAddress>,
        val currentOnChainWhitelist: List<EvmAddress>,
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

    data class Signer(
        val name: String,
        val email: String,
        val publicKey: String,
        val nameHashIsEmpty: Boolean,
        val jpegThumbnail: String?
    )

    sealed class OnChainPolicy {

        companion object {
            val onChainPolicyAdapterFactory: RuntimeTypeAdapterFactory<OnChainPolicy> = RuntimeTypeAdapterFactory.of(
                OnChainPolicy::class.java, "chain"
            ).registerSubtype(
                Ethereum::class.java, "ethereum"
            ).registerSubtype(
                Polygon::class.java, "polygon"
            )
        }

        data class Ethereum(
            val owners: List<OwnerAddress>,
            val threshold: Int
        ) : OnChainPolicy()

        data class Polygon(
            val owners: List<OwnerAddress>,
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
    ) {
        fun fundamentalAmountAsBigInteger(): BigInteger {
            return BigInteger(nativeValue.replace(".", ""), 10)
        }
    }

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

    sealed class EvmTokenInfo {

        companion object {
            val evmTokenInfoAdapterFactory: RuntimeTypeAdapterFactory<EvmTokenInfo> = RuntimeTypeAdapterFactory.of(
                EvmTokenInfo::class.java, "type"
            ).registerSubtype(
                ERC20::class.java, "ERC20"
            ).registerSubtype(
                ERC721::class.java, "ERC721"
            ).registerSubtype(
                ERC1155::class.java, "ERC1155"
            )
        }
        data class ERC20(val contractAddress: String) : EvmTokenInfo()
        data class ERC721(val contractAddress: String, val tokenId: String) : EvmTokenInfo()
        data class ERC1155(val contractAddress: String, val tokenId: String) : EvmTokenInfo()
    }

    sealed class SigningData {

        companion object {
            val signingDataAdapterFactory: RuntimeTypeAdapterFactory<SigningData> =
                RuntimeTypeAdapterFactory.of(
                    SigningData::class.java, "type"
                ).registerSubtype(
                    BitcoinSigningData::class.java, "bitcoin"
                ).registerSubtype(
                    EthereumSigningData::class.java, "ethereum"
                ).registerSubtype(
                    PolygonSigningData::class.java, "polygon"
                )
        }

        data class ContractNameAndAddress(
            val name: String,
            val address: String
        )
        data class EthereumTransaction(
            val chainId: Long,
            val safeNonce: Long,
            val vaultAddress: String?,
            val contractAddresses: List<ContractNameAndAddress> = listOf()
        )

        data class BitcoinSigningData(
            val childKeyIndex: Int,
            val transaction: BitcoinTransaction
        ) : SigningData()

        data class EthereumSigningData(
            val transaction: EthereumTransaction
        ) : SigningData()

        data class PolygonSigningData(
            val transaction: EthereumTransaction
        ) : SigningData()

    }
}
