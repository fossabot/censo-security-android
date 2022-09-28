package com.strikeprotocols.mobile.common

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.MessageDigest

fun ByteArray.toPaddedHexString(length: Int) = joinToString("") { "%02X".format(it) }.padStart(length, '0')
fun ByteArray.pad(length: Int): ByteArray = Hex.decode(this.toPaddedHexString(length * 2))
fun BigInteger.toByteArrayNoSign(len: Int): ByteArray {
    val byteArray = this.toByteArray()
    return when {
        byteArray.size == len + 1 && byteArray[0].compareTo(0) == 0 -> byteArray.slice(IntRange(1, byteArray.size - 1)).toByteArray()
        byteArray.size < len -> byteArray.pad(len)
        else -> byteArray
    }
}

object BitcoinUtils {
    init {
        java.security.Security.addProvider(BouncyCastleProvider())
    }
    private val sha256 = MessageDigest.getInstance("SHA-256")
    private val rmd = MessageDigest.getInstance("RipeMD160")

    fun sha256Hash160(input: ByteArray): ByteArray {
        return rmd.digest(sha256.digest(input))
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
