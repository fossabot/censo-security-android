package com.censocustody.android.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.ui.text.toLowerCase
import com.censocustody.android.BuildConfig
import com.censocustody.android.common.censoLog
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import java.security.KeyStore
import java.security.*
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

class ResetDeviceForKeyAccessException : Exception("Could not access device key")

interface CryptographyManager {

    fun createDeviceKeyId(): String
    fun deleteKeyIfPresent(keyName: String)
    fun getCertificateFromKeystore(deviceId: String): Certificate
    fun verifySignature(
        keyName: String,
        dataSigned: ByteArray,
        signatureToCheck: ByteArray
    ): Boolean

    fun getOrCreateKey(keyName: String): PrivateKey
    fun getOrCreateSentinelKey(email: String): SecretKey
    fun getPublicKeyFromDeviceKey(keyName: String): PublicKey
    fun signData(keyName: String, dataToSign: ByteArray): ByteArray
    fun decryptData(keyName: String, ciphertext: ByteArray): ByteArray
    fun encryptDataLocal(keyName: String, plainText: String): ByteArray

    fun encryptSentinelData(cipher: Cipher) : EncryptedData
    fun decryptSentinelData(ciphertext: ByteArray, cipher: Cipher) : ByteArray

    fun getInitializedCipherForSentinelEncryption(email: String): Cipher
    fun getInitializedCipherForSentinelDecryption(email: String, initializationVector: ByteArray, failedToAccessKey: () -> Unit): Cipher
}

class CryptographyManagerImpl : CryptographyManager {

    companion object {
        const val BIOMETRY_TIMEOUT = 5
        const val KEY_SIZE: Int = 256
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val SECP_256_R1 = "secp256r1"
        const val SHA_256_ECDSA = "SHA256withECDSA"

        //AES Specific
        const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }

    override fun createDeviceKeyId() =
        UUID.randomUUID().toString().replace("-", "")

    override fun signData(keyName: String, dataToSign: ByteArray): ByteArray {
        val key = getOrCreateKey(keyName)
        val signature = Signature.getInstance(SHA_256_ECDSA)
        signature.initSign(key)
        signature.update(dataToSign)
        val signedData =  signature.sign()
        return signedData
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

    override fun encryptSentinelData(cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(SENTINEL_STATIC_DATA.toByteArray(Charsets.UTF_8))
        return EncryptedData(ciphertext, cipher.iv)
    }

    override fun decryptSentinelData(ciphertext: ByteArray, cipher: Cipher): ByteArray {
        val decryptedData = cipher.doFinal(ciphertext)
        return decryptedData
    }

    override fun getInitializedCipherForSentinelEncryption(email: String): Cipher {
//        val timer = Timer()
//        timer.schedule(timerTask {
//            throw ResetDeviceForKeyAccessException()
//        }, 5000)
        val cipher = getAESCipher()
        val secretKey = getOrCreateSentinelKey(email)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        //timer.cancel()
        return cipher
    }

    override fun getInitializedCipherForSentinelDecryption(
        email: String,
        initializationVector: ByteArray,
        failedToAccessKey: () -> Unit
    ): Cipher {
        censoLog(message = "Trying to get sentinel key for $email")
        val cipher = getAESCipher()
        censoLog(message = "Got cipher: $cipher")
        val secretKey = getOrCreateSentinelKey(email)
        censoLog(message = "Retrieved key: $secretKey")

        val timer = Timer()
        timer.schedule(timerTask {
            failedToAccessKey()
        }, 5000)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))

        timer.cancel()

        censoLog(message = "Initialized key: $secretKey")
        return cipher
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
        val verified = signature.verify(signatureToCheck)
        return verified
    }

    override fun getOrCreateSentinelKey(email: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val key = keyStore.getKey("${email.lowercase().trim()}_$SENTINEL_KEY_NAME", null)
        if (key != null) return key as SecretKey
        val newKey = createAESKey(SENTINEL_KEY_NAME)
        return newKey
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
                BIOMETRY_TIMEOUT, KeyProperties.AUTH_BIOMETRIC_STRONG
            )
            .setInvalidatedByBiometricEnrollment(true)
            .setDigests(
                KeyProperties.DIGEST_SHA256
            )
            .setIsStrongBoxBacked(true)
            .build()

        kpg.initialize(parameterSpec)

        return kpg.generateKeyPair().private
    }

    private fun createAESKey(keyName: String): SecretKey {
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
            setIsStrongBoxBacked(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    override fun deleteKeyIfPresent(keyName: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val haveKey = keyStore.containsAlias(keyName)

        if (haveKey) {
            keyStore.deleteEntry(keyName)
        }
    }

    private fun getAESCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }
}

data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}