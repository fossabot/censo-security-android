package com.strikeprotocols.mobile.data

interface StrikePrivateKey {
    fun getPublicKeyBytes(): ByteArray
    fun signData(data: ByteArray): ByteArray
    fun verifySignature(data: ByteArray, signature: ByteArray): Boolean
}