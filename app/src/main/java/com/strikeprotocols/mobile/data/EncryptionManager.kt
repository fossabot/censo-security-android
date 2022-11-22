package com.strikeprotocols.mobile.data

import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Ed25519HierarchicalPrivateKey
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.NoKeyDataException
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import com.strikeprotocols.mobile.data.models.SupplyDappInstruction
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.Signature
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
    fun signSolanaApprovalDispositionMessage(
        signable: Signable,
        email: String,
        cipher: Cipher? = null,
        rootSeed: String? = null
    ): SignedPayload

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        cipher: Cipher? = null,
        rootSeed: String? = null,
        childKeyIndex: Int
    ): List<SignedPayload>

    fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        rootSeed: String? = null,
        cipher: Cipher? = null,
    ): List<SignedPayload>

    fun signEthereumApprovalDispositionMessage(
        signable: Signable,
        email: String,
        rootSeed: String? = null,
        cipher: Cipher? = null,
    ): SignedPayload

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
        cipher: Cipher
    ): ByteArray

    fun signKeyForMigration(rootSeed: ByteArray, publicKey: String): ByteArray
    //endregion

    //region generic key work
    fun generatePhrase(): String
    //endregion

    //region save/retrieve key data
    fun haveARootSeedStored(email: String): Boolean

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

    fun saveV3PublicKeys(rootSeed: ByteArray, email: String): HashMap<String, String>

    fun generateSolanaPublicKeyFromRootSeed(rootSeed: ByteArray): String
    //endregion

    //region Work with device keystore

    fun deleteBiometryKeyFromKeystore(keyName: String)

    fun getInitializedCipherForEncryption(keyName: String): Cipher
    fun getSignatureForDeviceSigning(keyName: String) : Signature
    fun getInitializedCipherForDecryption(keyName: String, initVector: ByteArray): Cipher
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
    override fun signKeyForMigration(rootSeed: ByteArray, publicKey: String): ByteArray {
        val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)
        return solanaHierarchicalKey.signData(BaseWrapper.decode(publicKey))
    }

    override fun signSolanaApprovalDispositionMessage(
        signable: Signable, email: String, cipher: Cipher?, rootSeed: String?,
    ): SignedPayload {
        if (cipher == null && rootSeed == null) {
            throw Exception("Need to pass either cipher or root seed to sign data")
        }

        val safeRootSeed = rootSeed ?: retrieveRootSeed(email = email, cipher = cipher!!)

        if (safeRootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val ed25519HierarchicalPrivateKey =
            Ed25519HierarchicalPrivateKey.fromRootSeed(BaseWrapper.decode(safeRootSeed))

        return generateSignedPayload(
            strikePrivateKey = ed25519HierarchicalPrivateKey,
            signable = signable
        )
    }

    private fun generateSignedPayload(strikePrivateKey: StrikePrivateKey, signable: Signable): SignedPayload {
        val messageToSign =
            signable.retrieveSignableData(
                approverPublicKey = BaseWrapper.encode(strikePrivateKey.getPublicKeyBytes())
            ).first()

        val signedData = strikePrivateKey.signData(messageToSign)

        return SignedPayload(
            signature = BaseWrapper.encodeToBase64(signedData),
            payload = BaseWrapper.encodeToBase64(messageToSign)
        )
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        cipher: Cipher?,
        rootSeed: String?,
        childKeyIndex: Int,
    ): List<SignedPayload> {
        if (cipher == null && rootSeed == null) {
            throw Exception("Need to pass either cipher or root seed to sign data")
        }

        val safeRootSeed = rootSeed ?: retrieveRootSeed(email = email, cipher = cipher!!)

        if (safeRootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey
            .fromRootSeed(
                rootSeed = BaseWrapper.decode(safeRootSeed),
                pathList = Secp256k1HierarchicalKey.bitcoinDerivationPath
            ).derive(ChildPathNumber(childKeyIndex, false))

        return signable.retrieveSignableData(
            approverPublicKey = btcKey.getPublicKeyBytes().toHexString()
        ).map {
            SignedPayload(
                signature = BaseWrapper.encodeToBase64(btcKey.signData(it)),
                payload = BaseWrapper.encodeToBase64(it)
            )
        }
    }

    override fun signBitcoinApprovalDispositionMessage(
        signable: Signable,
        email: String,
        rootSeed: String?,
        cipher: Cipher?,
    ): List<SignedPayload> {
        if (cipher == null && rootSeed == null) {
            throw Exception("Need to pass either cipher or root seed to sign data")
        }

        val safeRootSeed = rootSeed ?: retrieveRootSeed(email = email, cipher = cipher!!)

        if (safeRootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val btcKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed = BaseWrapper.decode(safeRootSeed),
            pathList = Secp256k1HierarchicalKey.bitcoinDerivationPath
        )

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
        rootSeed: String?,
        cipher: Cipher?,
    ): SignedPayload {
        if (cipher == null && rootSeed == null) {
            throw Exception("Need to pass either cipher or root seed to sign data")
        }

        val safeRootSeed = rootSeed ?: retrieveRootSeed(email = email, cipher = cipher!!)

        if (safeRootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val ethKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed = BaseWrapper.decode(safeRootSeed),
            pathList = Secp256k1HierarchicalKey.ethereumDerivationPath
        )

        return generateSignedPayload(
            strikePrivateKey = ethKey,
            signable = signable
        )
    }

    private fun signSolanaApprovalInitiationMessage(
        ephemeralPrivateKey: ByteArray,
        signable: Signable,
        email: String,
        rootSeed: String? = null,
        cipher: Cipher? = null,
    ): String {
        if (cipher == null && rootSeed == null) {
            throw Exception("Need to pass either cipher or root seed to sign data")
        }

        val safeRootSeed = rootSeed ?: retrieveRootSeed(email = email, cipher = cipher!!)

        if (safeRootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val ed25519HierarchicalPrivateKey =
            Ed25519HierarchicalPrivateKey.fromRootSeed(BaseWrapper.decode(safeRootSeed))

        val messageToSign =
            signable.retrieveSignableData(
                approverPublicKey = BaseWrapper.encode(ed25519HierarchicalPrivateKey.getPublicKeyBytes())
            ).first()

        val signedData = Ed25519HierarchicalPrivateKey.signDataWithKeyProvided(
            data = messageToSign,
            privateKey = ephemeralPrivateKey
        )

        return BaseWrapper.encodeToBase64(signedData)
    }

    override fun signInitiationRequestData(
        signable: Signable,
        email: String,
        ephemeralPrivateKey: ByteArray,
        supplyInstructions: List<SupplyDappInstruction>,
        cipher: Cipher
    ): SignedInitiationData {
        val rootSeed = retrieveRootSeed(
            email = email,
            cipher = cipher,
        )

        if (rootSeed.isEmpty()) {
            throw NoKeyDataException
        }

        val initiatorSignature = try {
            signSolanaApprovalDispositionMessage(
                signable = signable,
                rootSeed = rootSeed,
                email = email
            ).signature
        } catch (e: Exception) {
            throw Exception("SIGNING_DATA_FAILURE")
        }

        val opAccountSignature = try {
            signSolanaApprovalInitiationMessage(
                ephemeralPrivateKey = ephemeralPrivateKey,
                signable = signable,
                rootSeed = rootSeed,
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
                        signature = signSolanaApprovalDispositionMessage(
                            signable = instruction,
                            rootSeed = rootSeed,
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
    ): ByteArray {
        val rootSeed = retrieveRootSeed(email = userEmail, cipher = cipher)
        val ed25519HierarchicalPrivateKey =
            Ed25519HierarchicalPrivateKey.fromRootSeed(BaseWrapper.decode(rootSeed))

        return ed25519HierarchicalPrivateKey.signData(data = data)
    }

    override fun generatePhrase(): String =
        String(Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).chars)

    override fun saveV3PublicKeys(rootSeed: ByteArray, email: String): HashMap<String, String> {
        val keys = createAllKeys(rootSeed)
        val solanaPublicKey = BaseWrapper.encode(keys.solanaKey.getPublicKeyBytes())
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

        return BaseWrapper.encode(keys.solanaKey.getPublicKeyBytes())
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

        listOf(
            solanaHierarchicalKey,
            bitcoinHierarchicalKey,
            ethereumHierarchicalKey
        ).forEach {
            it.signData(DATA_CHECK)
        }

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

    override fun getSignatureForDeviceSigning(keyName: String): Signature {
        return cryptographyManager.getSignatureForDeviceSigning(keyName)
    }

    override fun getInitializedCipherForDecryption(keyName: String, initVector: ByteArray): Cipher {
        return cryptographyManager.getInitializedCipherForDecryption(
            keyName = keyName, initializationVector = initVector
        )
    }

    override fun haveARootSeedStored(email: String) =
        securePreferences.userHasV1RootSeedStored(email) || securePreferences.userHasV2RootSeedStored(email)
                || securePreferences.hasV3RootSeed(email = email)

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
    val solanaKey: Ed25519HierarchicalPrivateKey,
    val bitcoinKey: Secp256k1HierarchicalKey,
    val ethereumKey: Secp256k1HierarchicalKey
)

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): List<ByteArray>
}

data class SignedInitiationData(
    val initiatorSignature: String,
    val opAccountSignature: String,
    val supplyDAppInstructions: InitiationRequest.SupplyDAppInstructions?
)