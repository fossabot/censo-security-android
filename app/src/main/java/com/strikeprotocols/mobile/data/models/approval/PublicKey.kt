package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.Base58
import org.bouncycastle.math.ec.rfc8032.Ed25519
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PublicKey(val bytes: ByteArray) {
    companion object {
        const val SIZE = 32

        fun deserialize(byteBuffer: ByteBuffer): PublicKey {
            val bytes = ByteArray(SIZE)
            byteBuffer.get(bytes)
            return deserialize(bytes)
        }

        fun deserialize(bytes: ByteArray): PublicKey {
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            val key = PublicKey(ByteArray(SIZE))
            buffer.get(key.bytes)
            return key
        }

        fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
            val buffer = ByteArrayOutputStream()
            for (seed in seeds) {
                require(seed.size <= 32) { "Max seed length exceeded" }
                buffer.write(seed)
            }
            buffer.write(programId.toByteArray())
            buffer.write("ProgramDerivedAddress".toByteArray())
            val hash = Hash.sha256(buffer.toByteArray())
            if (Ed25519.validatePublicKeyPartial(hash, 0)) {
                throw RuntimeException("Invalid seeds, address must fall off the curve")
            }
            return PublicKey(hash)
        }

        fun findProgramAddress(
            seeds: List<ByteArray>,
            programId: PublicKey
        ): ProgramDerivedAddress {
            var nonce = 255
            val seedsWithNonce: MutableList<ByteArray> = ArrayList()
            seedsWithNonce.addAll(seeds)
            while (nonce != 0) {
                val address = try {
                    seedsWithNonce.add(byteArrayOf(nonce.toByte()))
                    createProgramAddress(seedsWithNonce, programId)
                } catch (e: Exception) {
                    seedsWithNonce.removeAt(seedsWithNonce.size - 1)
                    nonce--
                    continue
                }
                return ProgramDerivedAddress(address, nonce)
            }
            throw RuntimeException("Unable to find a viable program address nonce")
        }

        val empty = PublicKey("11111111111111111111111111111111")
    }

    init {
        require(bytes.size <= SIZE) { "Invalid public key input" }
    }

    constructor(pubkeyString: String) : this(Base58.decode(pubkeyString))

    fun toBase58(): String {
        return Base58.encode(bytes)
    }

    override fun toString(): String {
        return toBase58()
    }

    fun toByteArray(): ByteArray {
        return bytes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKey

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    val isEmpty: Boolean
        get() = this == empty
}

data class ProgramDerivedAddress(
    val address: PublicKey,
    val nonce: Int
)
