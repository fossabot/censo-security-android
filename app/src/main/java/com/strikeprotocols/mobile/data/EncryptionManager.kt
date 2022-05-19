package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.IV_AND_KEY_COMBINED_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.IV_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NO_OFFSET_INDEX
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NoKeyDataException
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

fun generateEphemeralPrivateKey() : Ed25519PrivateKeyParameters {
    val keyPairGenerator: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()
    keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
    val keyPair = keyPairGenerator.generateKeyPair()
    return keyPair.private as Ed25519PrivateKeyParameters
}

interface EncryptionManager {
    fun createKeyPair(): StrikeKeyPair
    fun signApprovalDispositionMessage(
        signable: Signable,
        userEmail: String
    ): String
    fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        userEmail: String
    ): String
    fun signatures(
        userEmail: String,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String>
    fun signData(data: ByteArray, privateKey: ByteArray): ByteArray
    fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun encrypt(message: ByteArray, generatedPassword: ByteArray): ByteArray
    fun decrypt(encryptedMessage: ByteArray, generatedPassword: ByteArray): ByteArray
    fun generatePassword(): ByteArray
    fun verifyKeyPair(encryptedPrivateKey: String?, publicKey: String?, symmetricKey: String?): Boolean
    fun regeneratePublicKey(mainKey: String): String
}

class EncryptionManagerImpl @Inject constructor(private val securePreferences: SecurePreferences) : EncryptionManager {

    //region interface methods
    override fun signData(data: ByteArray, privateKey: ByteArray): ByteArray {
        val privateKeyParam = Ed25519PrivateKeyParameters(privateKey.inputStream())

        val signer = Ed25519Signer()
        signer.init(true, privateKeyParam)
        signer.update(data, NO_OFFSET_INDEX, data.size)

        return signer.generateSignature()
    }

    override fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        val publicKeyParameter = Ed25519PublicKeyParameters(publicKey.inputStream())

        val verifier = Ed25519Signer()
        verifier.init(false, publicKeyParameter)
        verifier.update(data, NO_OFFSET_INDEX, data.size)

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

    override fun signApprovalDispositionMessage(signable: Signable, userEmail: String): String {
        val mainKey = securePreferences.retrievePrivateKey(userEmail)

        if(mainKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(mainKey = mainKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey)

        val signedData = signData(
            data = messageToSign, privateKey = BaseWrapper.decode(mainKey)
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        userEmail: String
    ): String {
        val privateKey = securePreferences.retrievePrivateKey(userEmail)
        if (privateKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(mainKey = privateKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey)

        val signedData = signData(
            data = messageToSign,
            privateKey = ephemeralPrivateKey
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signatures(
        userEmail: String,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String> {
        val mainKey = securePreferences.retrievePrivateKey(userEmail)

        if (mainKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(mainKey = mainKey)

        return signableSupplyInstructions.signableSupplyInstructions(approverPublicKey = publicKey)
            .map {
                val signature = signData(it, BaseWrapper.decode(mainKey))
                BaseWrapper.encodeToBase64(signature)
            }
            .toList()
    }

    override fun encrypt(message: ByteArray, generatedPassword: ByteArray): ByteArray {
        return getCipher(Cipher.ENCRYPT_MODE, generatedPassword).doFinal(message)
    }

    override fun decrypt(encryptedMessage: ByteArray, generatedPassword: ByteArray): ByteArray {
        return getCipher(Cipher.DECRYPT_MODE, generatedPassword).doFinal(encryptedMessage)
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
        val base58EncryptedPrivateKey = BaseWrapper.decode(encryptedPrivateKey)
        val base58PublicKey = BaseWrapper.decode(publicKey)
        val decryptedPrivateKey = decrypt(base58EncryptedPrivateKey, decryptionKey)
        val signData = signData(DATA_CHECK, decryptedPrivateKey)
        return verifyData(DATA_CHECK, signData, base58PublicKey)
    }

    override fun regeneratePublicKey(mainKey: String): String {
        val base58PrivateKey = BaseWrapper.decode(mainKey)
        val privateKeyParam = Ed25519PrivateKeyParameters(base58PrivateKey.inputStream())

        val publicKey = privateKeyParam.generatePublicKey()

        return BaseWrapper.encode(publicKey.encoded)
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

        cipher.init(opMode, secretKeySpec, GCMParameterSpec(128, ivParameter))
        return cipher
    }
    //endregion

    //region companion
    object Companion {
        private const val PASSWORD_BYTE_LENGTH = 32
        const val IV_LENGTH = 12
        const val IV_AND_KEY_COMBINED_LENGTH = PASSWORD_BYTE_LENGTH + IV_LENGTH
        const val NO_OFFSET_INDEX = 0
        val DATA_CHECK = BaseWrapper.decode("VerificationCheck")

        val NoKeyDataException = Exception("Unable to retrieve key data")
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

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): ByteArray
}

interface SignableSupplyInstructions {
    fun signableSupplyInstructions(approverPublicKey: String): List<ByteArray>
}