package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.models.approval.*
import com.strikeprotocols.mobile.data.models.approval.PublicKey
import com.strikeprotocols.mobile.data.models.approval.Transaction.compileMessage
import com.strikeprotocols.mobile.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction

class SupplyDappInstruction(
    val nonce: Nonce,
    val nonceAccountAddress: String,
    val instructionBatch: SolanaInstructionBatch,
    val signingData: SolanaSigningData,
    val opAccountPublicKey: PublicKey,
    val walletAccountPublicKey: PublicKey,
) : Signable {

    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
        if (approverPublicKey == null) throw Exception("MISSING KEY")

        val keyList = listOf(
            AccountMeta(publicKey = opAccountPublicKey, isSigner = false, isWritable = true),
            AccountMeta(publicKey = walletAccountPublicKey, isSigner = false, isWritable = false),
            AccountMeta(
                publicKey = PublicKey(approverPublicKey),
                isSigner = true, isWritable = false
            )
        )

        return compileMessage(
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
                    data = instructionBatch.combinedBytes()
                )
            )
        ).serialize()
    }

}