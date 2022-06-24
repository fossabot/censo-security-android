package com.strikeprotocols.mobile.data

import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Ed25519HierarchicalPrivateKey
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NO_OFFSET_INDEX
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NoKeyDataException
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import com.strikeprotocols.mobile.data.EncryptionManagerException.*
import java.security.SecureRandom
import javax.inject.Inject

fun generateEphemeralPrivateKey(): Ed25519PrivateKeyParameters {
    val keyPairGenerator: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()
    keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
    val keyPair = keyPairGenerator.generateKeyPair()
    return keyPair.private as Ed25519PrivateKeyParameters
}

interface EncryptionManager {
    fun createKeyPair(phrase: String): StrikeKeyPair
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
    fun verifyKeyPair(
        privateKey: String?,
        publicKey: String?,
    ): Boolean
    fun generatePhrase(): String

    fun regeneratePublicKey(mainKey: String): String
}

class EncryptionManagerImpl @Inject constructor(private val securePreferences: SecurePreferences) :
    EncryptionManager {

    //region interface methods
    override fun signData(data: ByteArray, privateKey: ByteArray): ByteArray {
        try {
            val privateKeyParam = Ed25519PrivateKeyParameters(privateKey.inputStream())

            val signer = Ed25519Signer()
            signer.init(true, privateKeyParam)
            signer.update(data, NO_OFFSET_INDEX, data.size)

            return signer.generateSignature()
        } catch (e: Exception) {
            throw SignDataException()
        }
    }

    override fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean {
        try {
            val publicKeyParameter = Ed25519PublicKeyParameters(publicKey.inputStream())

            val verifier = Ed25519Signer()
            verifier.init(false, publicKeyParameter)
            verifier.update(data, NO_OFFSET_INDEX, data.size)

            return verifier.verifySignature(signature)
        } catch (e: Exception) {
            throw VerifyFailedException()
        }
    }

    override fun createKeyPair(phrase: String): StrikeKeyPair {
        try {
            val bip39PrivateKey = Ed25519HierarchicalPrivateKey.fromSeedPhrase(phrase)
            return StrikeKeyPair(
                privateKey = bip39PrivateKey.privateKeyBytes,
                publicKey = bip39PrivateKey.publicKeyBytes
            )
        } catch (e: Exception) {
            throw KeyPairGenerationFailedException()
        }
    }

    override fun signApprovalDispositionMessage(signable: Signable, userEmail: String): String {
        val mainKey = securePreferences.retrievePrivateKey(userEmail)

        if (mainKey.isEmpty()) {
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

    override fun verifyKeyPair(privateKey: String?, publicKey: String?, ): Boolean {
        if (privateKey.isNullOrEmpty() || publicKey.isNullOrEmpty()) {
            return false
        }

        try {
            val base58PublicKey = BaseWrapper.decode(publicKey)
            val privateKeyBytes = BaseWrapper.decode(privateKey)
            val signedData = signData(DATA_CHECK, privateKeyBytes)
            return verifyData(DATA_CHECK, signedData, base58PublicKey)
        } catch (e: Exception) {
            throw VerifyFailedException()
        }
    }

    override fun generatePhrase(): String =
        String(Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).chars)

    override fun regeneratePublicKey(mainKey: String): String {
        try {
            val base58PrivateKey = BaseWrapper.decode(mainKey)
            val privateKeyParam = Ed25519PrivateKeyParameters(base58PrivateKey.inputStream())

            val publicKey = privateKeyParam.generatePublicKey()

            return BaseWrapper.encode(publicKey.encoded)
        } catch (e: Exception) {
            throw PublicKeyRegenerationFailedException()
        }
    }
    //endregion

    //region companion
    object Companion {
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