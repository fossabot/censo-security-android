package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.IV_AND_KEY_COMBINED_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.IV_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NO_OFFSET_INDEX
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

interface EncryptionManager {
    fun createKeyPair(): StrikeKeyPair
    fun signData(data: String, privateKey: String): ByteArray
    fun verifyData(data: String, signature: ByteArray, publicKey: String): Boolean
    fun encrypt(message: String, generatedPassword: ByteArray): String
    fun decrypt(encryptedMessage: String, generatedPassword: ByteArray): String
    fun generatePassword(): ByteArray
    fun verifyKeyPair(encryptedPrivateKey: String?, publicKey: String?, symmetricKey: String?): Boolean
}

class EncryptionManagerImpl : EncryptionManager {

    //region interface methods
    override fun signData(data: String, privateKey: String): ByteArray {
        val decodedData = BaseWrapper.decodeFromUTF8(data)
        val privateKeyByteArray = BaseWrapper.decode(privateKey)
        val privateKeyParam = Ed25519PrivateKeyParameters(privateKeyByteArray.inputStream())

        val signer = Ed25519Signer()
        signer.init(true, privateKeyParam)
        signer.update(decodedData, NO_OFFSET_INDEX, decodedData.size)

        return signer.generateSignature()
    }

    override fun verifyData(data: String, signature: ByteArray, publicKey: String): Boolean {
        val publicByteArray = BaseWrapper.decode(publicKey)
        val publicKeyParameter = Ed25519PublicKeyParameters(publicByteArray.inputStream())
        val decodedData = BaseWrapper.decodeFromUTF8(data)

        val verifier = Ed25519Signer()
        verifier.init(false, publicKeyParameter)
        verifier.update(decodedData, NO_OFFSET_INDEX, decodedData.size)

        return verifier.verifySignature(signature)
    }

    override fun createKeyPair(): StrikeKeyPair {
        val keyPairGenerator: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = (keyPair.private as Ed25519PrivateKeyParameters).encoded
        val publicKey = (keyPair.public as Ed25519PublicKeyParameters).encoded

        return StrikeKeyPair(privateKey = privateKey, publicKey = publicKey)
    }

    override fun encrypt(message: String, generatedPassword: ByteArray): String {
        val encrypted = getCipher(Cipher.ENCRYPT_MODE, generatedPassword)
            .doFinal(BaseWrapper.decodeFromUTF8(message))
        return BaseWrapper.encode(encrypted)
    }

    override fun decrypt(encryptedMessage: String, generatedPassword: ByteArray): String {
        val byteStr = BaseWrapper.decode(encryptedMessage)
        val decrypted = getCipher(Cipher.DECRYPT_MODE, generatedPassword).doFinal(byteStr)
        return BaseWrapper.encodeToUTF8(decrypted)
    }

    override fun generatePassword(): ByteArray =
        SecureRandom().generateSeed(IV_AND_KEY_COMBINED_LENGTH)

    override fun verifyKeyPair(
        encryptedPrivateKey: String?,
        publicKey: String?,
        symmetricKey: String?
    ): Boolean {
        if (encryptedPrivateKey.isNullOrEmpty()
            || publicKey.isNullOrEmpty() || symmetricKey.isNullOrEmpty()) {
            return false
        }

        val decryptionKey = BaseWrapper.decode(symmetricKey)
        val decryptedPrivateKey = decrypt(encryptedPrivateKey, decryptionKey)
        val signData = signData(DATA_CHECK, decryptedPrivateKey)
        return verifyData(DATA_CHECK, signData, publicKey)
    }
    //endregion

    //region helper methods
    private fun getCipher(opMode: Int, password: ByteArray): Cipher {

        if(password.size != IV_AND_KEY_COMBINED_LENGTH) {
            throw IllegalStateException("iv and key are not the correct size")
        }

        val ivParameter = password.slice(0.until(IV_LENGTH)).toByteArray()
        val key = password.slice(IV_LENGTH.until(password.size)).toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKeySpec = SecretKeySpec(key, "AES")

        cipher.init(opMode, secretKeySpec, IvParameterSpec(ivParameter))
        return cipher
    }
    //endregion

    //region companion
    object Companion {
        private const val PASSWORD_BYTE_LENGTH = 32
        const val IV_LENGTH = 12
        const val IV_AND_KEY_COMBINED_LENGTH = PASSWORD_BYTE_LENGTH + IV_LENGTH
        const val NO_OFFSET_INDEX = 0
        const val DATA_CHECK = "VerificationCheck"
    }
    //endregion
}

data class StrikeKeyPair(val privateKey: ByteArray, val publicKey: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StrikeKeyPair

        if (!privateKey.contentEquals(other.privateKey)) return false
        if (!publicKey.contentEquals(other.publicKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = privateKey.contentHashCode()
        result = 31 * result + publicKey.contentHashCode()
        return result
    }
}