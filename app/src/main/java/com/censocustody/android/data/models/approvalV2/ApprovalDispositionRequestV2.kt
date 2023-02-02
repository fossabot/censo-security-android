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
import com.censocustody.android.data.models.approval.ApprovalSignature
import com.censocustody.android.data.models.approval.BooleanSetting
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

            is ApprovalRequestDetailsV2.VaultInvitation ->
                listOf(SignableDataResult.Device(
                    dataToSend = requestType.vaultName.toByteArray(charset = Charsets.UTF_8),
                    dataToSign = requestType.vaultName.toByteArray(charset = Charsets.UTF_8)
                ))

            is ApprovalRequestDetailsV2.PasswordReset ->
                listOf(SignableDataResult.Device(
                    dataToSend = requestId.toByteArray(charset = Charsets.UTF_8),
                    dataToSign = requestId.toByteArray(charset = Charsets.UTF_8)
                ))

            is ApprovalRequestDetailsV2.BitcoinWalletCreation,
            is ApprovalRequestDetailsV2.EthereumWalletCreation,
            is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
                val dataToSend = requestType.toJson().toByteArray()
                listOf(SignableDataResult.Offchain(
                    dataToSend = dataToSend,
                    dataToSign = Hash.sha256(dataToSend)))
            }

            is ApprovalRequestDetailsV2.CreateAddressBookEntry,
            is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
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
                                requestType.whitelistEnabled?.let { it == BooleanSetting.On },
                                requestType.dappsEnabled?.let { it == BooleanSetting.On },
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
                                requestType.whitelistEnabled?.let { it == BooleanSetting.On },
                                requestType.dappsEnabled?.let { it == BooleanSetting.On },
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
                val startingPolicy = SafeTx.Policy(requestType.currentOnChainPolicy.owners, requestType.currentOnChainPolicy.threshold)
                val targetPolicy = SafeTx.Policy(
                    requestType.approvalPolicy.approvers.map { EvmTransactionUtil.getEthereumAddressFromBase58(it.value.publicKey) },
                    requestType.approvalPolicy.approvalsRequired
                )
                val offchainDataToSend = requestType.toJson().toByteArray()
                listOf(
                    SignableDataResult.Ethereum(
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                            requestType.wallet.address,
                            startingPolicy.safeTransactions(targetPolicy).first,
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
                val startingPolicy = SafeTx.Policy(requestType.currentOnChainPolicy.owners, requestType.currentOnChainPolicy.threshold)
                val targetPolicy = SafeTx.Policy(
                    requestType.approvalPolicy.approvers.map { EvmTransactionUtil.getEthereumAddressFromBase58(it.value.publicKey) },
                    requestType.approvalPolicy.approvalsRequired
                )
                val offchainDataToSend = requestType.toJson().toByteArray()
                listOf(
                    SignableDataResult.Ethereum(
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                            requestType.wallet.address,
                            startingPolicy.safeTransactions(targetPolicy).first,
                            requestType.signingData.transaction
                        ),
                        offchain = SignableDataResult.Offchain(
                            offchainDataToSend,
                            Hash.sha256(offchainDataToSend)
                        )
                    ),
                )
            }
            is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
                val offchainDataToSend = requestType.toJson().toByteArray()
                requestType.signingData.mapNotNull { signingData ->
                    when (signingData) {
                        is ApprovalRequestDetailsV2.SigningData.EthereumSigningData -> {
                            requestType.currentOnChainPolicies.filterIsInstance<ApprovalRequestDetailsV2.OnChainPolicy.Ethereum>()
                                .firstOrNull()?.let {
                                val startingPolicy = SafeTx.Policy(it.owners, it.threshold)
                                val targetPolicy = SafeTx.Policy(
                                    requestType.approvalPolicy.approvers.mapNotNull { it.value.publicKeys.find { it.chain == Chain.ethereum }?.key?.let { EvmTransactionUtil.getEthereumAddressFromBase58(it) } },
                                    requestType.approvalPolicy.approvalsRequired
                                )
                                SignableDataResult.Ethereum(
                                    EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(
                                        startingPolicy.safeTransactions(targetPolicy).first,
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
            else -> listOf()
        }
    }

    fun convertToApiBody(
        encryptionManager: EncryptionManager,
        cryptoObject: BiometricPrompt.CryptoObject
    ): RegisterApprovalDispositionV2Body {

        val cipher = cryptoObject.cipher
        val signature = cryptoObject.signature

        val dataToSign = retrieveSignableData()

        val signatures =
            if (requestType.isDeviceKeyApprovalType()) {
                if (signature == null) throw Exception("Missing biometry approved signature")
                val deviceKeyDataToSign = dataToSign[0]
                if (deviceKeyDataToSign !is SignableDataResult.Device) throw Exception("Device key requires offchain data")
                val signedData =
                    encryptionManager.signApprovalDispositionForDeviceKey(
                        email = email, signature = signature, dataToSign = deviceKeyDataToSign
                    )
                listOf(signedData)
            } else {
                if (cipher == null) throw Exception("Missing biometry approved cipher")
                encryptionManager.signApprovalDisposition(
                    email = email, cipher = cipher, dataToSign = dataToSign
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
