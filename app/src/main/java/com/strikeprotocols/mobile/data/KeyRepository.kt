package com.strikeprotocols.mobile.data

import android.security.keystore.KeyPermanentlyInvalidatedException
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.generateFormattedTimestamp
import com.strikeprotocols.mobile.data.models.Chain
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import java.security.InvalidAlgorithmParameterException
import javax.crypto.Cipher

interface KeyRepository {
    suspend fun getCipherForEncryption(keyName: String): Cipher?
    suspend fun getCipherForBackgroundDecryption(): Cipher?
    suspend fun getCipherForPrivateKeyDecryption(): Cipher?
    suspend fun signTimestamp(
        timestamp: String,
        cipher: Cipher,
    ): String

    suspend fun getDeprecatedPrivateKey(): String
    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean

    suspend fun generateInitialAuthDataAndSaveKeyToUser(
        mnemonic: Mnemonics.MnemonicCode,
        cipher: Cipher
    ): WalletSigner

    suspend fun regenerateAuthDataAndSaveKeyToUser(
        phrase: String,
        backendPublicKey: String,
        cipher: Cipher
    )

    suspend fun regenerateDataAndUploadToBackend(): Resource<WalletSigner>

    suspend fun migrateOldDataToBiometryProtectedStorage(cipher: Cipher)

    suspend fun havePrivateKey(): Boolean

    suspend fun haveSentinelData() : Boolean

    suspend fun generateTimestamp() : String

    suspend fun saveSentinelData(cipher: Cipher)

    suspend fun retrieveSentinelData(cipher: Cipher) : String
}

class KeyRepositoryImpl(
    private val encryptionManager: EncryptionManager,
    private val securePreferences: SecurePreferences,
    private val api: BrooklynApiService,
    private val userRepository: UserRepository
) : KeyRepository, BaseRepository() {

    override suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKey = securePreferences.retrievePublicKey(email = userEmail)
        val havePrivateKey = encryptionManager.havePrivateKeyStored(email = userEmail)


        if (publicKey.isEmpty() || !havePrivateKey) {
            return false
        }

        val backendPublicKey = verifyUser.firstPublicKey()

        if (backendPublicKey.isNullOrEmpty()) {
            return false
        }

        //api call to get wallet signer
        for (walletSigner in walletSigners) {
            if (walletSigner?.publicKey != null
                && walletSigner.publicKey == publicKey
                && walletSigner.publicKey == backendPublicKey
            ) {
                return true
            }
        }

        return false
    }

    override suspend fun getCipherForEncryption(keyName: String): Cipher? {
        return try {
            encryptionManager.getInitializedCipherForEncryption(keyName)
        } catch (e: Exception) {
            handleCipherException(e, keyName)
        }
    }

    override suspend fun getCipherForBackgroundDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveSentinelData(email)
            encryptionManager.getInitializedCipherForDecryption(
                initVector = encryptedData.initializationVector, keyName = SENTINEL_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, SENTINEL_KEY_NAME)
        }
    }

    override suspend fun getCipherForPrivateKeyDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveEncryptedStoredKeys(email)
            val storedKeyData = StoredKeyData.fromJson(encryptedData)
            val initVector = BaseWrapper.decode(storedKeyData.initVector)
            encryptionManager.getInitializedCipherForDecryption(
                initVector = initVector, keyName = BIO_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, BIO_KEY_NAME)
        }
    }

    private suspend fun handleCipherException(e: Exception, keyName: String) : Cipher? {
        when (e) {
            is KeyPermanentlyInvalidatedException,
            is InvalidAlgorithmParameterException,
            is InvalidKeyPhraseException -> {
                wipeAllDataAfterKeyInvalidatedException(keyName)
            }
            else -> throw  e
        }
        return null
    }


    private suspend fun wipeAllDataAfterKeyInvalidatedException(keyName: String) {
        val email = userRepository.retrieveUserEmail()
        encryptionManager.deleteBiometryKeyFromKeystore(BIO_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(SENTINEL_KEY_NAME)
        securePreferences.clearAllRelevantKeyData(email)
        securePreferences.clearSentinelData(email)
        if (keyName == BIO_KEY_NAME) {
            userRepository.logOut()
        }
        userRepository.setKeyInvalidated()
    }

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
                cipher = cipher
            )

        return BaseWrapper.encodeToBase64(signedTimestamp)
    }

    override suspend fun getDeprecatedPrivateKey(): String {
        val userEmail = userRepository.retrieveUserEmail()
        return securePreferences.retrieveDeprecatedPrivateKey(userEmail)
    }

    override suspend fun generatePhrase(): String = encryptionManager.generatePhrase()

    override suspend fun generateInitialAuthDataAndSaveKeyToUser(
        mnemonic: Mnemonics.MnemonicCode,
        cipher: Cipher
    ): WalletSigner {
        val userEmail = userRepository.retrieveUserEmail()
        val keyPair = encryptionManager.createKeyPair(mnemonic)

        encryptionManager.saveKeyInformation(
            email = userEmail, cipher = cipher,
            privateKey = keyPair.privateKey, rootSeed = mnemonic.toSeed(),
            publicKey = keyPair.publicKey
        )

        return WalletSigner(
            publicKey = BaseWrapper.encode(keyPair.publicKey),
            chain = Chain.solana
        )
    }


    override suspend fun regenerateDataAndUploadToBackend(): Resource<WalletSigner> {
        return try {
            val userEmail = userRepository.retrieveUserEmail()
            val publicKey = securePreferences.retrievePublicKey(userEmail)

            val walletSigner = WalletSigner(
                publicKey = publicKey,
                chain = Chain.solana
            )

            retrieveApiResource { api.addWalletSigner(walletSigner) }
        } catch (e: Exception) {
            Resource.Error()
        }
    }

    override suspend fun regenerateAuthDataAndSaveKeyToUser(
        phrase: String,
        backendPublicKey: String,
        cipher: Cipher
    ) {
        val userEmail = userRepository.retrieveUserEmail()
        //Validate the phrase firsts
        val mnemonic = Mnemonics.MnemonicCode(phrase)

        //Regenerate the key pair
        val keyPair = encryptionManager.createKeyPair(mnemonic)

        //Verify the keyPair
        val validPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )
        if (!validPair) {
            throw AuthDataException.InvalidKeyPairException()
        }

        //Verify the backend public key and recreated private key work together
        val phraseKeyMatchesBackendKey = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = backendPublicKey
        )
        if (!phraseKeyMatchesBackendKey) {
            throw AuthDataException.PhraseKeyDoesNotMatchBackendKeyException()
        }

        encryptionManager.saveKeyInformation(
            email = userEmail, cipher = cipher,
            privateKey = keyPair.privateKey,
            rootSeed = mnemonic.toSeed(),
            publicKey = keyPair.publicKey
        )
    }

    override suspend fun migrateOldDataToBiometryProtectedStorage(cipher: Cipher) {
        val userEmail = userRepository.retrieveUserEmail()

        val oldPrivateKey = securePreferences.retrieveDeprecatedPrivateKey(userEmail)
        val oldRootSeed = securePreferences.retrieveDeprecatedRootSeed(userEmail)

        val publicKey = encryptionManager.regeneratePublicKey(oldPrivateKey)

        val verifiedCreatedPublicKey =
            encryptionManager.verifyKeyPair(privateKey = oldPrivateKey, publicKey = publicKey)

        if (!verifiedCreatedPublicKey) {
            throw Exception("Public key recreated does not match")
        }

        try {
            encryptionManager.saveKeyInformation(
                email = userEmail, cipher = cipher,
                privateKey = BaseWrapper.decode(oldPrivateKey),
                rootSeed = BaseWrapper.decode(oldRootSeed),
                publicKey = BaseWrapper.decode(publicKey)
            )

            securePreferences.clearDeprecatedPrivateKey(email = userEmail)
            securePreferences.clearDeprecatedRootSeed(email = userEmail)
        } catch (e: Exception) {
            encryptionManager.deleteBiometryKeyFromKeystore(BIO_KEY_NAME)
            throw e
        }
    }

    override suspend fun havePrivateKey(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return encryptionManager.havePrivateKeyStored(userEmail)
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