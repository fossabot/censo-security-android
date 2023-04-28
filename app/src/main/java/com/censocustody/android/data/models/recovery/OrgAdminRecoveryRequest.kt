package com.censocustody.android.data.models

import com.censocustody.android.data.models.OrgAdminRecoveryRequest.RecoverySafeTx.Companion.recoverySafeTxAdapterFactory
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.evm.EvmRecoveryTransactionBuilder
import com.censocustody.android.data.models.recovery.SignableRecoveryData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import org.web3j.crypto.Hash
import java.lang.reflect.Modifier

data class OrgAdminRecoveredDeviceAndSigners(
    val userDevice: UserDevice,
    val signersInfo: Signers,
)

data class PublicKeyInfo(
    val key: String,
    var chain: Chain,
)

data class OrgAdminRecoveryRequest(
    val deviceKey: String,
    val chainKeys: List<PublicKeyInfo>,
    val recoveryTxs: List<AdminRecoveryTxs>,
    val signingData: List<ApprovalRequestDetailsV2.SigningData>,
) {

    fun toJson(): String =
        gsonBuilder.toJson(this, OrgAdminRecoveryRequest::class.java)

    companion object {
        val gsonBuilder: Gson = GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapterFactory(recoverySafeTxAdapterFactory)
            .registerTypeAdapterFactory(ApprovalRequestDetailsV2.SigningData.signingDataAdapterFactory)
            .create()

        fun fromString(json: String): OrgAdminRecoveryRequest {
            return gsonBuilder.fromJson(json, OrgAdminRecoveryRequest::class.java)
        }
    }

    sealed class RecoverySafeTx {

        companion object {
            val recoverySafeTxAdapterFactory: RuntimeTypeAdapterFactory<RecoverySafeTx> =
                RuntimeTypeAdapterFactory.of(
                    RecoverySafeTx::class.java, "type"
                ).registerSubtype(
                    OrgVaultSwapOwner::class.java, "OrgVaultSwapOwner"
                ).registerSubtype(
                    VaultSwapOwner::class.java, "VaultSwapOwner"
                ).registerSubtype(
                    WalletSwapOwner::class.java, "WalletSwapOwner"
                )
        }
        data class OrgVaultSwapOwner(
            val prev: String,
        ) : RecoverySafeTx()

        data class VaultSwapOwner(
            val prev: String,
            val vaultSafeAddress: String
        ) : RecoverySafeTx()

        data class WalletSwapOwner(
            val prev: String,
            val vaultSafeAddress: String,
            val walletSafeAddress: String
        ) : RecoverySafeTx()
    }

    data class AdminRecoveryTxs(
        val chain: Chain,
        val recoveryContractAddress: String,
        val orgVaultSafeAddress: String,
        val oldOwnerAddress: String,
        val newOwnerAddress: String,
        val txs: List<RecoverySafeTx>,
    )

    fun retrieveSignableData(): List<SignableRecoveryData> {
        val offchainData = this.toJson().toByteArray()
        return this.recoveryTxs.mapNotNull { recoveryTxs ->
            when (recoveryTxs.chain) {
                Chain.ethereum -> SignableRecoveryData.Ethereum(
                    EvmRecoveryTransactionBuilder.getRecoveryDataSafeHash(
                        recoveryTxs,
                        signingData.filterIsInstance<ApprovalRequestDetailsV2.SigningData.EthereumSigningData>()
                            .first().transaction
                    )
                )
                Chain.polygon -> SignableRecoveryData.Polygon(
                    EvmRecoveryTransactionBuilder.getRecoveryDataSafeHash(
                        recoveryTxs,
                        signingData.filterIsInstance<ApprovalRequestDetailsV2.SigningData.PolygonSigningData>()
                            .first().transaction
                    )
                )
                else -> null
            }
        } + listOf(
            SignableRecoveryData.Offchain(offchainData, Hash.sha256(offchainData))
        )
    }
}

