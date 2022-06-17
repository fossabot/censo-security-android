package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.ApprovalConstants.MISSING_KEY
import com.strikeprotocols.mobile.data.models.approval.ApprovalConstants.NOT_ENOUGH_NONCE_ACCOUNTS
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import kotlin.Exception
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.INVALID_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*

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
            is SPLTokenAccountCreation -> 13
            is BalanceAccountAddressWhitelistUpdate -> 14

            is LoginApprovalRequest,
            is UnknownApprovalType -> 0
        }
    }

    fun opHashData(): ByteArray {
        val commonBytes = signingData().commonOpHashBytes()

        return when (requestType) {
            is BalanceAccountCreation -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is WithdrawalRequest -> {
                val buffer = ByteArrayOutputStream()

                val opAndCommonBuffer = ByteArrayOutputStream()
                opAndCommonBuffer.write(byteArrayOf(retrieveOpCode()))
                opAndCommonBuffer.write(commonBytes)
                buffer.write(opAndCommonBuffer.toByteArray())

                buffer.write(requestType.signingData.walletAddress.base58Bytes())
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

                buffer.write(requestType.signingData.walletAddress.base58Bytes())
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
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
                buffer.write(requestType.signer.combinedBytes())

                buffer.toByteArray()
            }

            is WrapConversionRequest -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(byteArrayOf(requestType.symbolAndAmountInfo.symbolInfo.getSOLProgramValue()))

                buffer.toByteArray()
            }
            is WalletConfigPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.approvalPolicy.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountSettingsUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is DAppBookUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is AddressBookUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountNameUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountAddressWhitelistUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())
                buffer.toByteArray()
            }
            is SPLTokenAccountCreation -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.signingData.walletAddress.base58Bytes())
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }

            is DAppTransactionRequest -> {
                val buffer = ByteArrayOutputStream()
                val hashBytesBuffer = ByteArrayOutputStream()
                hashBytesBuffer.write(byteArrayOf(retrieveOpCode()))
                hashBytesBuffer.write(commonBytes)
                hashBytesBuffer.write(requestType.signingData.walletAddress.base58Bytes())
                hashBytesBuffer.write(requestType.account.identifier.sha256HashBytes())
                hashBytesBuffer.write(requestType.dappInfo.address.base58Bytes())
                hashBytesBuffer.write(requestType.dappInfo.name.sha256HashBytes())
                hashBytesBuffer.writeShortLE(requestType.instructions.sumOf { it.instructions.size }.toShort())

                var hashBytes = hashBytesBuffer.toByteArray().sha256HashBytes()

                for (instructionBatch in requestType.instructions) {
                    for (instruction in instructionBatch.instructions) {
                        val instructionBuffer = ByteArrayOutputStream()
                        instructionBuffer.write(hashBytes)
                        instructionBuffer.write(instruction.combinedBytes())
                        val instructionBytes = instructionBuffer.toByteArray()
                        hashBytes = instructionBytes.sha256HashBytes()
                    }
                }
                buffer.write(hashBytes)
                buffer.toByteArray()
            }
            is LoginApprovalRequest -> throw Exception(
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
            is SPLTokenAccountCreation -> requestType.signingData
            is BalanceAccountAddressWhitelistUpdate -> requestType.signingData
            is DAppTransactionRequest -> requestType.signingData

            is LoginApprovalRequest -> throw Exception(
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
        buffer.write(
            when (requestType) {
                is DAppTransactionRequest -> opHashData()
                else -> Hash.sha256(opHashData())
            }
        )
        return buffer.toByteArray()
    }

    //todo: make this string non nullable
    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
        if (requestType is LoginApprovalRequest) {
            return requestType.jwtToken.toByteArray(charset = Charsets.UTF_8)
        } else {
            if (approverPublicKey == null) throw Exception(MISSING_KEY)

            val signingData = signingData()

            val nonce = nonces.firstOrNull()
            val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull()

            if (nonce == null || nonceAccountAddress == null) {
                throw Exception(NOT_ENOUGH_NONCE_ACCOUNTS)
            }

            val keyList = listOf(
                AccountMeta(
                    publicKey = PublicKey(signingData.multisigOpAccountAddress),
                    isSigner = false,
                    isWritable = true
                ),
                AccountMeta(
                    publicKey = PublicKey(approverPublicKey), isSigner = true, isWritable = false
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

            return transactionMessage.serialize()
        }
    }

    fun convertToApiBody(encryptionManager: EncryptionManager): RegisterApprovalDispositionBody {
        val signature = try {
            encryptionManager.signApprovalDispositionMessage(
                signable = this,
                userEmail = email
            )
        } catch (e: Exception) {
            throw Exception(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
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