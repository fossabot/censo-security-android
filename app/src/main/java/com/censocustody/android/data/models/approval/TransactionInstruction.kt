package com.censocustody.android.data.models.approval

import com.censocustody.android.data.models.approval.PublicKey.Companion.RECENT_BLOCKHASHES_SYSVAR_ID
import java.io.ByteArrayOutputStream

data class TransactionInstruction(
    val programId: PublicKey,
    val keys: List<AccountMeta>,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionInstruction

        if (programId != other.programId) return false
        if (keys != other.keys) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = programId.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    object Companion {
        fun createAdvanceNonceInstruction(
            nonceAccountAddress: String,
            feePayer: String
        ): TransactionInstruction {
            val buffer = ByteArrayOutputStream()
            buffer.writeIntLE(4)
            val dataAsArray = buffer.toByteArray()
            return TransactionInstruction(
                keys = listOf(
                    AccountMeta(
                        publicKey = PublicKey(nonceAccountAddress),
                        isSigner = false,
                        isWritable = true
                    ),
                    AccountMeta(
                        publicKey = RECENT_BLOCKHASHES_SYSVAR_ID,
                        isSigner = false,
                        isWritable = false
                    ),
                    AccountMeta(
                        publicKey = PublicKey(feePayer),
                        isSigner = true,
                        isWritable = false
                    )
                ),
                programId = PublicKey.SYS_PROGRAM_ID,
                data = dataAsArray
            )
        }
    }
}