package com.censocustody.android.data.cryptography

interface CensoPrivateKey {
    fun getPublicKeyBytes(): ByteArray
    fun signData(data: ByteArray): ByteArray
    fun verifySignature(data: ByteArray, signature: ByteArray): Boolean
}