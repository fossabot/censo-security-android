package com.strikeprotocols.mobile.data

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.generateFormattedTimestamp
import com.strikeprotocols.mobile.data.models.Chain
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletPublicKey
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.mapToPublicKeysList
import javax.crypto.Cipher

interface KeyRepository {

    suspend fun doesUserHaveV1KeyData() : Boolean
    suspend fun doesUserHaveV2KeyData() : Boolean

    suspend fun signTimestamp(
        timestamp: String,
        cipher: Cipher,
    ): String

    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean

    suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher)
    suspend fun saveV3PrivateKeys(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher)
    suspend fun saveV3PublicKeys(mnemonic: Mnemonics.MnemonicCode) : List<WalletSigner?>
    suspend fun retrieveV3PublicKeys() : List<WalletSigner?>

    suspend fun havePrivateKeys(): Boolean

    suspend fun haveSentinelData() : Boolean

    suspend fun generateTimestamp() : String

    suspend fun saveSentinelData(cipher: Cipher)

    suspend fun retrieveSentinelData(cipher: Cipher) : String

    suspend fun removeSentinelDataAndKickUserToAppEntrance()

    //suspend fun userHasLocalKeyThatBackendDoesNot(backendKeys : List<WalletPublicKey?>) : Boolean

    suspend fun signKeysThatBackendIsMissing(
        keysToBeAdded: List<WalletSigner>,
        mnemonic: Mnemonics.MnemonicCode
    ): List<WalletSigner>
}

class KeyRepositoryImpl(
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences,
    private val userRepository: UserRepository
) : KeyRepository, BaseRepository() {

    override suspend fun doesUserHaveValidLocalKey(verifyUser: VerifyUser): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKeys = securePreferences.retrieveV3PublicKeys(email = userEmail)
        val havePrivateKey = encryptionManager.havePrivateKeysStored(email = userEmail)


        if (publicKeys.isEmpty() || !havePrivateKey) {
            return false
        }

        val backendPublicKeys = verifyUser.publicKeys

        if (backendPublicKeys.isNullOrEmpty()) {
            return false
        }

        return verifyUser.compareAgainstLocalKeys(publicKeys)
    }

    override suspend fun doesUserHaveV1KeyData(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.userHasV1KeyData(email = userEmail)
    }

    override suspend fun doesUserHaveV2KeyData(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.userHasV2Storage(email = userEmail)
    }

    override suspend fun removeSentinelDataAndKickUserToAppEntrance() {
        val email = userRepository.retrieveUserEmail()
        encryptionManager.deleteBiometryKeyFromKeystore(SENTINEL_KEY_NAME)
        securePreferences.clearSentinelData(email)
        userRepository.logOut()
        userRepository.setInvalidSentinelData()
    }

//    override suspend fun userHasLocalKeyThatBackendDoesNot(backendKeys: List<WalletPublicKey?>): Boolean {
//        val userEmail = userRepository.retrieveUserEmail()
//
//        val localPublicKeys = securePreferences.retrieveV3PublicKeys(email = userEmail)
//
//        return localPublicKeys.keys.size > backendKeys.size
//    }

    override suspend fun signTimestamp(
        timestamp: String,
        cipher: Cipher
    ): String {
        val userEmail = userRepository.retrieveUserEmail()

        val tokenByteArray = timestamp.toByteArray(charset = Charsets.UTF_8)

        val signedTimestamp =
            encryptionManager.signDataWithEncryptedKey(
                data = tokenByteArray,
                userEmail = userEmail,
                cipher = cipher,
                keyType = SOLANA_KEY
            )

        return BaseWrapper.encodeToBase64(signedTimestamp)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun saveV3RootKey(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        val rootSeed = mnemonic.toSeed()

        encryptionManager.saveV3RootSeed(
            rootSeed = rootSeed,
            cipher = cipher,
            email = userEmail
        )
    }

    override suspend fun saveV3PrivateKeys(mnemonic: Mnemonics.MnemonicCode, cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        val rootSeed = mnemonic.toSeed()

        encryptionManager.saveV3PrivateKeys(
            rootSeed = rootSeed,
            cipher = cipher,
            email = userEmail
        )
    }

    override suspend fun saveV3PublicKeys(mnemonic: Mnemonics.MnemonicCode) : List<WalletSigner?> {
        val userEmail = userRepository.retrieveUserEmail()

        val rootSeed = mnemonic.toSeed()

        val publicKeys = encryptionManager.saveV3PublicKeys(
            rootSeed = rootSeed,
            email = userEmail
        )

        return publicKeys.mapToPublicKeysList()
    }

    override suspend fun retrieveV3PublicKeys(): List<WalletSigner?> {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.retrieveV3PublicKeys(userEmail).mapToPublicKeysList()
    }

    override suspend fun signKeysThatBackendIsMissing(
        keysToBeAdded: List<WalletSigner>,
        mnemonic: Mnemonics.MnemonicCode
    ): List<WalletSigner> {
        val rootSeed = mnemonic.toSeed()

        val signedKeysToAdd = mutableListOf<WalletSigner>()

        for (key in keysToBeAdded) {
            val signedKey = encryptionManager.signKeyForMigration(
                rootSeed = rootSeed,
                publicKey = key.publicKey ?: ""
            )
            signedKeysToAdd.add(key.copy(signature = BaseWrapper.encodeToBase64(signedKey)))
        }

        return signedKeysToAdd
    }

    override suspend fun havePrivateKeys(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return encryptionManager.havePrivateKeysStored(userEmail)
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