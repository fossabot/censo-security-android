package com.strikeprotocols.mobile.data

import androidx.biometric.BiometricPrompt
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Ed25519HierarchicalPrivateKey
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
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.ROOT_SEED
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.inject.Inject

fun generateEphemeralPrivateKey(): Ed25519PrivateKeyParameters {
    val keyPairGenerator: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()
    keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
    val keyPair = keyPairGenerator.generateKeyPair()
    return keyPair.private as Ed25519PrivateKeyParameters
}

interface EncryptionManager {
    fun createKeyPair(mnenomic: Mnemonics.MnemonicCode): StrikeKeyPair
    fun signApprovalDispositionMessage(
        signable: Signable,
        solanaKey: String
    ): String

    fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        solanaKey: String
    ): String

    fun signatures(
        solanaKey: String,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String>

    fun signDataWithEncryptedKey(
        data: ByteArray,
        userEmail: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): ByteArray

    fun signData(data: ByteArray, privateKey: ByteArray): ByteArray
    fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun verifyKeyPair(
        privateKey: String?,
        publicKey: String?,
    ): Boolean
    fun generatePhrase(): String

    fun regeneratePublicKey(privateKey: String): String

    fun saveKeyInformation(
        privateKey: ByteArray,
        publicKey: ByteArray,
        rootSeed: ByteArray,
        email: String,
        cryptoObject: BiometricPrompt.CryptoObject
    )

    fun retrieveSavedKey(
        email: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): ByteArray

    fun createStoredKeyDataAsJson(
        solanaKey: String,
        rootSeed: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String

    fun retrieveStoredKeys(
        json: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): HashMap<String, String>

    fun deleteBiometryKeyFromKeystore()

    fun getInitializedCipherForEncryption() : Cipher
    fun getInitializedCipherForDecryption(initVector: ByteArray) : Cipher
    fun havePrivateKeyStored(email: String): Boolean
}

class EncryptionManagerImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val cryptographyManager: CryptographyManager
) : EncryptionManager {

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

    override fun createKeyPair(mnenomic: Mnemonics.MnemonicCode): StrikeKeyPair {
        try {
            val rootSeed = mnenomic.toSeed()
            val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)

            return StrikeKeyPair(
                privateKey = solanaHierarchicalKey.privateKeyBytes,
                publicKey = solanaHierarchicalKey.publicKeyBytes,
                rootSeed = rootSeed
            )
        } catch (e: Exception) {
            throw KeyPairGenerationFailedException()
        }
    }

    override fun signApprovalDispositionMessage(
        signable: Signable, solanaKey: String
    ): String {
        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(privateKey = solanaKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey)

        val signedData = signData(
            data = messageToSign, privateKey = BaseWrapper.decode(solanaKey)
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        solanaKey: String
    ): String {
        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(privateKey = solanaKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey)

        val signedData = signData(
            data = messageToSign,
            privateKey = ephemeralPrivateKey
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signatures(
        solanaKey: String,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String> {
        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regeneratePublicKey(privateKey = solanaKey)

        return signableSupplyInstructions.signableSupplyInstructions(approverPublicKey = publicKey)
            .map {
                val signature = signData(it, BaseWrapper.decode(solanaKey))
                BaseWrapper.encodeToBase64(signature)
            }
            .toList()
    }

    override fun signDataWithEncryptedKey(
        data: ByteArray,
        userEmail: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): ByteArray {
        val privateKey = retrieveSavedKey(email = userEmail, cryptoObject = cryptoObject)

        return signData(data = data, privateKey = privateKey)
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

    override fun regeneratePublicKey(privateKey: String): String {
        try {
            val base58PrivateKey = BaseWrapper.decode(privateKey)
            val privateKeyParam = Ed25519PrivateKeyParameters(base58PrivateKey.inputStream())

            val publicKey = privateKeyParam.generatePublicKey()

            return BaseWrapper.encode(publicKey.encoded)
        } catch (e: Exception) {
            throw PublicKeyRegenerationFailedException()
        }
    }

    override fun saveKeyInformation(
        privateKey: ByteArray, publicKey: ByteArray, rootSeed: ByteArray,
        email: String, cryptoObject: BiometricPrompt.CryptoObject
    ) {
        val keyDataAsJson = createStoredKeyDataAsJson(
            solanaKey = BaseWrapper.encode(privateKey),
            rootSeed = BaseWrapper.encode(rootSeed),
            cryptoObject = cryptoObject,
        )

        securePreferences.saveAllRelevantKeyData(
            email = email,
            publicKey = publicKey,
            keyStorageJson = keyDataAsJson
        )
    }

    override fun retrieveSavedKey(
        email: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): ByteArray {
        val savedKey = securePreferences.retrieveEncryptedStoredKeys(email)

        val keysMap = retrieveStoredKeys(
            json = savedKey,
            cryptoObject = cryptoObject
        )

        return BaseWrapper.decode(keysMap[SOLANA_KEY] ?: "")
    }

    override fun getInitializedCipherForEncryption(): Cipher {
        return cryptographyManager.getInitializedCipherForEncryption(BIO_KEY_NAME)
    }

    override fun getInitializedCipherForDecryption(initVector: ByteArray): Cipher {
        return cryptographyManager.getInitializedCipherForDecryption(
            keyName = BIO_KEY_NAME, initializationVector = initVector
        )
    }

    override fun havePrivateKeyStored(email: String) =
        securePreferences.retrieveEncryptedStoredKeys(email = email).isNotEmpty()

    override fun deleteBiometryKeyFromKeystore() {
        cryptographyManager.deleteInvalidatedKey(BIO_KEY_NAME)
    }

    override fun createStoredKeyDataAsJson(
        solanaKey: String,
        rootSeed: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String {
        val mapOfKeys: HashMap<String, String> =
            hashMapOf(
                SOLANA_KEY to solanaKey,
                ROOT_SEED to rootSeed
            )

        val jsonMapOfKeys = StoredKeyData.mapToJson(
            keyMap = mapOfKeys
        )

        val encryptedKeysData =
            cryptographyManager.encryptData(jsonMapOfKeys, cryptoObject.cipher!!)

        val storedKeyData = StoredKeyData(
            initVector = BaseWrapper.encode(encryptedKeysData.initializationVector),
            encryptedKeysData = BaseWrapper.encode(encryptedKeysData.ciphertext)
        )

        return storedKeyData.toJson()
    }

    override fun retrieveStoredKeys(
        json: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): HashMap<String, String> {
        val storedKeyData = StoredKeyData.fromJson(json)

        val encryptedKeysData = storedKeyData.encryptedKeysData
        val decryptedKeyJson = cryptographyManager.decryptData(
            BaseWrapper.decode(encryptedKeysData),
            cryptoObject.cipher!!
        )

        //this is coming out as UTF-8 because we cannot store JSON in Base58
        val keys =
            StoredKeyData.mapFromJson(String(decryptedKeyJson, charset = Charset.forName("UTF-8")))

        return keys
    }
    //endregion

    //region companion
    object Companion {
        const val BIO_KEY_NAME = "biometric_encryption_key"
        const val NO_OFFSET_INDEX = 0
        val DATA_CHECK = BaseWrapper.decode("VerificationCheck")

        val NoKeyDataException = Exception("Unable to retrieve key data")
    }
    //endregion
}

data class StrikeKeyPair(
    val privateKey: ByteArray,
    val publicKey: ByteArray,
    val rootSeed: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StrikeKeyPair

        if (!privateKey.contentEquals(other.privateKey)) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!rootSeed.contentEquals(other.rootSeed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = privateKey.contentHashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + rootSeed.contentHashCode()
        return result
    }
}

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): ByteArray
}

interface SignableSupplyInstructions {
    fun signableSupplyInstructions(approverPublicKey: String): List<ByteArray>
}