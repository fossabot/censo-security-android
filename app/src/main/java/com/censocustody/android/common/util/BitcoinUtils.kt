package com.censocustody.android.common.util

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.MessageDigest


object BitcoinUtils {
    init {
        java.security.Security.addProvider(BouncyCastleProvider())
    }
    private val sha256 = MessageDigest.getInstance("SHA-256")

    fun sha256Hash160(input: ByteArray): ByteArray {
        val digest = RIPEMD160Digest()
        digest.update(sha256.digest(input), 0, 32)
        val outArray = ByteArray(20)
        digest.doFinal(outArray, 0)
        return outArray
    }

    fun addChecksum(input: ByteArray): ByteArray {
        val checksum = sha256.digest(sha256.digest(input)).slice(IntRange(0, 3))
        return input + checksum
    }

    fun verifyChecksum(input: ByteArray): ByteArray {
        val dataWithoutChecksum = input.slice(0 until input.size - 4).toByteArray()
        val inputWithRecalculatedChecksum = addChecksum(dataWithoutChecksum)
        if (!input.contentEquals(inputWithRecalculatedChecksum)) {
            throw Exception("Checksum failed")
        }
        return dataWithoutChecksum
    }
}
