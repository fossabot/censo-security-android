package com.censocustody.android.data.models.approvalV2

import com.censocustody.android.common.BaseWrapper.decodeFromBase64
import com.censocustody.android.common.evm.EvmDestination
import com.censocustody.android.common.evm.EvmTransactionUtil
import com.censocustody.android.common.evm.EvmWhitelistHelper
import com.censocustody.android.common.evm.SafeTx
import com.censocustody.android.data.*
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.evm.EvmConfigTransactionBuilder
import org.web3j.crypto.Hash
import kotlin.Exception
import com.censocustody.android.data.models.evm.EvmTransferTransactionBuilder
import org.bouncycastle.util.encoders.Hex

data class ApprovalDispositionRequestV2(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: ApprovalRequestDetailsV2,
    val email: String,
    val shards: List<Shard>?
) : SignableV2 {

    override fun retrieveSignableData(): List<SignableDataResult> {
        return when (approvalDisposition) {
            ApprovalDisposition.APPROVE -> {
                when (requestType) {
                    is ApprovalRequestDetailsV2.Login -> {
                        getLoginSignableData(requestType)
                    }

                    is ApprovalRequestDetailsV2.PasswordReset -> {
                        getPasswordResetSignableData()
                    }

                    is ApprovalRequestDetailsV2.BitcoinWalletCreation,
                    is ApprovalRequestDetailsV2.EthereumWalletCreation,
                    is ApprovalRequestDetailsV2.PolygonWalletCreation,
                    is ApprovalRequestDetailsV2.VaultCreation,
                    is ApprovalRequestDetailsV2.CreateAddressBookEntry,
                    is ApprovalRequestDetailsV2.DeleteAddressBookEntry,
                    is ApprovalRequestDetailsV2.AddDevice,
                    is ApprovalRequestDetailsV2.RemoveDevice,
                    is ApprovalRequestDetailsV2.OrgNameUpdate,
                    is ApprovalRequestDetailsV2.VaultUserRolesUpdate,
                    is ApprovalRequestDetailsV2.SuspendUser,
                    is ApprovalRequestDetailsV2.RestoreUser -> {
                        listOf(getApprovalRequestDetailsSignature())
                    }

                    is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> {
                        listOf(
                            SignableDataResult.Bitcoin(
                                dataToSign = requestType.signingData.transaction.txIns.map {
                                    decodeFromBase64(it.base64HashForSignature)
                                },
                                childKeyIndex = requestType.signingData.childKeyIndex,
                                offchain = getApprovalRequestDetailsSignature()
                            )
                        )
                    }

                    is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> {
                        listOf(
                            SignableDataResult.Ethereum(
                                EvmTransferTransactionBuilder.withdrawalSafeHash(
                                    requestType.symbolInfo,
                                    requestType.amount,
                                    requestType.wallet.address,
                                    requestType.destination.address,
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }

                    is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
                        listOf(
                            SignableDataResult.Polygon(
                                EvmTransferTransactionBuilder.withdrawalSafeHash(
                                    requestType.symbolInfo,
                                    requestType.amount,
                                    requestType.wallet.address,
                                    requestType.destination.address,
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }

                    is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate -> {
                        listOf(
                            SignableDataResult.Ethereum(
                                EvmConfigTransactionBuilder.getSetGuardExecutionFromModuleDataSafeHash(
                                    requestType.wallet.address,
                                    EvmWhitelistHelper.getTargetGuardAddress(
                                        requestType.currentGuardAddress,
                                        requestType.whitelistEnabled?.let { it == ApprovalRequestDetailsV2.BooleanSetting.On },
                                        requestType.dappsEnabled?.let { it == ApprovalRequestDetailsV2.BooleanSetting.On },
                                        requestType.signingData.transaction.contractAddresses
                                    ),
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }
                    is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
                        listOf(
                            SignableDataResult.Polygon(
                                EvmConfigTransactionBuilder.getSetGuardExecutionFromModuleDataSafeHash(
                                    requestType.wallet.address,
                                    EvmWhitelistHelper.getTargetGuardAddress(
                                        requestType.currentGuardAddress,
                                        requestType.whitelistEnabled?.let { it == ApprovalRequestDetailsV2.BooleanSetting.On },
                                        requestType.dappsEnabled?.let { it == ApprovalRequestDetailsV2.BooleanSetting.On },
                                        requestType.signingData.transaction.contractAddresses
                                    ),
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }
                    is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate -> {
                        listOf(
                            SignableDataResult.Ethereum(
                                EvmConfigTransactionBuilder.getUpdateWhitelistExecutionFromModuleDataSafeHash(
                                    requestType.wallet.address,
                                    EvmWhitelistHelper(
                                        requestType.currentOnChainWhitelist,
                                        requestType.destinations.map { EvmDestination(it.name, it.address) }
                                    ).allChanges().map { Hex.decode(it) },
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }
                    is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
                        listOf(
                            SignableDataResult.Polygon(
                                EvmConfigTransactionBuilder.getUpdateWhitelistExecutionFromModuleDataSafeHash(
                                    requestType.wallet.address,
                                    EvmWhitelistHelper(
                                        requestType.currentOnChainWhitelist,
                                        requestType.destinations.map { EvmDestination(it.name, it.address) }
                                    ).allChanges().map { Hex.decode(it) },
                                    requestType.signingData.transaction
                                ),
                                getApprovalRequestDetailsSignature()
                            )
                        )
                    }
                    is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate -> {
                        listOf(
                            SignableDataResult.Ethereum(
                                EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                                    requestType.signingData.transaction.vaultAddress!!,
                                    requestType.wallet.address,
                                    calculateWalletSafeTxs(
                                        requestType.currentOnChainPolicy.owners,
                                        requestType.currentOnChainPolicy.threshold,
                                        requestType.approvalPolicy),
                                    requestType.signingData.transaction
                                ),
                                offchain = getApprovalRequestDetailsSignature()
                            ),
                        )
                    }
                    is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
                        listOf(
                            SignableDataResult.Polygon(
                                EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                                    requestType.signingData.transaction.vaultAddress!!,
                                    requestType.wallet.address,
                                    calculateWalletSafeTxs(
                                        requestType.currentOnChainPolicy.owners,
                                        requestType.currentOnChainPolicy.threshold,
                                        requestType.approvalPolicy),
                                    requestType.signingData.transaction
                                ),
                                offchain = getApprovalRequestDetailsSignature()
                            ),
                        )
                    }

                    is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                        val approvalRequestSignature = getApprovalRequestDetailsSignature()
                        requestType.signingData.mapNotNull { signingData ->
                            when (signingData) {
                                is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                                    requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Ethereum>()
                                        .firstOrNull()?.let {
                                            SignableDataResult.Ethereum(
                                                EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(
                                                    calculateVaultSafeTxs(it.owners, it.threshold, requestType.approvalPolicy),
                                                    signingData.transaction
                                                ),
                                                approvalRequestSignature
                                            )
                                        }
                                }
                                is ApprovalRequestDetailsV2.SigningData.PolygonSigningData -> {
                                    requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Polygon>()
                                        .firstOrNull()?.let {
                                            SignableDataResult.Polygon(
                                                EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(
                                                    calculateVaultSafeTxs(it.owners, it.threshold, requestType.approvalPolicy),
                                                    signingData.transaction
                                                ),
                                                approvalRequestSignature
                                            )
                                        }
                                }
                                else -> null
                            }
                        } + listOf(approvalRequestSignature)
                    }

                    is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
                        val approvalRequestSignature = getApprovalRequestDetailsSignature()
                        requestType.signingData.mapNotNull { signingData ->
                            when (signingData) {
                                is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                                    requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Ethereum>()
                                        .firstOrNull()?.let {
                                            SignableDataResult.Ethereum(
                                                EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                                                    signingData.transaction.orgVaultAddress!!,
                                                    signingData.transaction.vaultAddress!!,
                                                    calculateVaultSafeTxs(it.owners, it.threshold, requestType.approvalPolicy),
                                                    signingData.transaction
                                                ),
                                                approvalRequestSignature
                                            )
                                        }
                                }
                                is ApprovalRequestDetailsV2.SigningData.PolygonSigningData -> {
                                    requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Polygon>()
                                        .firstOrNull()?.let {
                                            SignableDataResult.Polygon(
                                                EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                                                    signingData.transaction.orgVaultAddress!!,
                                                    signingData.transaction.vaultAddress!!,
                                                    calculateVaultSafeTxs(it.owners, it.threshold, requestType.approvalPolicy),
                                                    signingData.transaction
                                                ),
                                                approvalRequestSignature
                                            )
                                        }
                                }
                                else -> null
                            }
                        } + listOf(approvalRequestSignature)
                    }
                    is ApprovalRequestDetailsV2.VaultNameUpdate -> {
                        val approvalRequestSignature = getApprovalRequestDetailsSignature()
                        requestType.signingData.mapNotNull { signingData ->
                            when (signingData) {
                                is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                                    SignableDataResult.Ethereum(
                                        EvmConfigTransactionBuilder.getNameUpdateExecutionFromModuleDataSafeHash(
                                            signingData.transaction.orgVaultAddress!!,
                                            signingData.transaction.vaultAddress!!,
                                            requestType.newName,
                                            signingData.transaction
                                        ),
                                        approvalRequestSignature
                                    )
                                }
                                is ApprovalRequestDetailsV2.SigningData.PolygonSigningData -> {
                                    SignableDataResult.Polygon(
                                        EvmConfigTransactionBuilder.getNameUpdateExecutionFromModuleDataSafeHash(
                                            signingData.transaction.orgVaultAddress!!,
                                            signingData.transaction.vaultAddress!!,
                                            requestType.newName,
                                            signingData.transaction
                                        ),
                                        approvalRequestSignature
                                    )
                                }
                                else -> null
                            }
                        } + listOf(approvalRequestSignature)
                    }
                    is ApprovalRequestDetailsV2.EnableRecoveryContract -> {
                        val approvalRequestSignature = getApprovalRequestDetailsSignature()
                        requestType.signingData.mapNotNull { signingData ->
                            when (signingData) {
                                is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                                    SignableDataResult.Ethereum(
                                        EvmConfigTransactionBuilder.getEnableRecoveryContractExecutionFromModuleDataSafeHash(
                                            signingData.transaction.orgVaultAddress!!,
                                            requestType.recoveryThreshold,
                                            requestType.recoveryAddresses,
                                            requestType.orgName,
                                            signingData.transaction
                                        ),
                                        approvalRequestSignature
                                    )
                                }
                                is ApprovalRequestDetailsV2.SigningData.PolygonSigningData-> {
                                    SignableDataResult.Polygon(
                                        EvmConfigTransactionBuilder.getEnableRecoveryContractExecutionFromModuleDataSafeHash(
                                            signingData.transaction.orgVaultAddress!!,
                                            requestType.recoveryThreshold,
                                            requestType.recoveryAddresses,
                                            requestType.orgName,
                                            signingData.transaction
                                        ),
                                        approvalRequestSignature
                                    )
                                }
                                else -> null
                            }
                        } + listOf(approvalRequestSignature)
                    }
                    else -> listOf()
                }
            }
            ApprovalDisposition.DENY -> {
                when (requestType) {
                    is ApprovalRequestDetailsV2.Login -> {
                        getLoginSignableData(requestType)
                    }

                    is ApprovalRequestDetailsV2.PasswordReset -> {
                        getPasswordResetSignableData()
                    }

                    else -> listOf(getApprovalRequestDetailsSignature())
                }
            }
        }
    }

    private fun getLoginSignableData(approvalRequestDetails: ApprovalRequestDetailsV2.Login): List<SignableDataResult> {
        val payload = "{\"token\":\"${approvalRequestDetails.jwtToken}\",\"disposition\":\"${approvalDisposition.value}\"}".toByteArray(charset = Charsets.UTF_8)
        return listOf(SignableDataResult.Device(dataToSign = payload, dataToSend = payload))
    }

    private fun getPasswordResetSignableData(): List<SignableDataResult> {
        val payload = "{\"guid\":\"${requestId}\",\"disposition\":\"${approvalDisposition.value}\"}".toByteArray(charset = Charsets.UTF_8)
        return listOf(SignableDataResult.Device(dataToSign = payload, dataToSend = payload))
    }

    private fun getApprovalRequestDetailsSignature(): SignableDataResult.Offchain {
        val approvalRequestDetails = ApprovalRequestDetailsWithDisposition(
            requestType,
            approvalDisposition
        ).toJson().toByteArray()
        return SignableDataResult.Offchain(dataToSend = approvalRequestDetails, dataToSign = Hash.sha256(approvalRequestDetails))
    }

    private fun calculateVaultSafeTxs(currentOwners: List<String>, currentThreshold: Int, targetPolicy: ApprovalRequestDetailsV2.VaultApprovalPolicy): List<SafeTx> {
        val startingPolicy = SafeTx.Policy(currentOwners, currentThreshold)
        val updatedTargetPolicy = SafeTx.Policy(
            targetPolicy.approvers.mapNotNull { it.publicKeys.find { it.chain == Chain.ethereum }?.key?.let { EvmTransactionUtil.getEthereumAddressFromBase58(it) } },
            targetPolicy.approvalsRequired
        )
        return startingPolicy.safeTransactions(updatedTargetPolicy).first
    }

    private fun calculateWalletSafeTxs(currentOwners: List<String>, currentThreshold: Int, targetPolicy: ApprovalRequestDetailsV2.WalletApprovalPolicy): List<SafeTx> {
        val startingPolicy = SafeTx.Policy(currentOwners, currentThreshold)
        val updatedTargetPolicy = SafeTx.Policy(
            targetPolicy.approvers.map { EvmTransactionUtil.getEthereumAddressFromBase58(it.publicKey) },
            targetPolicy.approvalsRequired
        )
        return startingPolicy.safeTransactions(updatedTargetPolicy).first
    }

    fun convertToApiBody(encryptionManager: EncryptionManager): RegisterApprovalDispositionV2Body {
        val dataToSign = retrieveSignableData()

        val signatures =
            if (requestType.isDeviceKeyApprovalType()) {
                val deviceKeyDataToSign = dataToSign[0]
                if (deviceKeyDataToSign !is SignableDataResult.Device) throw Exception("Device key requires offchain data")
                val signedData =
                    encryptionManager.signApprovalDispositionForDeviceKey(
                        email = email, dataToSign = deviceKeyDataToSign
                    )
                listOf(signedData)
            } else {
                encryptionManager.signApprovalDisposition(
                    email = email, dataToSign = dataToSign
                )
            }

        val updatedShards = updateShards(encryptionManager)

        return RegisterApprovalDispositionV2Body(
            approvalDisposition = approvalDisposition,
            signatures = signatures,
            recoveryShards = updatedShards,
        )
    }

    private fun updateShards(encryptionManager: EncryptionManager): List<RecoveryShard>? {
        if (shards == null || shards.isEmpty()) return null

        return when (requestType) {
            is ApprovalRequestDetailsV2.AddDevice ->
                encryptionManager.reEncryptShards(email = email, shards = shards, publicKey = requestType.deviceKey)
            else -> null
        }
    }

    inner class RegisterApprovalDispositionV2Body(
        val approvalDisposition: ApprovalDisposition,
        val signatures: List<ApprovalSignature>,
        val shards: List<Shard>? = null,
        val recoveryShards: List<RecoveryShard>? = null
    )

    data class ApprovalRequestDetailsWithDisposition(
        val approvalRequestDetails: ApprovalRequestDetailsV2,
        val disposition: ApprovalDisposition
    ) {
        fun toJson(): String =
            ApprovalRequestDetailsV2
                .gsonBuilder
                .toJson(this, ApprovalRequestDetailsWithDisposition::class.java)
    }
}
