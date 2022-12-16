package com.censocustody.android.data

import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.mapToPublicKeysList
import javax.crypto.Cipher

interface MigrationRepository {
    suspend fun saveV3RootSeed(rootSeed: ByteArray, cipher: Cipher)
    suspend fun saveV3PublicKeys(rootSeed: ByteArray)

    suspend fun retrieveV3RootSeed(cipher: Cipher) : ByteArray?

    suspend fun haveV3RootSeed() : Boolean

    suspend fun retrieveV3PublicKeys(): HashMap<String, String>

    suspend fun retrieveWalletSignersToUpload(rootSeed: ByteArray): List<WalletSigner>

    suspend fun migrateSigner(walletSigners: List<WalletSigner>): Resource<Signers>
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

    override suspend fun haveV3RootSeed(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()

        return securePreferences.hasV3RootSeed(userEmail)
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
}