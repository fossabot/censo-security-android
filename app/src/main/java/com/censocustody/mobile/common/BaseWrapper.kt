package com.censocustody.mobile.common

import org.bitcoinj.core.Base58
import java.util.*

object BaseWrapper {
    fun encode(byteArray: ByteArray): String = Base58.encode(byteArray)
    fun decode(string: String): ByteArray = Base58.decode(string)
    fun encodeToBase64(byteArray: ByteArray): String =
        Base64.getEncoder().encodeToString(byteArray)
    fun decodeFromBase64(string: String): ByteArray =
        Base64.getDecoder().decode(string)
}


fun ByteArray.toHexString(): String =
    joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
