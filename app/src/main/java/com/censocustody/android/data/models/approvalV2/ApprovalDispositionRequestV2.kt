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
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.data.models.approval.ApprovalSignature
import com.censocustody.android.data.models.approval.BooleanSetting
import com.censocustody.android.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.censocustody.android.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import com.censocustody.android.data.models.evm.EvmConfigTransactionBuilder
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import kotlin.Exception
import com.censocustody.android.data.models.evm.EvmTransferTransactionBuilder

import java.security.Signature
import javax.crypto.Cipher
import org.bouncycastle.util.encoders.Hex

data class ApprovalDispositionRequestV2(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: ApprovalRequestDetailsV2,
    val email: String
) : SignableV2 {

    override fun retrieveSignableData(approverPublicKey: String?): List<SignableDataResult> {
        return when (requestType) {
            is ApprovalRequestDetailsV2.Login ->
                listOf(SignableDataResult.Device(requestType.jwtToken.toByteArray(charset = Charsets.UTF_8)))

            is ApprovalRequestDetailsV2.VaultInvitation ->
                listOf(SignableDataResult.Device(requestType.vaultName.toByteArray(charset = Charsets.UTF_8)))

            is ApprovalRequestDetailsV2.PasswordReset ->
                listOf(SignableDataResult.Device(requestId.toByteArray(charset = Charsets.UTF_8)))

            is ApprovalRequestDetailsV2.BitcoinWalletCreation,
            is ApprovalRequestDetailsV2.EthereumWalletCreation,
            is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
                val dataToSend = requestType.toJson().toByteArray()
                listOf(SignableDataResult.Offchain(dataToSend, Hash.sha256(dataToSend)))
            }

            is ApprovalRequestDetailsV2.CreateAddressBookEntry,
            is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
                val dataToSend = requestType.toJson().toByteArray()
                listOf(SignableDataResult.Offchain(dataToSend, Hash.sha256(dataToSend)))
            }

            is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> {
                requestType.signingData.transaction.txIns.map {
                    SignableDataResult.Bitcoin(decodeFromBase64(it.base64HashForSignature))
                }
            }

            is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> {
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

            is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
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
                val targetPolicy = SafeTx.Policy(requestType.approvalPolicy.approvers.map { it.value.publicKey}, requestType.approvalPolicy.approvalsRequired)
                val offchainDataToSend = requestType.toJson().toByteArray()
                listOf(
                    SignableDataResult.Ethereum(
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                            requestType.wallet.address,
                            startingPolicy.safeTransactions(targetPolicy).first,
                            requestType.signingData.transaction
                        )
                    ),
                    SignableDataResult.Offchain(
                        offchainDataToSend,
                        Hash.sha256(offchainDataToSend)
                    )
                )
            }
            is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
                val startingPolicy = SafeTx.Policy(requestType.currentOnChainPolicy.owners, requestType.currentOnChainPolicy.threshold)
                val targetPolicy = SafeTx.Policy(requestType.approvalPolicy.approvers.map { it.value.publicKey}, requestType.approvalPolicy.approvalsRequired)
                val offchainDataToSend = requestType.toJson().toByteArray()
                listOf(
                    SignableDataResult.Ethereum(
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(
                            requestType.wallet.address,
                            startingPolicy.safeTransactions(targetPolicy).first,
                            requestType.signingData.transaction
                        )
                    ),
                    SignableDataResult.Offchain(
                        offchainDataToSend,
                        Hash.sha256(offchainDataToSend)
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
                                val startingPolicy = SafeTx.Policy(it.owners, it.threshold)
                                val targetPolicy = SafeTx.Policy(
                                    requestType.approvalPolicy.approvers.mapNotNull { it.value.publicKeys.find { it.chain == Chain.ethereum }?.key },
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
                        offchainDataToSend,
                        Hash.sha256(offchainDataToSend)
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

        if (requestType.isDeviceKeyApprovalType()) {
            if (signature == null) throw Exception("Missing biometry approved signature")
        } else {
            if (cipher == null) throw Exception("Missing biometry approved cipher")
        }

        val signatureInfo: ApprovalSignature = when (requestType) {
            is ApprovalRequestDetailsV2.Login, is ApprovalRequestDetailsV2.PasswordReset, is ApprovalRequestDetails.VaultInvitation ->
                ApprovalSignature.NoChainSignature(
                    signRequestWithDeviceKey(encryptionManager, signature!!)
                )
            is WalletCreation -> getSignatureInfo(requestType.accountInfo.chain ?: Chain.solana, encryptionManager, cipher!!)
            is CreateAddressBookEntry -> getSignatureInfo(requestType.chain, encryptionManager, cipher!!)
            is DeleteAddressBookEntry -> getSignatureInfo(requestType.chain, encryptionManager, cipher!!)
            is WithdrawalRequest -> {
                when (requestType.signingData) {
                    is SigningData.BitcoinSigningData ->
                        ApprovalSignature.BitcoinSignatures(
                            signatures = signRequestWithBitcoinKey(encryptionManager, cipher!!, requestType.signingData.childKeyIndex).map { it.signature }
                        )
                    is SigningData.SolanaSigningData ->
                        ApprovalSignature.SolanaSignature(
                            signature = signRequestWithSolanaKey(encryptionManager, cipher!!).signature,
                            nonce = nonces.first().value,
                            nonceAccountAddress = requestType.nonceAccountAddresses().first()
                        )
                    is SigningData.EthereumSigningData ->
                        ApprovalSignature.EthereumSignature(
                            signature = signRequestWithEthereumKey(encryptionManager, cipher!!).signature,
                        )
                }
            }
            else -> ApprovalSignature.SolanaSignature(
                signature = signRequestWithSolanaKey(encryptionManager, cipher!!).signature,
                nonce = nonces.first().value,
                nonceAccountAddress = requestType.nonceAccountAddresses().first()
            )
        }
        
        return RegisterApprovalDispositionBody(
            approvalDisposition = approvalDisposition,
            signatureInfo = signatureInfo
        )
    }

    private fun getSignatureInfo(chain: Chain, encryptionManager: EncryptionManager, cipher: Cipher): ApprovalSignature {
        return when (chain) {
            Chain.bitcoin, Chain.ethereum -> ApprovalSignature.NoChainSignature(
                signRequestWithCensoKey(encryptionManager, cipher)
            )
            else -> ApprovalSignature.SolanaSignature(
                signature = signRequestWithSolanaKey(encryptionManager, cipher).signature,
                nonce = nonces.first().value,
                nonceAccountAddress = requestType.nonceAccountAddresses().first()
            )
        }
    }

    private fun signRequestWithDeviceKey(
        encryptionManager: EncryptionManager,
        signature: Signature) : SignedPayload {
        return try {
            encryptionManager.signApprovalWithDeviceKey(
                signable = this,
                email = email,
                signature = signature
            )
        } catch (e: Exception) {
            throw Exception("Signing with device key failure")
        }
    }

    private fun signRequestWithSolanaKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signSolanaApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher
            )
        } catch (e: Exception) {
            throw Exception("Signing with solana key failure")
        }
    }

    private fun signRequestWithBitcoinKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher,
        childKeyIndex: Int): List<SignedPayload> {

        return try {
            encryptionManager.signBitcoinApprovalDispositionMessage(
                signable = this,
                cipher = cipher,
                email = email,
                childKeyIndex = childKeyIndex
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    private fun signRequestWithEthereumKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signEthereumApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher,
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    private fun signRequestWithCensoKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signCensoApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher,
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    inner class RegisterApprovalDispositionV2Body(
        val approvalDisposition: ApprovalDisposition,
        val signatures: List<ApprovalSignature>
    )
}
