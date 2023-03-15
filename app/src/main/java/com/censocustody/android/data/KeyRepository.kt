package com.censocustody.android.data

import android.security.keystore.KeyPermanentlyInvalidatedException
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.*
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.mapToPublicKeysList
import com.raygun.raygun4android.RaygunClient
import java.security.InvalidAlgorithmParameterException
import javax.crypto.Cipher

interface KeyRepository {

    suspend fun signTimestamp(timestamp: String): String

    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean

    suspend fun retrieveV3RootSeed(): ByteArray?
    suspend fun haveV3RootSeed() : Boolean

    suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode)
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

    fun validateUserEnteredPhraseAgainstBackendKeys(
        phrase: String,
        verifyUser: VerifyUser?
    ): Boolean

    suspend fun uploadKeys(walletSigners: List<WalletSigner>): Resource<Unit>

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
            val email = userRepository.retrieveUserEmail()
            val sentinelKeyId = securePreferences.retrieveSentinelId(email)
            cryptographyManager.getInitializedCipherForSentinelEncryption(sentinelKeyId)
        } catch (e: Exception) {
            handleKeyInvalidatedException(e)
            null
        }
    }

    override suspend fun getInitializedCipherForSentinelDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveSentinelData(email)
            val sentinelKeyId = securePreferences.retrieveSentinelId(email)
            return cryptographyManager.getInitializedCipherForSentinelDecryption(
                sentinelKeyId = sentinelKeyId,
                initializationVector = encryptedData.initializationVector
            )
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

    override suspend fun signTimestamp(timestamp: String): String {
        val userEmail = userRepository.retrieveUserEmail()

        val tokenByteArray = timestamp.toByteArray(charset = Charsets.UTF_8)

        val signedTimestamp = encryptionManager.signDataWithDeviceKey(
            data = tokenByteArray,
            email = userEmail
        )

        return BaseWrapper.encodeToBase64(signedTimestamp)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode) {
        val userEmail = userRepository.retrieveUserEmail()

        val rootSeed = mnemonic.toSeed()

        keyStorage.saveRootSeed(
            rootSeed = rootSeed,
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
        val userEmail = userRepository.retrieveUserEmail()

        return String(
            keyStorage.retrieveSentinelData(email = userEmail, cipher = cipher),
            Charsets.UTF_8
        )
    }

    override suspend fun handleKeyInvalidatedException(exception: Exception) {
        RaygunClient.send(
            exception,
            listOf(CrashReportingUtil.MANUALLY_REPORTED_TAG, CrashReportingUtil.KEY_INVALIDATED)
        )

        when (exception) {
            is KeyPermanentlyInvalidatedException,
            is InvalidAlgorithmParameterException,
            is InvalidKeyPhraseException -> {
                wipeAllDataAfterKeyInvalidatedException()
            }
            else -> throw exception
        }
    }

    private suspend fun wipeAllDataAfterKeyInvalidatedException() {
        val email = userRepository.retrieveUserEmail()
        deleteSentinelKeyInfoWhenBiometryInvalidated(email)
        deleteRootSeedKeyInfoWhenBiometryInvalidated(email)
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

    private fun deleteSentinelKeyInfoWhenBiometryInvalidated(email: String) {
        val sentinelId = SharedPrefsHelper.retrieveSentinelId(email)

        if (sentinelId.isNotEmpty()) {
            cryptographyManager.deleteKeyIfPresent(sentinelId)
        }
        securePreferences.clearSentinelData(email)
    }

    private fun deleteRootSeedKeyInfoWhenBiometryInvalidated(email: String) {
        val rootSeedId = SharedPrefsHelper.retrieveRootSeedId(email)

        if (rootSeedId.isNotEmpty()) {
            cryptographyManager.deleteKeyIfPresent(rootSeedId)
        }
        securePreferences.clearAllV3KeyData(email)
    }
}