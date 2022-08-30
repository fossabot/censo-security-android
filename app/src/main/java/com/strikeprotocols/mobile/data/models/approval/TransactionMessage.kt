package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.BaseWrapper
import java.io.ByteArrayOutputStream

data class TransactionMessage(
    val accountKeys: List<AccountMeta>,
    val instructions: List<TransactionInstruction>,
    val recentBlockhash: String
) {
    fun serialize() : ByteArray {
        val header = encodeHeader()

        val accountKeys = encodeAccountKeys()

        val recentBlockhash = encodeRecentBlockHash()

        val compiledInstruction = encodeInstructions()

        val buffer = ByteArrayOutputStream()

        buffer.write(header)
        buffer.write(accountKeys)
        buffer.write(recentBlockhash)
        buffer.write(compiledInstruction)

        return buffer.toByteArray()
    }

    private fun encodeHeader() : ByteArray {
        val header = Header()
        for (meta in accountKeys) {
            if (meta.isSigner) {
                header.numRequiredSignatures += 1

                if (!meta.isWritable) {
                    header.numReadonlySignedAccounts += 1
                }
            } else {
                if (!meta.isWritable) {
                    header.numReadonlyUnsignedAccounts += 1
                }
            }
        }

        val buffer = ByteArrayOutputStream()
        buffer.write(header.bytes())
        return buffer.toByteArray()
    }

    private fun encodeAccountKeys() : ByteArray {
        val buffer = ByteArrayOutputStream()

        val keyLength = ShortvecEncoding.encodeLength(accountKeys.size)

        val signedKeys = accountKeys.filter { it.isSigner }
        val unsignedKeys = accountKeys.filter { !it.isSigner }
        val accountKeys = signedKeys + unsignedKeys

        buffer.write(keyLength)
        for (meta in accountKeys) {
            buffer.write(meta.publicKey.bytes)
        }

        return buffer.toByteArray()
    }

    private fun encodeRecentBlockHash() : ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(BaseWrapper.decode(recentBlockhash))
        return buffer.toByteArray()
    }

    private fun encodeInstructions() : ByteArray {
        val buffer = ByteArrayOutputStream()
        val compiledInstructions = mutableListOf<CompiledInstruction>()

        for (instruction in instructions) {
            val keySize = instruction.keys.size

            val keyIndicesBuffer = ByteArrayOutputStream()

            for (i in 0 until keySize) {
                val index = accountKeys.indexOfKey(instruction.keys[i].publicKey)
                if (index != -1) {
                    keyIndicesBuffer.write(index)
                }
            }

            val programIdIndex = accountKeys.indexOfKey(instruction.programId)

            val compiledInstruction = CompiledInstruction(
                programIdIndex = programIdIndex.toByte(),
                keyIndicesCount = ShortvecEncoding.encodeLength(keySize),
                keyIndices = keyIndicesBuffer.toByteArray(),
                dataLength = ShortvecEncoding.encodeLength(instruction.data.size),
                data = instruction.data
            )

            compiledInstructions.add(compiledInstruction)
        }

        val instructionLength = ShortvecEncoding.encodeLength(compiledInstructions.size)

        buffer.write(instructionLength)

        val compiledInstructionBuffer = ByteArrayOutputStream()

        for (compiledInstruction in compiledInstructions) {
            val compiledData = compiledInstruction.serializedData()
            compiledInstructionBuffer.write(compiledData)
        }

        buffer.write(compiledInstructionBuffer.toByteArray())

        return buffer.toByteArray()
    }
}

data class Header(
    var numRequiredSignatures: Int = 0,
    var numReadonlySignedAccounts: Int = 0,
    var numReadonlyUnsignedAccounts: Int = 0
) {
    fun bytes() =
        byteArrayOf(
            numRequiredSignatures.toByte(),
            numReadonlySignedAccounts.toByte(),
            numReadonlyUnsignedAccounts.toByte()
        )

    companion object {
        const val LENGTH = 3
    }
}

data class CompiledInstruction(
    val programIdIndex: Byte,
    val keyIndicesCount: ByteArray,
    val keyIndices: ByteArray,
    val dataLength: ByteArray,
    val data: ByteArray
) {

    fun length() =
        1 + keyIndicesCount.size.toByte() + keyIndices.size.toByte() + dataLength.size.toByte() + data.size.toByte()

    fun serializedData() : ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(programIdIndex))
        buffer.write(keyIndicesCount)
        buffer.write(keyIndices)
        buffer.write(dataLength)
        buffer.write(data)

        return buffer.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompiledInstruction

        if (programIdIndex != other.programIdIndex) return false
        if (!keyIndicesCount.contentEquals(other.keyIndicesCount)) return false
        if (!keyIndices.contentEquals(other.keyIndices)) return false
        if (!dataLength.contentEquals(other.dataLength)) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = programIdIndex.toInt()
        result = 31 * result + keyIndicesCount.contentHashCode()
        result = 31 * result + keyIndices.contentHashCode()
        result = 31 * result + dataLength.contentHashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}