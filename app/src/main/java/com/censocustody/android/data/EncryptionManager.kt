package com.censocustody.android.data

import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.common.*
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.censocustody.android.data.models.StoredKeyData
import com.censocustody.android.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.OFFCHAIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.approvalV2.ApprovalSignature
import com.google.android.gms.common.util.VisibleForTesting
import java.nio.charset.Charset
import java.security.Signature
import javax.crypto.Cipher
import javax.inject.Inject

data class SignedPayload(
    val signature: String,
    val payload: String
)

interface EncryptionManager {

    //region sign data
    fun signKeysWithCensoKey(rootSeed: ByteArray, publicKey: String): ByteArray

    fun signKeysForUpload(
        email: String,
        walletSigners: List<WalletSigner>
    ): ByteArray

    fun signDataWithDeviceKey(
        data: ByteArray,
        email: String
    ) : ByteArray

    fun signApprovalDispositionForDeviceKey(
        email: String,
        dataToSign: SignableDataResult.Device
    ) : ApprovalSignature.OffChainSignature

    fun signApprovalDisposition(
        email: String,
        dataToSign: List<SignableDataResult>
    ): List<ApprovalSignature>
    //endregion

    //region generic key work
    fun generatePhrase(): String
    //endregion

    fun saveV3PublicKeys(rootSeed: ByteArray, email: String): HashMap<String, String>

    fun haveSentinelDataStored(email: String): Boolean
    fun publicKeysFromRootSeed(rootSeed: ByteArray): HashMap<String, String>

    //endregion
}

class EncryptionManagerImpl @Inject constructor(
    private val cryptographyManager: CryptographyManager,
    private val keyStorage: KeyStorage
) : EncryptionManager {

    //region interface methods
    override fun signKeysWithCensoKey(rootSeed: ByteArray, publicKey: String): ByteArray {
        val keys = createAllKeys(rootSeed)
        return keys.censoKey.signData(BaseWrapper.decode(publicKey))
    }

    override fun signKeysForUpload(
        email: String,
        walletSigners: List<WalletSigner>
    ): ByteArray {
        val dataToSign = Signers.retrieveDataToSign(walletSigners)

        return signDataWithDeviceKey(
            data = dataToSign,
            email = email
        )
    }

    override fun signDataWithDeviceKey(
        data: ByteArray,
        email: String
    ): ByteArray {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)
        return cryptographyManager.signData(
            dataToSign = data,
            keyName = deviceId
        )
    }

    override fun signApprovalDispositionForDeviceKey(
        email: String,
        dataToSign: SignableDataResult.Device
    ): ApprovalSignature.OffChainSignature {
        val signedData = signDataWithDeviceKey(
            data = dataToSign.dataToSign, email = email
        )

        return ApprovalSignature.OffChainSignature(
            signature = BaseWrapper.encodeToBase64(signedData),
            signedData = BaseWrapper.encodeToBase64(dataToSign.dataToSign)
        )
    }

    override fun signApprovalDisposition(
        email: String,
        dataToSign: List<SignableDataResult>
    ): List<ApprovalSignature> {
        val rootSeed = keyStorage.retrieveRootSeed(email = email)
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

        keyStorage.savePublicKeys(
            email = email, keyJson = keyDataAsJson
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

    @VisibleForTesting()
    fun createAllKeys(rootSeed: ByteArray): AllKeys {
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

    override fun haveSentinelDataStored(email: String) = keyStorage.hasSentinelData(email)

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
        const val SENTINEL_STATIC_DATA = "sentinel_static_data"
        const val NO_OFFSET_INDEX = 0
        val DATA_CHECK = BaseWrapper.decode("VerificationCheck")
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