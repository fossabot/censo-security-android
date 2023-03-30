package com.censocustody.android.common

import org.bitcoinj.core.Base58
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

fun String.toParticipantIdAsBigInteger() = BigInteger(1, EcdsaUtils.getCompressedKeyBytesFromBase58(this, EcdsaUtils.r1Curve))

fun String.toParticipantIdAsHexString() = toParticipantIdAsBigInteger().toByteArrayNoSign(32).toHexString().lowercase()

fun String.toShareUserId() = this.sha256()
