package com.censocustody.android.data.models.approvalV2

import androidx.biometric.BiometricPrompt
import com.censocustody.android.common.BaseWrapper.decodeFromBase64
import com.censocustody.android.common.evm.EvmDestination
import com.censocustody.android.common.evm.EvmTransactionUtil
import com.censocustody.android.common.evm.EvmWhitelistHelper
import com.censocustody.android.common.evm.SafeTx
import com.censocustody.android.data.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.evm.EvmConfigTransactionBuilder
import org.web3j.crypto.Hash
import kotlin.Exception
import com.censocustody.android.data.models.evm.EvmTransferTransactionBuilder
import org.bouncycastle.util.encoders.Hex

data class ApprovalDispositionRequestV2(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: ApprovalRequestDetailsV2,
    val email: String
) : SignableV2 {

    override fun retrieveSignableData(): List<SignableDataResult> {
        return when (requestType) {
            is ApprovalRequestDetailsV2.Login ->
                listOf(SignableDataResult.Device(
                    dataToSign = requestType.jwtToken.toByteArray(charset = Charsets.UTF_8),
                    dataToSend = requestType.jwtToken.toByteArray(charset = Charsets.UTF_8)
                ))

            is ApprovalRequestDetailsV2.PasswordReset ->
                listOf(SignableDataResult.Device(
                    dataToSend = requestId.toByteArray(charset = Charsets.UTF_8),
                    dataToSign = requestId.toByteArray(charset = Charsets.UTF_8)
                ))

            is ApprovalRequestDetailsV2.BitcoinWalletCreation,
            is ApprovalRequestDetailsV2.EthereumWalletCreation,
            is ApprovalRequestDetailsV2.PolygonWalletCreation,
            is ApprovalRequestDetailsV2.VaultCreation,
            is ApprovalRequestDetailsV2.CreateAddressBookEntry,
            is ApprovalRequestDetailsV2.DeleteAddressBookEntry,
            is ApprovalRequestDetailsV2.AddDevice,
            is ApprovalRequestDetailsV2.OrgNameUpdate,
            is ApprovalRequestDetailsV2.VaultUserRolesUpdate -> {
                val dataToSend = requestType.toJson().toByteArray()
                listOf(SignableDataResult.Offchain(
                    dataToSend = dataToSend,
                    dataToSign = Hash.sha256(dataToSend)))
            }

            is ApprovalRequestDetailsV2.AddDevice -> {
                val dataToSend = requestType.toJson().toByteArray()
                listOf(SignableDataResult.Offchain(
                    dataToSend = dataToSend,
                    dataToSign = Hash.sha256(dataToSend)))
            }

            is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> {
                listOf(
                    SignableDataResult.Bitcoin(
                        dataToSign = requestType.signingData.transaction.txIns.map {
                            decodeFromBase64(it.base64HashForSignature)
                        },
                        childKeyIndex = requestType.signingData.childKeyIndex
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
                        )
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
                        )
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
                        )
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
                        )
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
                        )
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
                        )
                    )
                )
            }
            is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
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
                        offchain = SignableDataResult.Offchain(
                            offchainDataToSend,
                            Hash.sha256(offchainDataToSend)
                        )
                    ),
                )
            }
            is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
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
                        offchain = SignableDataResult.Offchain(
                            offchainDataToSend,
                            Hash.sha256(offchainDataToSend)
                        )
                    ),
                )
            }

            is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
                requestType.signingData.mapNotNull { signingData ->
                    when (signingData) {
                        is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                            requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Ethereum>()
                                .firstOrNull()?.let {
                                    SignableDataResult.Ethereum(
                                        EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(
                                            calculateVaultSafeTxs(it.owners, it.threshold, requestType.approvalPolicy),
                                            signingData.transaction
                                        )
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
                                        )
                                    )
                                }
                        }
                        else -> null
                    }
                } + listOf(
                    SignableDataResult.Offchain(
                        dataToSend = offchainDataToSend,
                        dataToSign = Hash.sha256(offchainDataToSend)
                    )
                )
            }

            is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
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
                                    )
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
                                        )
                                    )
                                }
                        }
                        else -> null
                    }
                } + listOf(
                    SignableDataResult.Offchain(
                        dataToSend = offchainDataToSend,
                        dataToSign = Hash.sha256(offchainDataToSend)
                    )
                )
            }
            is ApprovalRequestDetailsV2.VaultNameUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
                requestType.signingData.mapNotNull { signingData ->
                    when (signingData) {
                        is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                            SignableDataResult.Ethereum(
                                EvmConfigTransactionBuilder.getNameUpdateExecutionFromModuleDataSafeHash(
                                    signingData.transaction.orgVaultAddress!!,
                                    signingData.transaction.vaultAddress!!,
                                    requestType.newName,
                                    signingData.transaction
                                )
                            )
                        }
                        is ApprovalRequestDetailsV2.SigningData.PolygonSigningData -> {
                            SignableDataResult.Polygon(
                                EvmConfigTransactionBuilder.getNameUpdateExecutionFromModuleDataSafeHash(
                                    signingData.transaction.orgVaultAddress!!,
                                    signingData.transaction.vaultAddress!!,
                                    requestType.newName,
                                    signingData.transaction
                                )
                            )
                        }
                        else -> null
                    }
                } + listOf(
                    SignableDataResult.Offchain(
                        dataToSend = offchainDataToSend,
                        dataToSign = Hash.sha256(offchainDataToSend)
                    )
                )
            }
            else -> listOf()
        }
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

        return RegisterApprovalDispositionV2Body(
            approvalDisposition = approvalDisposition,
            signatures = signatures
        )
    }

    inner class RegisterApprovalDispositionV2Body(
        val approvalDisposition: ApprovalDisposition,
        val signatures: List<ApprovalSignature>
    )
}
