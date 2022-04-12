package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.INVALID_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.Exception

data class ApprovalDispositionRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: SolanaApprovalRequestType,
    val blockhash: String,
    val email: String
) : Signable {

    val opIndex: Byte = 9

    val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    fun retrieveOpCode(): Byte {
        return when (requestType) {
            is SolanaApprovalRequestType.BalanceAccountCreation -> 1
            is SolanaApprovalRequestType.WithdrawalRequest, is SolanaApprovalRequestType.ConversionRequest -> 3
            is SolanaApprovalRequestType.SignersUpdate -> 5
            else -> 0
        }
    }

    val opHashData: ByteArray = when(requestType) {
        is SolanaApprovalRequestType.BalanceAccountCreation -> {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(requestType.signingData.walletAddress.base58Bytes())
            buffer.write(byteArrayOf(requestType.accountSlot))
            buffer.write(requestType.accountInfo.identifier.sha256HashBytes())
            buffer.write(byteArrayOf(requestType.approvalsRequired))
            buffer.writeLongLE(requestType.approvalTimeout.convertToSeconds())
            buffer.write(byteArrayOf(requestType.approvers.size.toByte()))
            buffer.write(requestType.approvers.flatMap { it.combinedBytes().toList() }.toByteArray())
            buffer.write(byteArrayOf(requestType.whitelistEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(requestType.dappsEnabled.toSolanaProgramValue()))
            buffer.write(byteArrayOf(requestType.addressBookSlot))

            buffer.toByteArray()
        }
        is SolanaApprovalRequestType.WithdrawalRequest -> {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(requestType.signingData.walletAddress.base58Bytes())
            buffer.write(requestType.account.identifier.sha256HashBytes())
            buffer.write(requestType.destination.address.base58Bytes())
            buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
            buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

            buffer.toByteArray()
        }
        is SolanaApprovalRequestType.ConversionRequest -> {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(requestType.account.identifier.sha256HashBytes())
            buffer.write(requestType.destination.address.base58Bytes())
            buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
            buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress.base58Bytes())

            buffer.toByteArray()
        }
        is SolanaApprovalRequestType.SignersUpdate -> {
            val buffer = ByteArrayOutputStream()

            buffer.write(byteArrayOf(retrieveOpCode()))
            buffer.write(requestType.signingData.walletAddress.base58Bytes())
            buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
            buffer.write(requestType.signer.combinedBytes())

            buffer.toByteArray()
        }
        is SolanaApprovalRequestType.DAppTransactionRequest -> throw Exception(
            INVALID_REQUEST_APPROVAL
        )
        is SolanaApprovalRequestType.UnknownApprovalType -> throw Exception(
            UNKNOWN_REQUEST_APPROVAL
        )
    }

    val transactionInstructionData: ByteArray = generateTransactionInstructionData()

    fun signingData(): SolanaSigningData =
        when (requestType) {
            is SolanaApprovalRequestType.BalanceAccountCreation -> requestType.signingData
            is SolanaApprovalRequestType.WithdrawalRequest -> requestType.signingData
            is SolanaApprovalRequestType.ConversionRequest -> requestType.signingData
            is SolanaApprovalRequestType.SignersUpdate -> requestType.signingData
            is SolanaApprovalRequestType.DAppTransactionRequest -> throw Exception(
                INVALID_REQUEST_APPROVAL
            )
            is SolanaApprovalRequestType.UnknownApprovalType -> throw Exception(
                UNKNOWN_REQUEST_APPROVAL
            )
        }

    fun generateTransactionInstructionData(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(opIndex))
        buffer.write(byteArrayOf(solanaProgramValue))
        buffer.write(Hash.sha256(opHashData))
        return buffer.toByteArray()
    }

    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
        if (approverPublicKey == null) throw Exception("Missing Key")

        val signingData = signingData()

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
            recentBlockhash = blockhash,
            instructions = listOf(
                TransactionInstruction(
                    keys = keyList,
                    programId = programId,
                    data = transactionInstructionData
                )
            )
        )

        return transactionMessage.serialize()
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

        return RegisterApprovalDispositionBody(
            approvalDisposition = approvalDisposition,
            recentBlockhash = blockhash,
            signature = signature
        )
    }

    inner class RegisterApprovalDispositionBody(
        val approvalDisposition: ApprovalDisposition,
        val recentBlockhash: String,
        val signature: String
    )
}