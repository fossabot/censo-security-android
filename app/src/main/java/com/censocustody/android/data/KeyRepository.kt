package com.censocustody.android.data

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.KeyStorage
import com.censocustody.android.common.Resource
import com.censocustody.android.common.generateFormattedTimestamp
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.mapToPublicKeysList
import java.security.Signature
import javax.crypto.Cipher

interface KeyRepository {

    suspend fun signTimestamp(
        timestamp: String,
        signature: Signature,
    ): String

    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean

    suspend fun retrieveV3RootSeed(cipher: Cipher): ByteArray?
    suspend fun haveV3RootSeed() : Boolean

    suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher)
    suspend fun saveV3PublicKeys(rootSeed: ByteArray) : List<WalletSigner>
    suspend fun retrieveV3PublicKeys() : List<WalletSigner>

    suspend fun hasV3RootSeedStored() : Boolean

    suspend fun haveSentinelData() : Boolean

    suspend fun generateTimestamp() : String

    suspend fun saveSentinelData(cipher: Cipher)

    suspend fun retrieveSentinelData(cipher: Cipher) : String

    suspend fun removeSentinelDataAndKickUserToAppEntrance()

    fun validateUserEnteredPhraseAgainstBackendKeys(
        phrase: String,
        verifyUser: VerifyUser?
    ): Boolean

    suspend fun uploadKeys(
        walletSigners: List<WalletSigner>,
        signature: Signature
    ): Resource<Signers>

    suspend fun signPublicKeys(
        publicKeys: List<WalletSigner?>,
        rootSeed: ByteArray
    ): List<WalletSigner>
}

class KeyRepositoryImpl(
    private val encryptionManager: EncryptionManager,
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

    override suspend fun removeSentinelDataAndKickUserToAppEntrance() {
        val email = userRepository.retrieveUserEmail()
        encryptionManager.deleteBiometryKeyFromKeystore(SENTINEL_KEY_NAME)
        securePreferences.clearSentinelData(email)
        userRepository.logOut()
        userRepository.setInvalidSentinelData()
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

    override suspend fun signTimestamp(
        timestamp: String,
        signature: Signature
    ): String {
        val userEmail = userRepository.retrieveUserEmail()

        val tokenByteArray = timestamp.toByteArray(charset = Charsets.UTF_8)

        val signedTimestamp = encryptionManager.signDataWithDeviceKey(
            data = tokenByteArray,
            signature = signature,
            email = userEmail
        )

        return BaseWrapper.encodeToBase64(signedTimestamp)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        val rootSeed = mnemonic.toSeed()

        keyStorage.saveRootSeed(
            rootSeed = rootSeed,
            cipher = cipher,
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

    override suspend fun retrieveV3RootSeed(cipher: Cipher): ByteArray? {
        return try {
            val userEmail = userRepository.retrieveUserEmail()
            keyStorage.retrieveRootSeed(email = userEmail, cipher = cipher)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun uploadKeys(walletSigners: List<WalletSigner>, signature: Signature): Resource<Signers> {
        val email = userRepository.retrieveUserEmail()
        val signedData = encryptionManager.signKeysForUpload(email, signature, walletSigners)
        return retrieveApiResource {
            brooklynApiService.addWalletSigner(
                Signers(walletSigners, BaseWrapper.encodeToBase64(signedData))
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

        encryptionManager.saveSentinelData(email = userEmail, cipher = cipher)
    }

    override suspend fun retrieveSentinelData(cipher: Cipher) : String {
        val userEmail = userRepository.retrieveUserEmail()

        return encryptionManager.retrieveSentinelData(email = userEmail, cipher = cipher)
    }
}