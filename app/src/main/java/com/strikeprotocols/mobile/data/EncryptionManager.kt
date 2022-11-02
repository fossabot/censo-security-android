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
import com.strikeprotocols.mobile.data.models.SupplyDappInstruction
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
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
        email: String,
        cipher: Cipher? = null,
        privateKey: String? = null
    ): SignedPayload

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        cipher: Cipher? = null,
        privateKey: String? = null,
        childKeyIndex: Int
    ): List<SignedPayload>

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        privateKey: String? = null,
        cipher: Cipher? = null,
    ): List<SignedPayload>

    fun signEthereumApprovalDispositionMessage(
        signable: Signable,
        email: String,
        privateKey: String? = null,
        cipher: Cipher? = null,
    ): SignedPayload

    fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        email: String,
        privateKey: String? = null,
        cipher: Cipher? = null,
    ): String

    fun signatures(
        cipher: Cipher? = null,
        email: String,
        privateKey: String? = null,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String>

    fun signInitiationRequestData(
        signable: Signable,
        email: String,
        ephemeralPrivateKey: ByteArray,
        supplyInstructions: List<SupplyDappInstruction>,
        cipher: Cipher
    ): SignedInitiationData

    fun signDataWithSolanaEncryptedKey(
        data: ByteArray,
        userEmail: String,
        cipher: Cipher,
        keyType: String
    ): ByteArray

    fun signDataWithSolana(data: ByteArray, privateKey: ByteArray): ByteArray

    fun signKeyForMigration(rootSeed: ByteArray, publicKey: String) : ByteArray
    //endregion

    //region generic key work
    fun verifyData(data: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun verifySolanaKeyPair(
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

    fun retrieveSavedV2Key(
        email: String,
        cipher: Cipher,
        keyType: String = SOLANA_KEY
    ): ByteArray

    fun saveV3PublicKeys(rootSeed: ByteArray, email: String) : HashMap<String, String>

    fun generateSolanaPublicKeyFromRootSeed(rootSeed: ByteArray): String
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
    override fun signDataWithSolana(data: ByteArray, privateKey: ByteArray): ByteArray {
        try {
            val privateKeyParam = Ed25519PrivateKeyParameters(privateKey.inputStream())

            //create signature
            val signer = Ed25519Signer()
            signer.init(true, privateKeyParam)
            signer.update(data, NO_OFFSET_INDEX, data.size)

            val signature = signer.generateSignature()

            val publicKey = regenerateSolanaPublicKey(BaseWrapper.encode(privateKey))

            val validSignature = verifyData(data = data, signature = signature, publicKey = BaseWrapper.decode(publicKey))

            if (!validSignature) {
                throw Exception("Invalid signature")
            }

            return signature
        } catch (e: Exception) {
            throw SignDataException()
        }
    }

    override fun signKeyForMigration(rootSeed: ByteArray, publicKey: String): ByteArray {
        val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)

        return signDataWithSolana(
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
        signable: Signable, email: String, cipher: Cipher?, privateKey: String?,
    ): SignedPayload {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val solanaKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = SOLANA_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regenerateSolanaPublicKey(privateKey = BaseWrapper.encode(solanaKey))

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey).first()

        val signedData = signDataWithSolana(
            data = messageToSign, privateKey = solanaKey
        )

        return SignedPayload(
            signature = BaseWrapper.encodeToBase64(signedData),
            payload = BaseWrapper.encodeToBase64(messageToSign)
        )
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        cipher: Cipher?,
        privateKey: String?,
        childKeyIndex: Int,
    ): List<SignedPayload> {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val bitcoinKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = BITCOIN_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (bitcoinKey.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey
            .fromExtendedKey(BaseWrapper.encode(bitcoinKey))
            .derive(ChildPathNumber(childKeyIndex, false))

        return signable.retrieveSignableData(approverPublicKey = btcKey.getPublicKeyBytes().toHexString()).map {
            SignedPayload(
                signature = BaseWrapper.encodeToBase64(btcKey.signData(it)),
                payload = BaseWrapper.encodeToBase64(it)
            )
        }
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        privateKey: String?,
        cipher: Cipher?,
    ): List<SignedPayload> {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val bitcoinKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = BITCOIN_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (bitcoinKey.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey.fromExtendedKey(BaseWrapper.encode(bitcoinKey))

        val approverPublicKey = btcKey.getPublicKeyBytes().toHexString()

        return signable.retrieveSignableData(approverPublicKey = approverPublicKey).map {
                SignedPayload(
                    signature = BaseWrapper.encodeToBase64(btcKey.signData(it)),
                    payload = BaseWrapper.encodeToBase64(it)
                )
            }
    }

    override fun signEthereumApprovalDispositionMessage(
        signable: Signable,
        email: String,
        privateKey: String?,
        cipher: Cipher?,
    ): SignedPayload {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val ethereumKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = ETHEREUM_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (ethereumKey.isEmpty()) {
            throw NoKeyDataException
        }

        val ethKey = Secp256k1HierarchicalKey.fromExtendedKey(BaseWrapper.encode(ethereumKey))
        val payload = signable.retrieveSignableData(approverPublicKey = ethKey.getPublicKeyBytes().toHexString()).first()

        return SignedPayload(
            signature = BaseWrapper.encodeToBase64(ethKey.signData(payload)),
            payload = BaseWrapper.encodeToBase64(payload)
        )
    }

    override fun signApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        email: String,
        privateKey: String?,
        cipher: Cipher?,
    ): String {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val solanaKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = SOLANA_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regenerateSolanaPublicKey(privateKey = BaseWrapper.encode(solanaKey))

        val messageToSign = signable.retrieveSignableData(approverPublicKey = publicKey).first()

        val signedData = signDataWithSolana(
            data = messageToSign,
            privateKey = ephemeralPrivateKey
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signatures(
        cipher: Cipher?,
        email: String,
        privateKey: String?,
        signableSupplyInstructions: SignableSupplyInstructions
    ): List<String> {
        if (cipher == null && privateKey == null) {
            throw Exception("Need to pass either cipher or private key to sign data")
        }

        val solanaKey = if (privateKey == null) {
            retrieveSavedV3Key(
                email = email,
                cipher = cipher!!,
                keyType = SOLANA_KEY
            )
        } else {
            BaseWrapper.decode(privateKey)
        }

        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val publicKey = regenerateSolanaPublicKey(privateKey = BaseWrapper.encode(solanaKey))

        return signableSupplyInstructions.signableSupplyInstructions(approverPublicKey = publicKey)
            .map {
                val signature = signDataWithSolana(it, solanaKey)
                BaseWrapper.encodeToBase64(signature)
            }
            .toList()
    }

    override fun signInitiationRequestData(
        signable: Signable,
        email: String,
        ephemeralPrivateKey: ByteArray,
        supplyInstructions: List<SupplyDappInstruction>,
        cipher: Cipher
    ): SignedInitiationData {
        val solanaKey = retrieveSavedV3Key(
            email = email,
            cipher = cipher,
            keyType = StoredKeyData.SOLANA_KEY
        )

        if (solanaKey.isEmpty()) {
            throw NoKeyDataException
        }

        val initiatorSignature = try {
            signApprovalDispositionMessage(
                signable = signable,
                privateKey = BaseWrapper.encode(solanaKey),
                email = email
            ).signature
        } catch (e: Exception) {
            throw Exception("SIGNING_DATA_FAILURE")
        }

        val opAccountSignature = try {
            signApprovalInitiationMessage(
                ephemeralPrivateKey = ephemeralPrivateKey,
                signable = signable,
                privateKey = BaseWrapper.encode(solanaKey),
                email = email
            )
        } catch (e: Exception) {
            throw Exception("SIGNING_DATA_FAILURE")
        }

        val supplyDappInstruction =
            if (supplyInstructions.isNotEmpty()) {
                val supplyInstructionInitiatorSignatures = supplyInstructions.map { instruction ->
                    InitiationRequest.SupplyDappInstructionsTxSignature(
                        nonce = instruction.nonce.value,
                        nonceAccountAddress = instruction.nonceAccountAddress,
                        signature = signApprovalDispositionMessage(
                            signable = instruction,
                            privateKey = BaseWrapper.encode(solanaKey),
                            email = email,
                        ).signature
                    )
                }

                InitiationRequest.SupplyDAppInstructions(
                    supplyInstructionInitiatorSignatures = supplyInstructionInitiatorSignatures
                )
            } else {
                null
            }

        return SignedInitiationData(
            supplyDAppInstructions = supplyDappInstruction,
            initiatorSignature = initiatorSignature,
            opAccountSignature = opAccountSignature
        )
    }

    override fun signDataWithSolanaEncryptedKey(
        data: ByteArray,
        userEmail: String,
        cipher: Cipher,
        keyType: String
    ): ByteArray {
        val privateKey = retrieveSavedV3Key(email = userEmail, cipher = cipher, keyType = keyType)

        return signDataWithSolana(data = data, privateKey = privateKey)
    }

    override fun verifySolanaKeyPair(privateKey: String?, publicKey: String?, ): Boolean {
        if (privateKey.isNullOrEmpty() || publicKey.isNullOrEmpty()) {
            return false
        }

        try {
            val base58PublicKey = BaseWrapper.decode(publicKey)
            val privateKeyBytes = BaseWrapper.decode(privateKey)
            val signedData = signDataWithSolana(DATA_CHECK, privateKeyBytes)
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

    override fun generateSolanaPublicKeyFromRootSeed(rootSeed: ByteArray): String {
        val keys = createAllKeys(rootSeed)

        return BaseWrapper.encode(keys.solanaKey.publicKeyBytes)
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

    private fun retrieveSavedV3Key(
        email: String,
        cipher: Cipher,
        keyType: String
    ): ByteArray {
        val rootSeed = retrieveRootSeed(email = email, cipher = cipher)

        val keys = createAllKeys(BaseWrapper.decode(rootSeed))

        val keyBytes = when (keyType) {
            ETHEREUM_KEY -> keys.ethereumKey.getBase58ExtendedPrivateKey()
            SOLANA_KEY -> BaseWrapper.encode(keys.solanaKey.privateKeyBytes)
            BITCOIN_KEY -> keys.bitcoinKey.getBase58ExtendedPrivateKey()
            else -> throw Exception("Missing key for type")
        }

        return BaseWrapper.decode(keyBytes)
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
        securePreferences.hasV3RootSeed(email = email)

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

data class SignedInitiationData(
    val initiatorSignature: String,
    val opAccountSignature: String,
    val supplyDAppInstructions: InitiationRequest.SupplyDAppInstructions?
)