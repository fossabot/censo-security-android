package com.strikeprotocols.mobile.common

import java.util.*

object BaseWrapper {
    fun encode(byteArray: ByteArray): String = Base58.encode(byteArray)
    fun decode(string: String): ByteArray = Base58.decode(string)
    fun decodeFromUTF8(string: String): ByteArray = string.toByteArray(Charsets.UTF_8)
    fun encodeToUTF8(byteArray: ByteArray) : String = byteArray.toString(Charsets.UTF_8)
    fun encodeToBase64(byteArray: ByteArray): String =
        Base64.getEncoder().encodeToString(byteArray)
}
