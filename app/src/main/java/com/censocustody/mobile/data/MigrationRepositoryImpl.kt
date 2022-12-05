package com.censocustody.mobile.data

import com.censocustody.mobile.common.BaseWrapper
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.models.Signers
import com.censocustody.mobile.data.models.StoredKeyData.Companion.ROOT_SEED
import com.censocustody.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import com.censocustody.mobile.data.models.VerifyUser
import com.censocustody.mobile.data.models.WalletSigner
import com.censocustody.mobile.data.models.mapToPublicKeysList
import javax.crypto.Cipher

interface MigrationRepository {
    suspend fun saveV3RootSeed(rootSeed: ByteArray, cipher: Cipher)
    suspend fun saveV3PublicKeys(rootSeed: ByteArray)

    suspend fun retrieveV1RootSeed() : ByteArray?
    suspend fun retrieveV3RootSeed(cipher: Cipher) : ByteArray?

    suspend fun haveV2RootSeed() : Boolean
    suspend fun haveV3RootSeed() : Boolean

    suspend fun retrieveV2RootSeed(cipher: Cipher): ByteArray?
    suspend fun retrieveV3PublicKeys(): HashMap<String, String>

    suspend fun retrieveWalletSignersToUpload(rootSeed: ByteArray): List<WalletSigner>

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

    override suspend fun saveV3PublicKeys(rootSeed: ByteArray) {
        val userEmail = userRepository.retrieveUserEmail()

        encryptionManager.saveV3PublicKeys(
            email = userEmail, rootSeed = rootSeed
        )
    }

    override suspend fun retrieveV1RootSeed(): ByteArray? {
        val userEmail = userRepository.retrieveUserEmail()

        val hasV1Data = securePreferences.userHasV1RootSeedStored(email = userEmail)

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

        return securePreferences.userHasV2RootSeedStored(userEmail)
    }

    override suspend fun retrieveV2RootSeed(cipher: Cipher): ByteArray? {
        val userEmail = userRepository.retrieveUserEmail()

        val haveV2Data = securePreferences.userHasV2RootSeedStored(userEmail)

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

    override suspend fun retrieveWalletSignersToUpload(rootSeed: ByteArray): List<WalletSigner> {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKeysMap = securePreferences.retrieveV3PublicKeys(userEmail)

        val keysToAdd = mutableListOf<WalletSigner>()

        for (key in publicKeysMap.mapToPublicKeysList()) {
            val signedKey = encryptionManager.signKeyForMigration(
                rootSeed = rootSeed,
                publicKey = key.publicKey ?: ""
            )
            keysToAdd.add(key.copy(signature = BaseWrapper.encodeToBase64(signedKey)))
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