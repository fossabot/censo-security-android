package com.censocustody.android.data.models

import com.censocustody.android.data.Signable
import com.censocustody.android.data.models.approval.*
import com.censocustody.android.data.models.approval.PublicKey
import com.censocustody.android.data.models.approval.Transaction.compileMessage
import com.censocustody.android.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction

class SupplyDappInstruction(
    val nonce: Nonce,
    val nonceAccountAddress: String,
    val instructionChunk: SolanaInstructionChunk,
    val signingData: SigningData.SolanaSigningData,
    val opAccountPublicKey: PublicKey,
    val walletAccountPublicKey: PublicKey,
) : Signable {

    override fun retrieveSignableData(approverPublicKey: String?): List<ByteArray> {
        if (approverPublicKey == null) throw Exception("MISSING KEY")

        val keyList = listOf(
            AccountMeta(publicKey = opAccountPublicKey, isSigner = false, isWritable = true),
            AccountMeta(publicKey = walletAccountPublicKey, isSigner = false, isWritable = false),
            AccountMeta(
                publicKey = PublicKey(approverPublicKey),
                isSigner = true, isWritable = false
            )
        )

        return listOf(compileMessage(
            feePayer = PublicKey(signingData.feePayer),
            recentBlockhash = nonce.value,
            instructions = listOf(
                createAdvanceNonceInstruction(
                    nonceAccountAddress = nonceAccountAddress,
                    feePayer = signingData.feePayer
                ),
                TransactionInstruction(
                    keys = keyList,
                    programId = PublicKey(signingData.walletProgramId),
                    data = instructionChunk.combinedBytes()
                )
            )
        ).serialize())
    }

}