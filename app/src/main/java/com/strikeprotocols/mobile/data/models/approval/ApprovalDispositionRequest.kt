package com.strikeprotocols.mobile.data.models.approval

import androidx.biometric.BiometricPrompt
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import kotlin.Exception
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.INVALID_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import javax.crypto.Cipher

data class ApprovalDispositionRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: SolanaApprovalRequestType,
    val nonces: List<Nonce>,
    val email: String
) : Signable {

    val opIndex: Byte = 9

    val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    private fun retrieveOpCode(): Byte {
        return when (requestType) {
            is BalanceAccountCreation -> 1
            is WithdrawalRequest, is ConversionRequest -> 3
            is SignersUpdate -> 5
            is WrapConversionRequest -> 4
            is WalletConfigPolicyUpdate -> 6
            is DAppTransactionRequest -> 7
            is BalanceAccountSettingsUpdate -> 8

            is DAppBookUpdate -> 9
            is AddressBookUpdate -> 10
            is BalanceAccountNameUpdate -> 11
            is BalanceAccountPolicyUpdate -> 12
            is BalanceAccountAddressWhitelistUpdate -> 14
            is SignData -> 15

            is LoginApprovalRequest,
            is UnknownApprovalType,
            is AcceptVaultInvitation,
            is PasswordReset -> 0
        }
    }

    fun opHashData(): ByteArray {
        val commonBytes = signingData().commonOpHashBytes()

        signingData().base64DataToSign?.let {
            return@opHashData SignDataHelper.serializeSignData(it, commonBytes, 15)
        }

        return when (requestType) {
            is BalanceAccountCreation -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is WithdrawalRequest -> {
                val buffer = ByteArrayOutputStream()

                val opAndCommonBuffer = ByteArrayOutputStream()
                opAndCommonBuffer.write(byteArrayOf(retrieveOpCode()))
                opAndCommonBuffer.write(commonBytes)
                buffer.write(opAndCommonBuffer.toByteArray())

                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.destination.address.base58Bytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

                buffer.toByteArray()
            }
            is ConversionRequest -> {
                val buffer = ByteArrayOutputStream()

                val opAndCommonBuffer = ByteArrayOutputStream()
                opAndCommonBuffer.write(byteArrayOf(retrieveOpCode()))
                opAndCommonBuffer.write(commonBytes)
                buffer.write(opAndCommonBuffer.toByteArray())

                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.destination.address.base58Bytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

                buffer.toByteArray()
            }
            is SignersUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
                buffer.write(requestType.signer.opHashBytes())

                buffer.toByteArray()
            }

            is WrapConversionRequest -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(byteArrayOf(requestType.symbolAndAmountInfo.symbolInfo.getSOLProgramValue()))

                buffer.toByteArray()
            }
            is WalletConfigPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.approvalPolicy.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountSettingsUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is DAppBookUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is AddressBookUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountNameUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountAddressWhitelistUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                buffer.toByteArray()
            }

            is DAppTransactionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.dappInfo.address.base58Bytes())
                buffer.write(requestType.dappInfo.name.sha256HashBytes())
                buffer.writeShortLE(requestType.instructions.sumOf { it.decodedData().size }.toShort())
                buffer.write(requestType.instructions.map { it.decodedData() }.reduce { array, next -> array + next })
                buffer.toByteArray()
            }
            is SignData -> {
                SignDataHelper.serializeSignData(requestType.base64Data, commonBytes, 15)
            }
            is LoginApprovalRequest, is AcceptVaultInvitation, is PasswordReset -> throw Exception(
                INVALID_REQUEST_APPROVAL
            )
            is UnknownApprovalType -> throw Exception(
                UNKNOWN_REQUEST_APPROVAL
            )
        }
    }

    private fun signingData(): SolanaSigningData =
        when (requestType) {
            is BalanceAccountCreation -> requestType.signingData
            is WithdrawalRequest -> requestType.signingData
            is ConversionRequest -> requestType.signingData
            is SignersUpdate -> requestType.signingData
            is WrapConversionRequest -> requestType.signingData
            is WalletConfigPolicyUpdate -> requestType.signingData
            is BalanceAccountSettingsUpdate -> requestType.signingData
            is DAppBookUpdate -> requestType.signingData
            is AddressBookUpdate -> requestType.signingData
            is BalanceAccountNameUpdate -> requestType.signingData
            is BalanceAccountPolicyUpdate -> requestType.signingData
            is BalanceAccountAddressWhitelistUpdate -> requestType.signingData
            is DAppTransactionRequest -> requestType.signingData
            is SignData -> requestType.signingData

            is LoginApprovalRequest, is AcceptVaultInvitation, is PasswordReset -> throw Exception(
                INVALID_REQUEST_APPROVAL
            )
            is UnknownApprovalType -> throw Exception(
                UNKNOWN_REQUEST_APPROVAL
            )
        }

    private fun generateTransactionInstructionData(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(opIndex))
        buffer.write(byteArrayOf(solanaProgramValue))
        buffer.write(Hash.sha256(opHashData()))
        return buffer.toByteArray()
    }

    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
        return when (requestType) {
            is LoginApprovalRequest ->
                requestType.jwtToken.toByteArray(charset = Charsets.UTF_8)
            is AcceptVaultInvitation ->
                requestType.vaultName.toByteArray(charset = Charsets.UTF_8)
            is PasswordReset ->
                requestId.toByteArray(charset = Charsets.UTF_8)
            else -> {
                if (approverPublicKey == null) throw Exception("MISSING KEY")

                val signingData = signingData()

                val nonce = nonces.firstOrNull()
                val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull()

                if (nonce == null || nonceAccountAddress == null) {
                    throw Exception("NOT ENOUGH NONCE ACCOUNTS")
                }

                val keyList = listOf(
                    AccountMeta(
                        publicKey = PublicKey(signingData.multisigOpAccountAddress),
                        isSigner = false,
                        isWritable = true
                    ),
                    AccountMeta(
                        publicKey = PublicKey(approverPublicKey),
                        isSigner = true,
                        isWritable = false
                    ),
                    AccountMeta(
                        publicKey = SYSVAR_CLOCK_PUBKEY,
                        isSigner = false,
                        isWritable = false
                    )
                )

                val programId = PublicKey(signingData.walletProgramId)

                val transactionMessage = Transaction.compileMessage(
                    feePayer = PublicKey(signingData.feePayer),
                    recentBlockhash = nonce.value,
                    instructions = listOf(
                        createAdvanceNonceInstruction(
                            nonceAccountAddress = nonceAccountAddress,
                            feePayer = signingData.feePayer
                        ),
                        TransactionInstruction(
                            keys = keyList,
                            programId = programId,
                            data = generateTransactionInstructionData()
                        )
                    )
                )

                transactionMessage.serialize()
            }
        }
    }

    fun convertToApiBody(
        encryptionManager: EncryptionManager,
        cipher: Cipher): RegisterApprovalDispositionBody {

        val privateKeyByteArray = encryptionManager.retrieveSavedKey(
            email = email, cipher = cipher
        )

        val privateKey = BaseWrapper.encode(privateKeyByteArray)

        val signature = try {
            encryptionManager.signApprovalDispositionMessage(
                signable = this,
                solanaKey = privateKey
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }

        val nonce = if(nonces.isEmpty()) "" else nonces.first().value
        val nonceAccountAddress = if(requestType.nonceAccountAddresses().isEmpty()) "" else requestType.nonceAccountAddresses().first()

        return RegisterApprovalDispositionBody(
            approvalDisposition = approvalDisposition,
            signature = signature,
            nonce = nonce,
            nonceAccountAddress = nonceAccountAddress
        )
    }

    inner class RegisterApprovalDispositionBody(
        val approvalDisposition: ApprovalDisposition,
        val nonce: String,
        val nonceAccountAddress: String,
        val signature: String
    )
}