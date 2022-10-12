package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.Signers
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.ROOT_SEED
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.mapToPublicKeysList
import javax.crypto.Cipher

interface MigrationRepository {
    suspend fun saveV3RootSeed(rootSeed: ByteArray, cipher: Cipher)
    suspend fun saveV3PrivateKeys(rootSeed: ByteArray, cipher: Cipher)
    suspend fun saveV3PublicKeys(rootSeed: ByteArray)

    suspend fun retrieveV1RootSeed() : ByteArray?
    suspend fun retrieveV3RootSeed(cipher: Cipher) : ByteArray?

    suspend fun haveV2RootSeed() : Boolean
    suspend fun haveV3RootSeed() : Boolean

    suspend fun retrieveV2RootSeed(cipher: Cipher): ByteArray?
    suspend fun retrieveV3PublicKeys(): HashMap<String, String>

    suspend fun retrieveWalletSignersToUpload(
        rootSeed: ByteArray,
        verifyUser: VerifyUser?,
    ): List<WalletSigner>

    suspend fun migrateSigner(walletSigners: List<WalletSigner>): Resource<Signers>

    suspend fun clearOldData()
}

class MigrationRepositoryImpl(
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences,
    private val api: BrooklynApiService,
    private val userRepository: UserRepository
) : MigrationRepository, BaseRepository() {

    override suspend fun saveV3RootSeed(rootSeed: ByteArray, cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        encryptionManager.saveV3RootSeed(
            rootSeed = rootSeed,
            cipher = cipher,
            email = userEmail
        )
    }

    override suspend fun saveV3PrivateKeys(rootSeed: ByteArray, cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        encryptionManager.saveV3PrivateKeys(
            rootSeed = rootSeed,
            cipher = cipher,
            email = userEmail
        )
    }

    override suspend fun saveV3PublicKeys(rootSeed: ByteArray) {
        val userEmail = userRepository.retrieveUserEmail()

        encryptionManager.saveV3PublicKeys(
            email = userEmail, rootSeed = rootSeed
        )
    }

    override suspend fun retrieveV1RootSeed(): ByteArray? {
        val userEmail = userRepository.retrieveUserEmail()

        val hasV1Data = securePreferences.userHasV1KeyData(email = userEmail)

        if (!hasV1Data) return null

        val rootSeed = securePreferences.retrieveV1RootSeed(userEmail)

        return if (rootSeed.isEmpty()) null else BaseWrapper.decode(rootSeed)
    }

    override suspend fun haveV3RootSeed(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()

        return securePreferences.hasV3RootSeed(userEmail)
    }

    override suspend fun haveV2RootSeed(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()

        return securePreferences.userHasV2Storage(userEmail)
    }

    override suspend fun retrieveV2RootSeed(cipher: Cipher): ByteArray? {
        val userEmail = userRepository.retrieveUserEmail()

        val haveV2Data = securePreferences.userHasV2Storage(userEmail)

        if (!haveV2Data) return null

        return encryptionManager.retrieveSavedV2Key(
            email = userEmail, cipher = cipher, keyType = ROOT_SEED
        )
    }

    override suspend fun retrieveV3RootSeed(cipher: Cipher): ByteArray {
        val userEmail = userRepository.retrieveUserEmail()

        return BaseWrapper.decode(
            encryptionManager.retrieveRootSeed(email = userEmail, cipher = cipher)
        )
    }

    override suspend fun retrieveV3PublicKeys(): HashMap<String, String> {
        val userEmail = userRepository.retrieveUserEmail()

        return securePreferences.retrieveV3PublicKeys(email = userEmail)
    }

    override suspend fun retrieveWalletSignersToUpload(
        rootSeed: ByteArray,
        verifyUser: VerifyUser?,
    ): List<WalletSigner> {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKeysMap = securePreferences.retrieveV3PublicKeys(userEmail)

        val keysThatNeedToBeUploadedToBackend = verifyUser?.determineKeysUserNeedsToUpload(
            localKeys = publicKeysMap.mapToPublicKeysList()
        ) ?: emptyList()

        val keysToAdd = mutableListOf<WalletSigner>()

        if (keysThatNeedToBeUploadedToBackend.isNotEmpty()) {

            for (key in keysThatNeedToBeUploadedToBackend) {
                val signedKey = encryptionManager.signKeyForMigration(
                    rootSeed = rootSeed,
                    publicKey = key.publicKey ?: ""
                )
                keysToAdd.add(key.copy(signature = BaseWrapper.encodeToBase64(signedKey)))
            }
        }

        for (wallet in publicKeysMap.mapToPublicKeysList()) {
            if (wallet.chain !in keysToAdd.map { it.chain }) {
                keysToAdd.add(wallet)
            }
        }

        return keysToAdd
    }

    override suspend fun migrateSigner(walletSigners: List<WalletSigner>): Resource<Signers> {
        return retrieveApiResource {
            api.addWalletSigner(
                Signers(walletSigners)
            )
        }
    }

    override suspend fun clearOldData() {
        val userEmail = userRepository.retrieveUserEmail()

        securePreferences.clearAllV1KeyData(userEmail)
        securePreferences.clearAllV2KeyData(userEmail)
    }
}