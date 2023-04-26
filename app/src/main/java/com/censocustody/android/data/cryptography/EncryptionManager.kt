package com.censocustody.android.data.cryptography

import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.common.*
import com.censocustody.android.common.util.EcdsaUtils
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.common.wrapper.toByteArrayNoSign
import com.censocustody.android.common.wrapper.toHexString
import com.censocustody.android.common.wrapper.toParticipantIdAsHexString
import com.censocustody.android.data.cryptography.EncryptionManagerImpl.Companion.DATA_CHECK
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.OFFCHAIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.censocustody.android.data.models.approvalV2.ApprovalSignature
import com.censocustody.android.data.storage.KeyStorage
import com.censocustody.android.data.storage.SharedPrefsHelper
import com.google.android.gms.common.util.VisibleForTesting
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import javax.inject.Inject

import java.security.PrivateKey
import java.util.UUID

data class SignedPayload(
    val signature: String,
    val payload: String
)

interface EncryptionManager {

    //region sign data
    fun signKeysWithCensoKey(rootSeed: ByteArray, publicKey: String): ByteArray

    fun signKeysForUpload(
        email: String,
        walletSigners: List<WalletSigner>,
        bootstrapSign: Boolean = false
    ): ByteArray

    fun signDataWithDeviceKey(
        data: ByteArray,
        deviceId: String
    ) : ByteArray

    fun signApprovalDispositionForDeviceKey(
        email: String,
        dataToSign: SignableDataResult.Device
    ) : ApprovalSignature.OffChainSignature

    fun signApprovalDisposition(
        email: String,
        dataToSign: List<SignableDataResult>
    ): List<ApprovalSignature>

    fun createShareForBootstrapUser(email: String, rootSeed: ByteArray) : Share

    fun createShare(shardingPolicy: ShardingPolicy, rootSeed: ByteArray,) : Share

    fun reEncryptShards(email: String, shards: List<Shard>, targetDevicePublicKey: String): List<RecoveryShard>

    fun recoverRootSeedFromShards(
        shards: List<Shard>,
        ancestors: List<AncestorShard>,
        email: String
    ): ByteArray

    fun handleReshare(
        email: String,
        shards: List<Shard>,
        targetPolicy: ShardingPolicy,
    ): List<Shard>
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
        walletSigners: List<WalletSigner>,
        bootstrapSign: Boolean
    ): ByteArray {
        val dataToSign = Signers.retrieveDataToSign(walletSigners)

        val deviceId = if (bootstrapSign) {
            SharedPrefsHelper.retrieveBootstrapDeviceId(email)
        } else {
            SharedPrefsHelper.retrieveDeviceId(email)
        }

        return signDataWithDeviceKey(
            data = dataToSign,
            deviceId = deviceId
        )
    }

    override fun signDataWithDeviceKey(
        data: ByteArray,
        deviceId: String,
    ): ByteArray {
        return cryptographyManager.signData(
            dataToSign = data,
            keyName = deviceId
        )
    }

    override fun signApprovalDispositionForDeviceKey(
        email: String,
        dataToSign: SignableDataResult.Device
    ): ApprovalSignature.OffChainSignature {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)
        val signedData = signDataWithDeviceKey(
            data = dataToSign.dataToSign, deviceId = deviceId
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
                    ApprovalSignature.BitcoinSignatures(
                        signatures = bitcoinSignedData,
                        offchainSignature = offChainSignature(signable.offchain, keys.censoKey)
                    )
                }
                is SignableDataResult.Ethereum -> {
                    val signedData = keys.ethereumKey.signData(signable.dataToSign)
                    val signature = BaseWrapper.encodeToBase64(signedData)

                    ApprovalSignature.EthereumSignature(
                        signature = signature,
                        offchainSignature = offChainSignature(signable.offchain, keys.censoKey)
                    )
                }
                is SignableDataResult.Offchain -> offChainSignature(signable, keys.censoKey)
                is SignableDataResult.Device -> null
                is SignableDataResult.Polygon -> {
                    val signedData = keys.ethereumKey.signData(signable.dataToSign)
                    val signature = BaseWrapper.encodeToBase64(signedData)

                    ApprovalSignature.PolygonSignature(
                        signature = signature,
                        offchainSignature = offChainSignature(signable.offchain, keys.censoKey)
                    )
                }
            }
        }
    }

    private fun offChainSignature(signableData: SignableDataResult.Offchain, censoKey: Secp256k1HierarchicalKey): ApprovalSignature.OffChainSignature {
        val censoSignedData = censoKey.signData(signableData.dataToSign)
        val censoSignature = BaseWrapper.encodeToBase64(censoSignedData)

        return ApprovalSignature.OffChainSignature(
            signedData = BaseWrapper.encodeToBase64(signableData.dataToSend),
            signature = censoSignature
        )
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

    override fun createShareForBootstrapUser(email: String, rootSeed: ByteArray) : Share {
        val devicePublicKey = SharedPrefsHelper.retrieveDevicePublicKey(email)
        val bootstrapDevicePublicKey = SharedPrefsHelper.retrieveBootstrapDevicePublicKey(email)

        val deviceShardingParticipant = ShardingParticipant(
            participantId = devicePublicKey.toParticipantIdAsHexString(),
            devicePublicKeys = listOf(devicePublicKey)
        )

        val bootstrapShardingParticipant = ShardingParticipant(
            participantId = bootstrapDevicePublicKey.toParticipantIdAsHexString(),
            devicePublicKeys = listOf(bootstrapDevicePublicKey)
        )

        val shardingPolicy = ShardingPolicy(
            policyRevisionGuid = UUID.randomUUID().toString(),
            threshold = 2,
            participants = listOf(deviceShardingParticipant, bootstrapShardingParticipant)
        )

        return createShare(shardingPolicy = shardingPolicy, rootSeed = rootSeed)
    }

    override fun createShare(shardingPolicy: ShardingPolicy, rootSeed: ByteArray): Share {

        val participantIdToAdminUserMap = shardingPolicy.participants.associateBy {
            BigInteger(it.participantId, 16)
        }

        val secretSharer = SecretSharer(
            secret = BigInteger(rootSeed.toHexString(), 16),
            threshold = shardingPolicy.threshold,
            participants = participantIdToAdminUserMap.keys.toList()
        )

        return Share(
            policyRevisionId = shardingPolicy.policyRevisionGuid,
            shards = secretSharer.shards.mapNotNull { point ->
                participantIdToAdminUserMap[point.x]?.let { participant ->
                    Shard(
                        participant.participantId,
                        participant.devicePublicKeys.map { devicePublicKey ->
                            ShardCopy(
                                devicePublicKey,
                                encryptShard(point, devicePublicKey)
                            )
                        },
                    )
                }
            }
        )
    }

    override fun handleReshare(
        email: String,
        shards: List<Shard>,
        targetPolicy: ShardingPolicy
    ): List<Shard> {

        val participantIdToAdminUserMap =
            targetPolicy.participants.associateBy {
                BigInteger(it.participantId, 16)
            }

        val privateKeyMap = getMapOfDeviceKeys(email = email)

        return shards.mapNotNull { shard ->
            val keyToDecrypt = privateKeyMap[shard.shardCopies.firstOrNull()?.encryptionPublicKey]

            keyToDecrypt?.let {
                val secretSharer = SecretSharer(
                    decryptShard(
                        shard.shardCopies[0].encryptedData,
                        it
                    ),
                    targetPolicy.threshold,
                    participantIdToAdminUserMap.keys.toList()
                )

                secretSharer.shards.mapNotNull { point ->
                    participantIdToAdminUserMap[point.x]?.let { participant ->
                        Shard(
                            participant.participantId,
                            participant.devicePublicKeys.map { devicePublicKey ->
                                ShardCopy(
                                    devicePublicKey,
                                    encryptShard(point, devicePublicKey)
                                )
                            },
                            parentShardId = shard.shardId
                        )
                    } ?: run {
                        null
                    }
                }
            }
        }.flatten()
    }

    override fun recoverRootSeedFromShards(
        shards: List<Shard>,
        ancestors: List<AncestorShard>,
        email: String
    ): ByteArray {
        val privateKeyMap = getMapOfDeviceKeys(email = email)

        val shardEntries = recoverShards(
            shards.mapNotNull {
                privateKeyMap[it.shardCopies[0].encryptionPublicKey]?.let { privateKey ->
                    ShardEntry(
                        it.shardId!!,
                        BigInteger(it.participantId, 16),
                        it.parentShardId,
                        decryptShard(it.shardCopies[0].encryptedData, privateKey)
                    )
                }
            },
            ancestors.associateBy { it.shardId }
        )

        return shardEntries.firstOrNull()?.shard?.toByteArrayNoSign(64)
            ?: throw Exception("No ShardEntry created when trying to recover seed.")
    }

    private fun recoverShards(
        shards: List<ShardEntry>,
        ancestors: Map<String, AncestorShard>
    ): List<ShardEntry> {
        if (shards.size <= 1) {
            return shards
        }

        val recoveredShards = shards.groupBy { it.parentId }.map { (key, shards) ->
            val secret = SecretSharerUtils.recoverSecret(
                shards.map { Point(it.participantId, it.shard!!) },
                ORDER
            )
            key?.let { ancestors[key] }?.let { parent ->
                ShardEntry(
                    parent.shardId,
                    BigInteger(1, Hex.decode(parent.participantId)),
                    parent.parentShardId,
                    secret
                )
            } ?: ShardEntry(null, BigInteger.ZERO, null, secret)
        }
        return recoverShards(recoveredShards, ancestors)
    }

    override fun reEncryptShards(email: String, shards: List<Shard>, targetDevicePublicKey: String): List<RecoveryShard> {
        val deviceKeys = getDeviceAndBootstrapKeys(email)
        val recoveryShards: MutableList<RecoveryShard> =
            emptyList<RecoveryShard>().toMutableList()

        shards.forEach { shard ->
            shard.shardCopies.forEach { shardCopy ->

                val keyToDecrypt = getCorrectKeyToDecrypt(
                    deviceKey = deviceKeys.standardDeviceKey,
                    bootstrapKey = deviceKeys.bootstrapKey,
                    email = email,
                    encryptionPublicKey = shardCopy.encryptionPublicKey
                )
                val decrypted = decryptShard(
                    encryptedShard = shardCopy.encryptedData,
                    privateKey = keyToDecrypt
                )

                val encryptedData = encryptShard(
                    y = decrypted,
                    base58AdminKey = targetDevicePublicKey
                )

                shard.shardId?.let {
                    recoveryShards.add(
                        RecoveryShard(
                            shardId = shard.shardId,
                            encryptedData = encryptedData
                        )
                    )
                }
            }
        }

        return recoveryShards
    }

    private fun getDeviceAndBootstrapKeys(email: String) : DeviceKeys {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)
        val bootstrapId =
            if (SharedPrefsHelper.userHasBootstrapDeviceIdSaved(email)) SharedPrefsHelper.retrieveBootstrapDeviceId(
                email
            ) else null

        val deviceKey = cryptographyManager.getOrCreateKey(deviceId)
        val bootStrapKey =
            if (bootstrapId != null) cryptographyManager.getOrCreateKey(bootstrapId) else null

        return DeviceKeys(standardDeviceKey = deviceKey, bootstrapKey = bootStrapKey)
    }
    private fun getCorrectKeyToDecrypt(
        deviceKey: PrivateKey,
        bootstrapKey: PrivateKey?,
        email: String,
        encryptionPublicKey: String
    ): PrivateKey {
        return if (bootstrapKey == null) {
            deviceKey
        } else {
            val devicePublicKey = SharedPrefsHelper.retrieveDevicePublicKey(email)
            val bootstrapPublicKey =
                SharedPrefsHelper.retrieveBootstrapDevicePublicKey(email)

            if (devicePublicKey == encryptionPublicKey) {
                deviceKey
            } else if (bootstrapPublicKey == encryptionPublicKey) {
                bootstrapKey
            } else {
                throw Exception("Device does not have key to decrypt shard")
            }
        }
    }

    private fun getMapOfDeviceKeys(email: String): Map<String, PrivateKey> {
        val mapOfKeys: MutableMap<String, PrivateKey> = mutableMapOf()

        val deviceId = SharedPrefsHelper.retrieveDeviceId(email = email)
        val deviceKey = cryptographyManager.getOrCreateKey(deviceId)
        val devicePublicKey = SharedPrefsHelper.retrieveDevicePublicKey(email)

        mapOfKeys[devicePublicKey] = deviceKey

        if (SharedPrefsHelper.userHasBootstrapDeviceIdSaved(email)) {
            val bootstrapId = SharedPrefsHelper.retrieveBootstrapDeviceId(email = email)
            val bootStrapKey = cryptographyManager.getOrCreateKey(bootstrapId)
            val bootstrapPublicKey = SharedPrefsHelper.retrieveBootstrapDevicePublicKey(email)

            mapOfKeys[bootstrapPublicKey] = bootStrapKey
        }

        return mapOfKeys.toMap()
    }

    private fun decryptShard(encryptedShard: String, privateKey: PrivateKey): BigInteger {
        return BigInteger(
            1,
            ECIESManager.decryptMessage(
                BaseWrapper.decodeFromBase64(encryptedShard),
                privateKey
            )
        )
    }

    private fun encryptShard(point: Point, base58AdminKey: String): String {
        return encryptShard(point.y, base58AdminKey)
    }

    private fun encryptShard(y: BigInteger, base58AdminKey: String): String {
        return BaseWrapper.encodeToBase64(
            ECIESManager.encryptMessage(
                y.toByteArrayNoSign(),
                EcdsaUtils.getECPublicKeyFromBase58(base58AdminKey, EcdsaUtils.r1Curve).q.getEncoded(false)
            )
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
        const val SENTINEL_KEY_NAME = "sentinel_biometry_key"
        const val ROOT_SEED_KEY_NAME = "root_seed_encryption_key"
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

interface Signable {
    fun retrieveSignableData(approverPublicKey: String?): List<ByteArray>
}

sealed class SignableDataResult {
    interface Evm {
        val dataToSign: ByteArray
        val offchain: Offchain
    }

    data class Ethereum(
        override val dataToSign: ByteArray,
        override val offchain: Offchain,
    ): SignableDataResult(), Evm {
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
            result = 31 * result + (offchain.hashCode())
            return result
        }
    }

    data class Bitcoin(
        val dataToSign: List<ByteArray>,
        val childKeyIndex: Int,
        val offchain: Offchain,
    ): SignableDataResult()

    data class Polygon(
        override val dataToSign: ByteArray,
        override val offchain: Offchain,
    ): SignableDataResult(), Evm {
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
            result = 31 * result + (offchain.hashCode())
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

data class DeviceKeys(
    val standardDeviceKey: PrivateKey,
    val bootstrapKey: PrivateKey?
)