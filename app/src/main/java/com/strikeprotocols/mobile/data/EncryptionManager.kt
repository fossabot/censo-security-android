package com.strikeprotocols.mobile.data


import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.AUTH_TAG_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.IV_LENGTH
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NO_OFFSET_INDEX
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

interface EncryptionManager {
    fun createKeyPair(): AsymmetricCipherKeyPair
    fun signData(data: String, privateKey: AsymmetricKeyParameter): ByteArray
    fun verifyData(data: String, signature: ByteArray, publicKey: AsymmetricKeyParameter): Boolean
    fun encrypt(message: String, generatedPassword: ByteArray): String
    fun decrypt(encryptedMessage: String, generatedPassword: ByteArray): String
    fun generatePassword(): ByteArray
}

class EncryptionManagerImpl : EncryptionManager {

    //region interface methods
    override fun signData(data: String, privateKey: AsymmetricKeyParameter): ByteArray {
        val decodedData = BaseWrapper.decodeFromUTF8(data)

        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(decodedData, NO_OFFSET_INDEX, decodedData.size)

        return signer.generateSignature()
    }

    override fun verifyData(data: String, signature: ByteArray, publicKey: AsymmetricKeyParameter): Boolean {
        val decodedData = BaseWrapper.decodeFromUTF8(data)

        val verifier = Ed25519Signer()
        verifier.init(false, publicKey)
        verifier.update(decodedData, NO_OFFSET_INDEX, decodedData.size)

        return verifier.verifySignature(signature)
    }

    override fun createKeyPair(): AsymmetricCipherKeyPair {
        val keyPairGenerator: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        return keyPairGenerator.generateKeyPair()
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
        SecureRandom().generateSeed(Companion.PASSWORD_BYTE_LENGTH)
    //endregion

    //region helper methods
    private fun getCipher(opMode: Int, password: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKeySpec = SecretKeySpec(password, "AES")

        //Another option could be:
        //cipher.init(opMode, secretKeySpec, IvParameterSpec(ByteArray(IV_LENGTH)))
        cipher.init(opMode, secretKeySpec, GCMParameterSpec(AUTH_TAG_LENGTH, ByteArray(IV_LENGTH)))
        return cipher
    }
    //endregion

    //region companion
    object Companion {
        const val PASSWORD_BYTE_LENGTH = 32
        const val IV_LENGTH = 12
        const val AUTH_TAG_LENGTH = 128
        const val NO_OFFSET_INDEX = 0
    }
    //endregion
}