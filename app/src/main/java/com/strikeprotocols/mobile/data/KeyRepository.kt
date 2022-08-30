package com.strikeprotocols.mobile.data

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import javax.crypto.Cipher

interface KeyRepository {
    suspend fun getCipherForEncryption(): Cipher?
    suspend fun getCipherForDecryption(): Cipher?
    suspend fun signTimestamp(
        timestamp: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String

    suspend fun getDeprecatedPrivateKey(): String
    suspend fun generatePhrase(): String
    suspend fun doesUserHaveValidLocalKey(
        verifyUser: VerifyUser,
        walletSigners: List<WalletSigner?>
    ): Boolean

    suspend fun generateInitialAuthDataAndSaveKeyToUser(
        mnemonic: Mnemonics.MnemonicCode,
        cryptoObject: BiometricPrompt.CryptoObject
    ): WalletSigner

    suspend fun regenerateAuthDataAndSaveKeyToUser(
        phrase: String,
        backendPublicKey: String,
        cryptoObject: BiometricPrompt.CryptoObject
    )

    suspend fun regenerateDataAndUploadToBackend(): Resource<WalletSigner>

    suspend fun migrateOldDataToBiometryProtectedStorage(cryptoObject: BiometricPrompt.CryptoObject)

    suspend fun havePrivateKey(): Boolean
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

    override suspend fun getCipherForEncryption(): Cipher? {
        return try {
            encryptionManager.getInitializedCipherForEncryption()
        } catch (e: KeyPermanentlyInvalidatedException) {
            wipeAllDataAfterKeyInvalidatedException()
            userRepository.setKeyInvalidated()
            null
        }
    }

    override suspend fun getCipherForDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveEncryptedStoredKeys(email)
            val storedKeyData = StoredKeyData.fromJson(encryptedData)
            val initVector = BaseWrapper.decode(storedKeyData.initVector)
            encryptionManager.getInitializedCipherForDecryption(initVector)
        } catch (e: KeyPermanentlyInvalidatedException) {
            wipeAllDataAfterKeyInvalidatedException()
            userRepository.setKeyInvalidated()
            null
        }
    }

    private suspend fun wipeAllDataAfterKeyInvalidatedException() {
        val email = userRepository.retrieveUserEmail()
        encryptionManager.deleteBiometryKeyFromKeystore()
        securePreferences.clearAllRelevantKeyData(email)
        userRepository.logOut()
    }

    override suspend fun signTimestamp(
        timestamp: String,
        cryptoObject: BiometricPrompt.CryptoObject
    ): String {
        val userEmail = userRepository.retrieveUserEmail()

        val tokenByteArray = timestamp.toByteArray(charset = Charsets.UTF_8)

        val signedTimestamp =
            encryptionManager.signDataWithEncryptedKey(
                data = tokenByteArray,
                userEmail = userEmail,
                cryptoObject = cryptoObject
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
        cryptoObject: BiometricPrompt.CryptoObject
    ): WalletSigner {
        val userEmail = userRepository.retrieveUserEmail()
        val keyPair = encryptionManager.createKeyPair(mnemonic)

        encryptionManager.saveKeyInformation(
            email = userEmail, cryptoObject = cryptoObject,
            privateKey = keyPair.privateKey, rootSeed = mnemonic.toSeed(),
            publicKey = keyPair.publicKey
        )

        return WalletSigner(
            publicKey = BaseWrapper.encode(keyPair.publicKey),
            walletType = WalletSigner.WALLET_TYPE_SOLANA
        )
    }


    override suspend fun regenerateDataAndUploadToBackend(): Resource<WalletSigner> {
        return try {
            val userEmail = userRepository.retrieveUserEmail()
            val publicKey = securePreferences.retrievePublicKey(userEmail)

            val walletSigner = WalletSigner(
                publicKey = publicKey,
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )

            retrieveApiResource { api.addWalletSigner(walletSigner) }
        } catch (e: Exception) {
            Resource.Error()
        }
    }

    override suspend fun regenerateAuthDataAndSaveKeyToUser(
        phrase: String,
        backendPublicKey: String,
        cryptoObject: BiometricPrompt.CryptoObject
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
            email = userEmail, cryptoObject = cryptoObject,
            privateKey = keyPair.privateKey,
            rootSeed = mnemonic.toSeed(),
            publicKey = keyPair.publicKey
        )
    }

    override suspend fun migrateOldDataToBiometryProtectedStorage(cryptoObject: BiometricPrompt.CryptoObject) {
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
                email = userEmail, cryptoObject = cryptoObject,
                privateKey = BaseWrapper.decode(oldPrivateKey),
                rootSeed = BaseWrapper.decode(oldRootSeed),
                publicKey = BaseWrapper.decode(publicKey)
            )

            securePreferences.clearDeprecatedPrivateKey(email = userEmail)
            securePreferences.clearDeprecatedRootSeed(email = userEmail)
        } catch (e: Exception) {
            encryptionManager.deleteBiometryKeyFromKeystore()
            throw e
        }
    }

    override suspend fun havePrivateKey(): Boolean {
        val userEmail = userRepository.retrieveUserEmail()
        return encryptionManager.havePrivateKeyStored(userEmail)
    }
}