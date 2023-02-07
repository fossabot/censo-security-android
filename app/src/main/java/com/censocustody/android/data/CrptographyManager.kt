package com.censocustody.android.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.censocustody.android.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.security.*
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec
import java.util.UUID

interface CryptographyManager {

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE] is used.
     */
    fun getInitializedCipherForEncryption(keyName: String): Cipher

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE] is used.
     */
    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    fun getSignatureForDeviceSigning(keyName: String) : Signature

    /**
     * The Cipher created with [getInitializedCipherForEncryption] is used here
     */
    fun encryptData(data: String, cipher: Cipher): EncryptedData

    /**
     * The Cipher created with [getInitializedCipherForDecryption] is used here
     */
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): ByteArray

    fun createDeviceKeyId() : String

    fun deleteInvalidatedKey(keyName: String)
    fun deleteKeyIfPresent(keyName: String)
    fun signDataWithDeviceKey(data: ByteArray, keyName: String, signature: Signature): ByteArray
    fun createPublicDeviceKey(keyName: String): ByteArray
    fun getCertificateFromKeystore(deviceId: String): Certificate
    fun verifySignature(keyName: String, dataSigned: ByteArray, signatureToCheck: ByteArray): Boolean
}

data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray)

class CryptographyManagerImpl : CryptographyManager {

    companion object {
        const val KEY_SIZE: Int = 256
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val SHA_256_ECDSA = "SHA256withECDSA"
        const val SECP_256_R1 = "secp256r1"
        const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun getSignatureForDeviceSigning(keyName: String): Signature {
        val signature = Signature.getInstance(SHA_256_ECDSA)
        val deviceKey = getDeviceKey(keyName)
        signature.initSign(deviceKey)
        return signature
    }

    override fun verifySignature(keyName: String, dataSigned: ByteArray, signatureToCheck: ByteArray): Boolean {
        val signature = Signature.getInstance(SHA_256_ECDSA)
        val certificate = getCertificateFromKeystore(keyName)
        signature.initVerify(certificate)
        signature.update(dataSigned)
        return signature.verify(signatureToCheck)
    }

    override fun getInitializedCipherForDecryption(
        keyName: String,
        initializationVector: ByteArray
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    override fun encryptData(data: String, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(data.toByteArray())
        return EncryptedData(ciphertext, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): ByteArray {
        return cipher.doFinal(ciphertext)
    }

    override fun createDeviceKeyId() =
        UUID.randomUUID().toString().replace("-", "")

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
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

    override fun signDataWithDeviceKey(
        data: ByteArray,
        keyName: String,
        signature: Signature
    ): ByteArray {
        signature.update(data)
        return signature.sign()
    }

    override fun createPublicDeviceKey(keyName: String): ByteArray {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val paramBuilder = KeyGenParameterSpec.Builder(
            keyName, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )

        paramBuilder.apply {
            setAlgorithmParameterSpec(ECGenParameterSpec(SECP_256_R1))
            setDigests(
                KeyProperties.DIGEST_SHA256
            )
            setUserAuthenticationRequired(true)
            setInvalidatedByBiometricEnrollment(true)
            setRandomizedEncryptionRequired(true)
            setIsStrongBoxBacked(!BuildConfig.DEBUG)
        }

        val parameterSpec = paramBuilder.build()

        kpg.initialize(parameterSpec)

        val keyPair = kpg.generateKeyPair()

        return extractData(keyPair.public)
    }

    override fun getCertificateFromKeystore(deviceId: String): Certificate {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        val cert = keyStore.getCertificate(deviceId)
        return cert
    }

    private fun extractData(publicKey: PublicKey): ByteArray {
        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
        val encodedBytes = subjectPublicKeyInfo.publicKeyData.bytes
        val publicKeyData = ByteArray(encodedBytes.size - 1)
        System.arraycopy(encodedBytes, 1, publicKeyData, 0, encodedBytes.size - 1)
        return publicKeyData
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        val key = keyStore.getKey(keyName, null)
        if (key != null) return key as SecretKey

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
            setInvalidatedByBiometricEnrollment(true)
            setRandomizedEncryptionRequired(true)
            setIsStrongBoxBacked(!BuildConfig.DEBUG)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    private fun getDeviceKey(keyName: String): PrivateKey {
        // If PrivateKey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        val key = keyStore.getKey(keyName, null)
        //AndroidKeyStoreECPrivateKey
        return key as PrivateKey
    }
}