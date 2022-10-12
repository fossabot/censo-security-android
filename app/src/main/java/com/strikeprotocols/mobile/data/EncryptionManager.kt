package com.strikeprotocols.mobile.data

import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.*
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
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.ETHEREUM_KEY
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

data class SignedPayload(
    val signature: String,
    val payload: String
)

interface EncryptionManager {

    //region sign data
    fun signApprovalDispositionMessage(
        signable: Signable,
        solanaKey: String
    ): SignedPayload

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        bitcoinKey: String,
        childKeyIndex: Int
    ): List<SignedPayload>

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        bitcoinKey: String
    ): List<SignedPayload>

    fun signEthereumApprovalDispositionMessage(
        signable: Signable,
        ethereumKey: String
    ): SignedPayload

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
        cipher: Cipher,
        keyType: String
    ): ByteArray

    fun signData(data: ByteArray, privateKey: ByteArray): ByteArray

    fun signKeyForMigration(rootSeed: ByteArray, publicKey: String) : ByteArray
    //endregion

    //region generic key work
    fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun verifyKeyPair(
        privateKey: String?,
        publicKey: String?,
    ): Boolean
    fun generatePhrase(): String
    fun regenerateSolanaPublicKey(privateKey: String): String
    //endregion

    //region save/retrieve key data
    fun havePrivateKeysStored(email: String): Boolean

    fun saveV3RootSeed(
        rootSeed: ByteArray,
        email: String,
        cipher: Cipher
    )

    fun retrieveRootSeed(email: String, cipher: Cipher): String

    fun saveV3PrivateKeys(
        rootSeed: ByteArray,
        email: String,
        cipher: Cipher
    )

    fun retrieveSavedV2Key(
        email: String,
        cipher: Cipher,
        keyType: String = SOLANA_KEY
    ): ByteArray

    fun retrieveSavedV3Key(
        email: String,
        cipher: Cipher,
        keyType: String = SOLANA_KEY
    ) : ByteArray

    fun saveV3PublicKeys(rootSeed: ByteArray, email: String) : HashMap<String, String>
    //endregion

    //region Work with device keystore

    fun deleteBiometryKeyFromKeystore(keyName: String)

    fun getInitializedCipherForEncryption(keyName: String) : Cipher
    fun getInitializedCipherForDecryption(keyName: String, initVector: ByteArray) : Cipher
    fun haveSentinelDataStored(email: String): Boolean
    fun saveSentinelData(email: String, cipher: Cipher)
    fun retrieveSentinelData(email: String, cipher: Cipher): String

    //endregion
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

    override fun signKeyForMigration(rootSeed: ByteArray, publicKey: String): ByteArray {
        val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)

        return signData(
            data = BaseWrapper.decode(publicKey),
            privateKey = solanaHierarchicalKey.privateKeyBytes
        )
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

    override fun signApprovalDispositionMessage(
        signable: Signable, solanaKey: String
    ): SignedPayload {
        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regenerateSolanaPublicKey(privateKey = solanaKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey).first()

        val signedData = signData(
            data = messageToSign, privateKey = BaseWrapper.decode(solanaKey)
        )

        return SignedPayload(
            signature = BaseWrapper.encodeToBase64(signedData),
            payload = BaseWrapper.encodeToBase64(messageToSign)
        )
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        bitcoinKey: String,
        childKeyIndex: Int
    ): List<SignedPayload> {
        if (bitcoinKey.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey.fromExtendedKey(bitcoinKey).derive(ChildPathNumber(childKeyIndex, false))

        return signable.retrieveSignableData(approverPublicKey = btcKey.getPublicKeyBytes().toHexString()).map {
            SignedPayload(
                signature = BaseWrapper.encodeToBase64(btcKey.signData(it)),
                payload = BaseWrapper.encodeToBase64(it)
            )
        }
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        bitcoinKey: String
    ): List<SignedPayload> {
        if (bitcoinKey.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey.fromExtendedKey(bitcoinKey)

        return signable.retrieveSignableData(approverPublicKey = btcKey.getPublicKeyBytes().toHexString()).map {
            SignedPayload(
                signature = BaseWrapper.encodeToBase64(btcKey.signData(it)),
                payload = BaseWrapper.encodeToBase64(it)
            )
        }
    }

    override fun signEthereumApprovalDispositionMessage(
        signable: Signable,
        ethereumKey: String
    ): SignedPayload {
        if (ethereumKey.isEmpty()) {
            throw NoKeyDataException
        }

        val ethKey = Secp256k1HierarchicalKey.fromExtendedKey(ethereumKey)
        val payload = signable.retrieveSignableData(approverPublicKey = ethKey.getPublicKeyBytes().toHexString()).first()

        return SignedPayload(
            signature = BaseWrapper.encodeToBase64(ethKey.signData(payload)),
            payload = BaseWrapper.encodeToBase64(payload)
        )
    }

    override fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        solanaKey: String
    ): String {
        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regenerateSolanaPublicKey(privateKey = solanaKey)

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey).first()

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

        val publicKey = regenerateSolanaPublicKey(privateKey = solanaKey)

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
        cipher: Cipher,
        keyType: String
    ): ByteArray {
        val privateKey = retrieveSavedV3Key(email = userEmail, cipher = cipher, keyType = keyType)

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

    override fun regenerateSolanaPublicKey(privateKey: String): String {
        try {
            val base58PrivateKey = BaseWrapper.decode(privateKey)
            val privateKeyParam = Ed25519PrivateKeyParameters(base58PrivateKey.inputStream())

            val publicKey = privateKeyParam.generatePublicKey()

            return BaseWrapper.encode(publicKey.encoded)
        } catch (e: Exception) {
            throw PublicKeyRegenerationFailedException()
        }
    }

    override fun saveV3PublicKeys(rootSeed: ByteArray, email: String) : HashMap<String, String> {
        val keys = createAllKeys(rootSeed)
        val solanaPublicKey = BaseWrapper.encode(keys.solanaKey.publicKeyBytes)
        val bitcoinPublicKey = keys.bitcoinKey.getBase58ExtendedPublicKey()
        val ethereumPublicKey = keys.ethereumKey.getBase58UncompressedPublicKey()

        val keyDataAsJson = createV3PublicKeyJson(
            solanaPublicKey = solanaPublicKey,
            bitcoinPublicKey = bitcoinPublicKey,
            ethereumPublicKey = ethereumPublicKey
        )

        securePreferences.saveV3PublicKeys(
            email = email,
            keyJson = keyDataAsJson
        )

        return hashMapOf(
            SOLANA_KEY to solanaPublicKey,
            BITCOIN_KEY to bitcoinPublicKey,
            ETHEREUM_KEY to ethereumPublicKey
        )
    }

    override fun saveV3PrivateKeys(rootSeed: ByteArray, email: String, cipher: Cipher) {
        val keys = createAllKeys(rootSeed)
        val solanaPrivateKey = BaseWrapper.encode(keys.solanaKey.privateKeyBytes)
        val bitcoinPrivateKey = keys.bitcoinKey.getBase58ExtendedPrivateKey()
        val ethereumPrivateKey = keys.ethereumKey.getBase58ExtendedPrivateKey()

        val keyDataAsJson = createV3StoredKeyDataAsJson(
            solanaKey = solanaPrivateKey,
            bitcoinKey = bitcoinPrivateKey,
            ethereumKey = ethereumPrivateKey,
            cipher = cipher
        )

        securePreferences.saveV3PrivateKeys(
            email = email,
            keyJson = keyDataAsJson
        )
    }

    private fun createV3StoredKeyDataAsJson(
        solanaKey: String,
        bitcoinKey: String,
        ethereumKey: String,
        cipher: Cipher,
    ): String {
        val mapOfKeys: HashMap<String, String> =
            hashMapOf(
                SOLANA_KEY to solanaKey,
                BITCOIN_KEY to bitcoinKey,
                ETHEREUM_KEY to ethereumKey
            )

        val jsonMapOfKeys = StoredKeyData.mapToJson(
            keyMap = mapOfKeys
        )

        val encryptedKeysData =
            cryptographyManager.encryptData(jsonMapOfKeys, cipher)

        val storedKeyData = StoredKeyData(
            initVector = BaseWrapper.encode(encryptedKeysData.initializationVector),
            encryptedKeysData = BaseWrapper.encode(encryptedKeysData.ciphertext)
        )

        return storedKeyData.toJson()
    }

    private fun createV3PublicKeyJson(
        solanaPublicKey: String,
        bitcoinPublicKey: String,
        ethereumPublicKey: String
    ): String {
        val mapOfKeys: HashMap<String, String> =
            hashMapOf(
                SOLANA_KEY to solanaPublicKey,
                BITCOIN_KEY to bitcoinPublicKey,
                ETHEREUM_KEY to ethereumPublicKey
            )

        return StoredKeyData.mapToJson(
            keyMap = mapOfKeys
        )
    }

    private fun createAllKeys(rootSeed: ByteArray): AllKeys {
        val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)

        //generate bitcoin keys from root seed
        val bitcoinHierarchicalKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed, Secp256k1HierarchicalKey.bitcoinDerivationPath
        )

        val ethereumHierarchicalKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed, Secp256k1HierarchicalKey.ethereumDerivationPath
        )

        return AllKeys(
            solanaKey = solanaHierarchicalKey,
            bitcoinKey = bitcoinHierarchicalKey,
            ethereumKey = ethereumHierarchicalKey
        )
    }

    override fun saveV3RootSeed(rootSeed: ByteArray, email: String, cipher: Cipher) {
        val encryptedRootSeed =
            cryptographyManager.encryptData(data = BaseWrapper.encode(rootSeed), cipher = cipher)

        securePreferences.saveV3RootSeed(email = email, encryptedData = encryptedRootSeed)
    }

    override fun retrieveSavedV2Key(
        email: String,
        cipher: Cipher,
        keyType: String
    ): ByteArray {
        val savedKey = securePreferences.retrieveV2RootSeedAndPrivateKey(email)

        val keysMap = retrieveStoredKeys(
            json = savedKey,
            cipher = cipher,
        )

        return BaseWrapper.decode(keysMap[keyType] ?: "")
    }

    override fun retrieveSavedV3Key(
        email: String,
        cipher: Cipher,
        keyType: String
    ): ByteArray {
        val savedKey = securePreferences.retrieveV3PrivateKeys(email)

        val keysMap = retrieveStoredKeys(
            json = savedKey,
            cipher = cipher,
        )

        return BaseWrapper.decode(keysMap[keyType] ?: "")
    }

    override fun retrieveSentinelData(email: String, cipher: Cipher): String {
        val savedSentinelData = securePreferences.retrieveSentinelData(email)

        val decryptedSentinelData = cryptographyManager.decryptData(
            ciphertext = savedSentinelData.ciphertext,
            cipher = cipher
        )

        return String(decryptedSentinelData, charset = Charset.forName("UTF-8"))
    }

    override fun retrieveRootSeed(email: String, cipher: Cipher): String {
        val savedRootSeedData = securePreferences.retrieveV3RootSeed(email)

        val decryptedRootSeed = cryptographyManager.decryptData(
            ciphertext = savedRootSeedData.ciphertext,
            cipher = cipher
        )

        return String(decryptedRootSeed, charset = Charset.forName("UTF-8"))
    }

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        return cryptographyManager.getInitializedCipherForEncryption(keyName)
    }

    override fun getInitializedCipherForDecryption(keyName: String, initVector: ByteArray): Cipher {
        return cryptographyManager.getInitializedCipherForDecryption(
            keyName = keyName, initializationVector = initVector
        )
    }

    override fun havePrivateKeysStored(email: String) =
        securePreferences.retrieveV3PrivateKeys(email = email).isNotEmpty()

    override fun haveSentinelDataStored(email: String) = securePreferences.hasSentinelData(email)

    override fun saveSentinelData(email: String, cipher: Cipher) {
        val encryptedSentinelData =
            cryptographyManager.encryptData(data = SENTINEL_STATIC_DATA, cipher = cipher)

        securePreferences.saveSentinelData(email = email, encryptedData = encryptedSentinelData)
    }

    override fun deleteBiometryKeyFromKeystore(keyName: String) {
        cryptographyManager.deleteInvalidatedKey(keyName)
    }

    private fun retrieveStoredKeys(
        json: String,
        cipher: Cipher,
    ): HashMap<String, String> {
        val storedKeyData = StoredKeyData.fromJson(json)

        val encryptedKeysData = storedKeyData.encryptedKeysData
        val decryptedKeyJson = cryptographyManager.decryptData(
            BaseWrapper.decode(encryptedKeysData),
            cipher
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
        const val SENTINEL_KEY_NAME = "sentinel_biometry_key"
        const val ROOT_SEED_KEY_NAME = "root_seed_encryption_key"
        const val PRIVATE_KEYS_KEY_NAME = "private_keys_key_name"
        const val SENTINEL_STATIC_DATA = "sentinel_static_data"
        const val NO_OFFSET_INDEX = 0
        val DATA_CHECK = BaseWrapper.decode("VerificationCheck")

        val NoKeyDataException = Exception("Unable to retrieve key data")
    }
    //endregion
}

data class AllKeys(
    val solanaKey : Ed25519HierarchicalPrivateKey,
    val bitcoinKey: Secp256k1HierarchicalKey,
    val ethereumKey: Secp256k1HierarchicalKey
)

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): List<ByteArray>
}

interface SignableSupplyInstructions {
    fun signableSupplyInstructions(approverPublicKey: String): List<ByteArray>
}