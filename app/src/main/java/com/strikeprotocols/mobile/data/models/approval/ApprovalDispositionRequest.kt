package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.INVALID_REQUEST_APPROVAL
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.Exception

class ApprovalDispositionRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val approvalDisposition: ApprovalDisposition,
    val requestType: SolanaApprovalRequestType,
    val blockhash: String,
    val email: String
) : Signable {

    private val opIndex: Byte = 9

    private val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    private val opHashData: ByteArray = requestType.serialize()

    private val transactionInstructionData: ByteArray = generateTransactionInstructionData()

    private fun signingData(): SolanaSigningData =
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

    private fun generateTransactionInstructionData(): ByteArray {
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
                publicKey = PublicKey(SYSVAR_CLOCK_PUBKEY),
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
            recentBlockHash = blockhash,
            signature = signature
        )
    }

    inner class RegisterApprovalDispositionBody(
        val approvalDisposition: ApprovalDisposition,
        val recentBlockHash: String,
        val signature: String
    )

    companion object {
        const val SYSVAR_CLOCK_PUBKEY = "SysvarC1ock11111111111111111111111111111111"
    }
}