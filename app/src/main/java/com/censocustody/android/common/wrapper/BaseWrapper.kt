package com.censocustody.android.common.wrapper

import org.bitcoinj.core.Base58
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

object BaseWrapper {
    fun encode(byteArray: ByteArray): String = Base58.encode(byteArray)
    fun decode(string: String): ByteArray = Base58.decode(string)
    fun encodeToBase64(byteArray: ByteArray): String =
        Base64.getEncoder().encodeToString(byteArray)
    fun decodeFromBase64(string: String): ByteArray =
        Base64.getDecoder().decode(string)
}

fun BigInteger.toHexString(): String {
    return this.toByteArrayNoSign().toHexString().lowercase()
}
fun ByteArray.toHexString(): String =
    joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

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

fun BigInteger.toByteArrayNoSign(): ByteArray {
    val byteArray = this.toByteArray()
    return if (byteArray[0].compareTo(0) == 0) {
        byteArray.slice(IntRange(1, byteArray.size - 1)).toByteArray()
    } else byteArray
}

fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}

fun String.toParticipantIdAsBigInteger(): BigInteger {
    val bytes = Base58.decode(this)
    return BigInteger(
        1,
        when (bytes.size) {
            32 -> bytes
            64 -> bytes.slice(0..31).toByteArray()
            33, 65 -> bytes.slice(1..32).toByteArray()
            else -> throw Exception(":Invalid key")
        }
    )
}

fun String.toParticipantIdAsHexString() = toParticipantIdAsBigInteger().toByteArrayNoSign(32).toHexString().lowercase()

fun String.toShareUserId() = this.sha256()
