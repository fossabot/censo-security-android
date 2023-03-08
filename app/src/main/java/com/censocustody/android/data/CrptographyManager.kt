package com.censocustody.android.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.*
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec
import java.util.UUID

interface CryptographyManager {

    fun createDeviceKeyId(): String
    fun deleteInvalidatedKey(keyName: String)
    fun deleteKeyIfPresent(keyName: String)
    fun getCertificateFromKeystore(deviceId: String): Certificate
    fun verifySignature(
        keyName: String,
        dataSigned: ByteArray,
        signatureToCheck: ByteArray
    ): Boolean

    fun getOrCreateKey(keyName: String): PrivateKey
    fun getPublicKeyFromDeviceKey(keyName: String): PublicKey
    fun signData(keyName: String, dataToSign: ByteArray): ByteArray
    fun decryptData(keyName: String, ciphertext: ByteArray): ByteArray
    fun encryptDataLocal(keyName: String, plainText: String): ByteArray
}


class CryptographyManagerImpl : CryptographyManager {

    companion object {
        const val KEY_SIZE: Int = 256
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val SECP_256_R1 = "secp256r1"
        const val SHA_256_ECDSA = "SHA256withECDSA"
    }

    override fun createDeviceKeyId() =
        UUID.randomUUID().toString().replace("-", "")

    override fun signData(keyName: String, dataToSign: ByteArray): ByteArray {
        val key = getOrCreateKey(keyName)
        val signature = Signature.getInstance(SHA_256_ECDSA)
        signature.initSign(key)
        signature.update(dataToSign)
        return signature.sign()
    }

    override fun decryptData(keyName: String, ciphertext: ByteArray): ByteArray {
        val key = getOrCreateKey(keyName)

        return ECIESManager.decryptMessage(
            cipherData = ciphertext,
            privateKey = key
        )
    }

    override fun encryptDataLocal(
        keyName: String,
        plainText: String,
    ): ByteArray {

        val publicKey = getPublicKeyFromDeviceKey(keyName)

        val compressedKey = ECIESManager.extractUncompressedPublicKey(publicKey.encoded)

        return ECIESManager.encryptMessage(
            dataToEncrypt = plainText.toByteArray(Charsets.UTF_8),
            publicKeyBytes = compressedKey,
        )
    }

    override fun verifySignature(
        keyName: String,
        dataSigned: ByteArray,
        signatureToCheck: ByteArray
    ): Boolean {
        val signature = Signature.getInstance(SHA_256_ECDSA)
        val certificate = getCertificateFromKeystore(keyName)
        signature.initVerify(certificate)
        signature.update(dataSigned)
        return signature.verify(signatureToCheck)
    }

    override fun getOrCreateKey(keyName: String): PrivateKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val key = keyStore.getKey(keyName, null)
        if (key != null) return key as PrivateKey

        return createECDeviceKey(keyName)
    }

    override fun getPublicKeyFromDeviceKey(keyName: String): PublicKey {
        val certificate = getCertificateFromKeystore(keyName)
        return certificate.publicKey
    }

    override fun getCertificateFromKeystore(deviceId: String): Certificate {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        val cert = keyStore.getCertificate(deviceId)
        return cert
    }

    private fun createECDeviceKey(keyName: String): PrivateKey {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val paramBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY or KeyProperties.PURPOSE_AGREE_KEY
        )

        val parameterSpec = paramBuilder
            .setAlgorithmParameterSpec(ECGenParameterSpec(SECP_256_R1))
            .setKeySize(KEY_SIZE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(
                5, KeyProperties.AUTH_BIOMETRIC_STRONG
            )
            .setInvalidatedByBiometricEnrollment(true)
            .setDigests(
                KeyProperties.DIGEST_SHA256
            )
            .build()

        kpg.initialize(parameterSpec)

        return kpg.generateKeyPair().private
    }

    override fun deleteInvalidatedKey(keyName: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.deleteEntry(keyName)
    }

    override fun deleteKeyIfPresent(keyName: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val haveKey = keyStore.containsAlias(keyName)

        if (haveKey) {
            keyStore.deleteEntry(keyName)
        }
    }
}