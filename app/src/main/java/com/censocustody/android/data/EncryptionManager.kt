package com.censocustody.android.data

import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.common.*
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.EncryptionManagerImpl.Companion.DATA_CHECK
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.censocustody.android.data.models.StoredKeyData
import com.censocustody.android.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.OFFCHAIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.approvalV2.ApprovalSignature
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
    fun signKeysWithCensoKey(rootSeed: ByteArray, publicKey: String): ByteArray

    fun signKeysForUpload(
        email: String,
        signature: Signature,
        walletSigners: List<WalletSigner>
    ): ByteArray

    fun signDataWithDeviceKey(
        data: ByteArray,
        signature: Signature,
        email: String
    ) : ByteArray

    fun signApprovalDispositionForDeviceKey(
        email: String,
        signature: Signature,
        dataToSign: SignableDataResult.Device
    ) : ApprovalSignature.OffChainSignature

    fun signApprovalDisposition(
        email: String,
        cipher: Cipher,
        dataToSign: List<SignableDataResult>
    ) : List<ApprovalSignature>
    //endregion

    //region generic key work
    fun generatePhrase(): String
    //endregion

    //region save/retrieve key data
    fun saveV3RootSeed(
        rootSeed: ByteArray,
        email: String,
        cipher: Cipher
    )

    fun retrieveRootSeed(email: String, cipher: Cipher): String

    fun saveV3PublicKeys(rootSeed: ByteArray, email: String): HashMap<String, String>
    //endregion

    //region Work with device keystore

    fun deleteBiometryKeyFromKeystore(keyName: String)
    fun deleteKeyIfInKeystore(keyName: String)

    fun getInitializedCipherForEncryption(keyName: String): Cipher
    fun getInitializedCipherForDecryption(keyName: String, initVector: ByteArray): Cipher
    fun haveSentinelDataStored(email: String): Boolean
    fun saveSentinelData(email: String, cipher: Cipher)
    fun retrieveSentinelData(email: String, cipher: Cipher): String
    fun getSignatureForDeviceSigning(keyName: String) : Signature
    fun publicKeysFromRootSeed(rootSeed: ByteArray): HashMap<String, String>

    //endregion
}

class EncryptionManagerImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val cryptographyManager: CryptographyManager
) : EncryptionManager {

    //region interface methods
    override fun signKeysWithCensoKey(rootSeed: ByteArray, publicKey: String): ByteArray {
        val keys = createAllKeys(rootSeed)
        return keys.censoKey.signData(BaseWrapper.decode(publicKey))
    }

    override fun signKeysForUpload(
        email: String,
        signature: Signature,
        walletSigners: List<WalletSigner>
    ): ByteArray {
        val dataToSign = Signers.retrieveDataToSign(walletSigners)

        return signDataWithDeviceKey(
            signature = signature,
            data = dataToSign,
            email = email
        )
    }

    override fun signDataWithDeviceKey(
        data: ByteArray,
        signature: Signature,
        email: String
    ): ByteArray {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)
        return cryptographyManager.signDataWithDeviceKey(
            signature = signature,
            data = data,
            keyName = deviceId
        )
    }

    override fun signApprovalDispositionForDeviceKey(
        email: String,
        signature: Signature,
        dataToSign: SignableDataResult.Device
    ): ApprovalSignature.OffChainSignature {
        val signedData = signDataWithDeviceKey(
            data = dataToSign.dataToSign, signature = signature, email = email
        )

        return ApprovalSignature.OffChainSignature(
            signature = BaseWrapper.encodeToBase64(signedData),
            signedData = BaseWrapper.encodeToBase64(dataToSign.dataToSign)
        )
    }

    override fun signApprovalDisposition(
        email: String,
        cipher: Cipher,
        dataToSign: List<SignableDataResult>
    ): List<ApprovalSignature> {
        val rootSeed = BaseWrapper.decode(retrieveRootSeed(email = email, cipher = cipher))
        val keys = createAllKeys(rootSeed)

        return dataToSign.mapNotNull { signable ->
            when (signable) {
                is SignableDataResult.Bitcoin -> {
                    val bitcoinSignedData = signable.dataToSign.map {
                        val signedData =
                            keys.bitcoinKey
                                .derive(
                                    path = ChildPathNumber(
                                        index = signable.childKeyIndex,
                                        hardened = false
                                    )
                                )
                                .signData(it)
                        BaseWrapper.encodeToBase64(signedData)
                    }
                    ApprovalSignature.BitcoinSignatures(bitcoinSignedData)
                }
                is SignableDataResult.Ethereum -> {
                    val signedData = keys.ethereumKey.signData(signable.dataToSign)
                    val signature = BaseWrapper.encodeToBase64(signedData)

                    ApprovalSignature.EthereumSignature(
                        signature = signature,
                        offchainSignature = signable.offchain?.let {
                            val censoSignedData = keys.censoKey.signData(it.dataToSign)
                            val censoSignature = BaseWrapper.encodeToBase64(censoSignedData)

                            ApprovalSignature.OffChainSignature(
                                signedData = BaseWrapper.encodeToBase64(it.dataToSend),
                                signature = censoSignature
                            )
                        }
                    )
                }
                is SignableDataResult.Offchain -> {
                    val signedData = keys.censoKey.signData(signable.dataToSign)
                    val signature = BaseWrapper.encodeToBase64(signedData)
                    ApprovalSignature.OffChainSignature(
                        signedData = BaseWrapper.encodeToBase64(signable.dataToSend),
                        signature = signature
                    )
                }
                is SignableDataResult.Device -> null
                is SignableDataResult.Polygon -> {
                    val signedData = keys.ethereumKey.signData(signable.dataToSign)
                    val signature = BaseWrapper.encodeToBase64(signedData)

                    ApprovalSignature.PolygonSignature(
                        signature = signature,
                        offchainSignature = signable.offchain?.let {
                            val censoSignedData = keys.censoKey.signData(it.dataToSign)
                            val censoSignature = BaseWrapper.encodeToBase64(censoSignedData)

                            ApprovalSignature.OffChainSignature(
                                signedData = BaseWrapper.encodeToBase64(it.dataToSend),
                                signature = censoSignature
                            )
                        }
                    )
                }
            }
        }
    }

    override fun generatePhrase(): String =
        String(Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).chars)

    override fun saveV3PublicKeys(rootSeed: ByteArray, email: String): HashMap<String, String> {
        val keys = createAllKeys(rootSeed)
        val bitcoinPublicKey = keys.bitcoinKey.getBase58ExtendedPublicKey()
        val ethereumPublicKey = keys.ethereumKey.getBase58UncompressedPublicKey()
        val censoPublicKey = keys.censoKey.getBase58UncompressedPublicKey()

        val keyDataAsJson = createV3PublicKeyJson(
            bitcoinPublicKey = bitcoinPublicKey,
            ethereumPublicKey = ethereumPublicKey,
            censoPublicKey = censoPublicKey
        )

        securePreferences.saveV3PublicKeys(
            email = email,
            keyJson = keyDataAsJson
        )

        return hashMapOf(
            BITCOIN_KEY to bitcoinPublicKey,
            ETHEREUM_KEY to ethereumPublicKey,
            OFFCHAIN_KEY to censoPublicKey
        )
    }

    private fun createV3PublicKeyJson(
        bitcoinPublicKey: String,
        ethereumPublicKey: String,
        censoPublicKey: String
    ): String {
        val mapOfKeys: HashMap<String, String> =
            hashMapOf(
                BITCOIN_KEY to bitcoinPublicKey,
                ETHEREUM_KEY to ethereumPublicKey,
                OFFCHAIN_KEY to censoPublicKey
            )

        return StoredKeyData.mapToJson(
            keyMap = mapOfKeys
        )
    }

    private fun createAllKeys(rootSeed: ByteArray): AllKeys {
        val bitcoinHierarchicalKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed, Secp256k1HierarchicalKey.bitcoinDerivationPath
        )

        val ethereumHierarchicalKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed, Secp256k1HierarchicalKey.ethereumDerivationPath
        )

        val censoHierarchicalKey = Secp256k1HierarchicalKey.fromRootSeed(
            rootSeed, Secp256k1HierarchicalKey.censoDerivationPath
        )

        listOf(
            bitcoinHierarchicalKey,
            ethereumHierarchicalKey,
            censoHierarchicalKey
        ).forEach {
            it.signData(DATA_CHECK)
        }

        return AllKeys(
            bitcoinKey = bitcoinHierarchicalKey,
            ethereumKey = ethereumHierarchicalKey,
            censoKey = censoHierarchicalKey
        )
    }

    override fun saveV3RootSeed(rootSeed: ByteArray, email: String, cipher: Cipher) {
        val encryptedRootSeed =
            cryptographyManager.encryptData(data = BaseWrapper.encode(rootSeed), cipher = cipher)

        securePreferences.saveV3RootSeed(email = email, encryptedData = encryptedRootSeed)
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

    override fun haveSentinelDataStored(email: String) = securePreferences.hasSentinelData(email)

    override fun saveSentinelData(email: String, cipher: Cipher) {
        val encryptedSentinelData =
            cryptographyManager.encryptData(data = SENTINEL_STATIC_DATA, cipher = cipher)

        securePreferences.saveSentinelData(email = email, encryptedData = encryptedSentinelData)
    }

    override fun deleteBiometryKeyFromKeystore(keyName: String) {
        cryptographyManager.deleteInvalidatedKey(keyName)
    }

    override fun deleteKeyIfInKeystore(keyName: String) {
        cryptographyManager.deleteKeyIfPresent(keyName)
    }

    override fun getSignatureForDeviceSigning(keyName: String): Signature {
        return cryptographyManager.getSignatureForDeviceSigning(keyName)
    }

    override fun publicKeysFromRootSeed(rootSeed: ByteArray): HashMap<String, String> {
        val keys = createAllKeys(rootSeed)
        val publicKeys = publicKeys(keys)

        return hashMapOf(
            BITCOIN_KEY to publicKeys.bitcoinPublicKey,
            ETHEREUM_KEY to publicKeys.ethereumPublicKey,
            OFFCHAIN_KEY to publicKeys.censoPublicKey
        )
    }

    private fun publicKeys(keys: AllKeys): AllPublicKeys {
        val bitcoinPublicKey = keys.bitcoinKey.getBase58ExtendedPublicKey()
        val ethereumPublicKey = keys.ethereumKey.getBase58UncompressedPublicKey()
        val censoPublicKey = keys.censoKey.getBase58UncompressedPublicKey()

        return AllPublicKeys(
            bitcoinPublicKey = bitcoinPublicKey,
            ethereumPublicKey = ethereumPublicKey,
            censoPublicKey = censoPublicKey
        )
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
    val bitcoinKey: Secp256k1HierarchicalKey,
    val ethereumKey: Secp256k1HierarchicalKey,
    val censoKey: Secp256k1HierarchicalKey
)

data class AllPublicKeys(
    val bitcoinPublicKey: String,
    val ethereumPublicKey: String,
    val censoPublicKey: String
)

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): List<ByteArray>
}

sealed class SignableDataResult {
    data class Ethereum(
        val dataToSign: ByteArray,
        val offchain: Offchain? = null,
    ): SignableDataResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Ethereum

            if (!dataToSign.contentEquals(other.dataToSign)) return false
            if (offchain != other.offchain) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSign.contentHashCode()
            result = 31 * result + (offchain?.hashCode() ?: 0)
            return result
        }
    }

    data class Bitcoin(
        val dataToSign: List<ByteArray>,
        val childKeyIndex: Int
    ): SignableDataResult()

    data class Polygon(
         val dataToSign: ByteArray,
         val offchain: Offchain? = null,
    ): SignableDataResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Polygon

            if (!dataToSign.contentEquals(other.dataToSign)) return false
            if (offchain != other.offchain) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSign.contentHashCode()
            result = 31 * result + (offchain?.hashCode() ?: 0)
            return result
        }
    }

    data class Offchain(
        val dataToSend: ByteArray,
        val dataToSign: ByteArray
    ): SignableDataResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Offchain

            if (!dataToSend.contentEquals(other.dataToSend)) return false
            if (!dataToSign.contentEquals(other.dataToSign)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSend.contentHashCode()
            result = 31 * result + dataToSign.contentHashCode()
            return result
        }
    }

    data class Device(
        val dataToSend: ByteArray,
        val dataToSign: ByteArray
    ): SignableDataResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Device

            if (!dataToSend.contentEquals(other.dataToSend)) return false
            if (!dataToSign.contentEquals(other.dataToSign)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSend.contentHashCode()
            result = 31 * result + dataToSign.contentHashCode()
            return result
        }
    }
}

interface SignableV2 {
    fun retrieveSignableData(): List<SignableDataResult>
}