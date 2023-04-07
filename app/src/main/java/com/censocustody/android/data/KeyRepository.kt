package com.censocustody.android.data

import android.security.keystore.KeyPermanentlyInvalidatedException
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.models.*
import com.raygun.raygun4android.RaygunClient
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStoreException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

interface KeyRepository {
    suspend fun signTimestamp(timestamp: String): String
    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean
    suspend fun retrieveV3RootSeed(): ByteArray?
    suspend fun haveV3RootSeed() : Boolean
    suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode?, rootSeed: ByteArray? = null)
    suspend fun saveV3PublicKeys(rootSeed: ByteArray) : List<WalletSigner>
    suspend fun retrieveV3PublicKeys() : List<WalletSigner>
    suspend fun hasV3RootSeedStored() : Boolean
    suspend fun haveSentinelData() : Boolean
    suspend fun generateTimestamp() : String
    suspend fun saveSentinelData(cipher: Cipher)
    suspend fun retrieveSentinelData(cipher: Cipher) : String
    suspend fun getInitializedCipherForSentinelEncryption(): Cipher?
    suspend fun getInitializedCipherForSentinelDecryption(): Cipher?
    suspend fun handleKeyInvalidatedException(exception: Exception)
    suspend fun retrieveRecoveryShards(): Resource<GetRecoveryShardsResponse>
    fun validateUserEnteredPhraseAgainstBackendKeys(
        phrase: String,
        verifyUser: VerifyUser?
    ): Boolean

    fun validateRecoveredRootSeed(
        rootSeed: ByteArray,
        verifyUser: VerifyUser?
    ): Boolean

    suspend fun uploadKeys(walletSigners: List<WalletSigner>): Resource<Unit>

    suspend fun recoverRootSeed(shards: List<Shard>, ancestors: List<AncestorShard>) : ByteArray

    suspend fun signPublicKeys(
        publicKeys: List<WalletSigner?>,
        rootSeed: ByteArray
    ): List<WalletSigner>
}

class KeyRepositoryImpl(
    private val encryptionManager: EncryptionManager,
    private val cryptographyManager: CryptographyManager,
    private val securePreferences: SecurePreferences,
    private val keyStorage: KeyStorage,
    private val userRepository: UserRepository,
    private val brooklynApiService: BrooklynApiService
) : KeyRepository, BaseRepository() {

    override suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKeys = securePreferences.retrieveV3PublicKeys(email = userEmail)
        val havePrivateKey = securePreferences.hasV3RootSeed(email = userEmail)


        if (publicKeys.isEmpty() || !havePrivateKey) {
            return false
        }

        val backendPublicKeys = verifyUser.publicKeys

        if (backendPublicKeys.isNullOrEmpty()) {
            return false
        }

        return verifyUser.compareAgainstLocalKeys(publicKeys)
    }

    override suspend fun getInitializedCipherForSentinelEncryption(): Cipher? {
        return try {
            val userEmail = userRepository.retrieveUserEmail()
            cryptographyManager.getInitializedCipherForSentinelEncryption(userEmail)
        } catch (e: Exception) {
            handleKeyInvalidatedException(e)
            null
        }
    }

    override suspend fun getInitializedCipherForSentinelDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveSentinelData(email)
            return cryptographyManager.getInitializedCipherForSentinelDecryption(
                email,
                encryptedData.initializationVector
            ) {
                censoLog(message = "Failing to get key...")
                userRepository.setKeyInvalidated(UserState.RESET_DEVICE)
            }
        } catch (e: Exception) {
            handleKeyInvalidatedException(e)
            null
        }
    }

    override fun validateUserEnteredPhraseAgainstBackendKeys(
        phrase: String,
        verifyUser: VerifyUser?
    ): Boolean {
        try {
            val rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()

            val publicKeys = encryptionManager.publicKeysFromRootSeed(rootSeed)

            if (publicKeys.isEmpty()) {
                return false
            }

            return verifyUser?.compareAgainstLocalKeys(publicKeys) == true
        } catch (e: Exception) {
            return false
        }
    }

    override fun validateRecoveredRootSeed(
        rootSeed: ByteArray,
        verifyUser: VerifyUser?
    ): Boolean {
        try {
            val publicKeys = encryptionManager.publicKeysFromRootSeed(rootSeed)

            if (publicKeys.isEmpty()) {
                return false
            }

            return verifyUser?.compareAgainstLocalKeys(publicKeys) == true
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.RECOVER_KEY,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            return false
        }
    }

    override suspend fun signTimestamp(timestamp: String): String {
        val userEmail = userRepository.retrieveUserEmail()

        val tokenByteArray = timestamp.toByteArray(charset = Charsets.UTF_8)

        val deviceId = SharedPrefsHelper.retrieveDeviceId(userEmail)

        val signedTimestamp = encryptionManager.signDataWithDeviceKey(
            data = tokenByteArray,
            deviceId = deviceId
        )

        return BaseWrapper.encodeToBase64(signedTimestamp)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode?, rootSeed: ByteArray?) {
        val userEmail = userRepository.retrieveUserEmail()

        keyStorage.saveRootSeed(
            rootSeed = rootSeed ?: mnemonic?.toSeed() ?: throw Exception("Must pass one non null version of root seed"),
            email = userEmail
        )
    }

    override suspend fun saveV3PublicKeys(rootSeed: ByteArray): List<WalletSigner> {
        val userEmail = userRepository.retrieveUserEmail()

        val publicKeys = encryptionManager.saveV3PublicKeys(
            rootSeed = rootSeed,
            email = userEmail
        )

        return publicKeys.mapToPublicKeysList()
    }

    override suspend fun retrieveV3PublicKeys(): List<WalletSigner> {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.retrieveV3PublicKeys(userEmail).mapToPublicKeysList()
    }

    override suspend fun haveV3RootSeed(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()

        return securePreferences.hasV3RootSeed(userEmail)
    }


    override suspend fun signPublicKeys(
        publicKeys: List<WalletSigner?>,
        rootSeed: ByteArray
    ): List<WalletSigner> {

        return publicKeys.mapNotNull {
            val signedKey = encryptionManager.signKeysWithCensoKey(
                rootSeed = rootSeed,
                publicKey = it?.publicKey ?: ""
            )
            it?.copy(signature = BaseWrapper.encodeToBase64(signedKey))
        }
    }

    override suspend fun retrieveV3RootSeed(): ByteArray? {
        return try {
            val userEmail = userRepository.retrieveUserEmail()
            keyStorage.retrieveRootSeed(email = userEmail)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun uploadKeys(walletSigners: List<WalletSigner>): Resource<Unit> {
        val email = userRepository.retrieveUserEmail()
        val signedData = encryptionManager.signKeysForUpload(email, walletSigners)
        return retrieveApiResource {
            brooklynApiService.addWalletSigner(
                Signers(walletSigners, BaseWrapper.encodeToBase64(signedData), share = null)
            )
        }
    }

    override suspend fun recoverRootSeed(
        shards: List<Shard>,
        ancestors: List<AncestorShard>
    ): ByteArray {
        val userEmail = userRepository.retrieveUserEmail()

        return encryptionManager.recoverRootSeedFromShards(
            email = userEmail,
            shards = shards,
            ancestors = ancestors
        )
    }

    override suspend fun hasV3RootSeedStored(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.hasV3RootSeed(userEmail)
    }

    override suspend fun haveSentinelData(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return encryptionManager.haveSentinelDataStored(userEmail)
    }

    override suspend fun generateTimestamp() = generateFormattedTimestamp()
    override suspend fun saveSentinelData(cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        keyStorage.saveSentinelData(email = userEmail, cipher = cipher)
    }

    override suspend fun retrieveSentinelData(cipher: Cipher) : String {
        return try {
            val userEmail = userRepository.retrieveUserEmail()

            String(
                keyStorage.retrieveSentinelData(email = userEmail, cipher = cipher),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            handleKeyInvalidatedException(e)
            ""
        }
    }

    override suspend fun handleKeyInvalidatedException(exception: Exception) {
        RaygunClient.send(
            exception,
            listOf(CrashReportingUtil.MANUALLY_REPORTED_TAG, CrashReportingUtil.KEY_INVALIDATED)
        )

        if (exception.cause is KeyStoreException) {
            wipeAllDataAfterKeyInvalidatedException()
            return
        }

        when (exception) {
            is IncorrectSentinelException,
            is AEADBadTagException,
            is KeyPermanentlyInvalidatedException,
            is InvalidAlgorithmParameterException,
            is InvalidKeyPhraseException -> {
                wipeAllDataAfterKeyInvalidatedException()
            }
            else -> throw exception
        }
    }
    override suspend fun retrieveRecoveryShards() =
        retrieveApiResource { brooklynApiService.getRecoveryShards() }

    private suspend fun wipeAllDataAfterKeyInvalidatedException() {
        val email = userRepository.retrieveUserEmail()
        cryptographyManager.deleteKeyIfPresent(SENTINEL_KEY_NAME)
        cryptographyManager.deleteKeyIfPresent(EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME)
        securePreferences.clearAllV3KeyData(email)
        securePreferences.clearSentinelData(email)
        deleteDeviceKeyInfoWhenBiometryInvalidated(email)
        userRepository.logOut()
        userRepository.setKeyInvalidated()
    }

    private fun deleteDeviceKeyInfoWhenBiometryInvalidated(email: String) {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)

        if (deviceId.isNotEmpty()) {
            cryptographyManager.deleteKeyIfPresent(deviceId)
        }
        securePreferences.clearDeviceKeyData(email)
    }
}