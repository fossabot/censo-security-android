package com.strikeprotocols.mobile

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { throw Exception("Must be an even length") }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun generateEphemeralPrivateKeyFromText(keyValueAsHex: String) : Ed25519PrivateKeyParameters {
    val base58PrivateKey = keyValueAsHex.decodeHex()
    return Ed25519PrivateKeyParameters(base58PrivateKey.inputStream())
}