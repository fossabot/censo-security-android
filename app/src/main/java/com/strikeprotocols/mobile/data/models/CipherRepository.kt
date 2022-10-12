package com.strikeprotocols.mobile.data.models

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.PRIVATE_KEYS_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import java.security.InvalidAlgorithmParameterException
import javax.crypto.Cipher

interface CipherRepository {
    suspend fun getCipherForEncryption(keyName: String): Cipher?
    suspend fun getCipherForBackgroundDecryption(): Cipher?
    suspend fun getCipherForV2KeysDecryption(): Cipher?
    suspend fun getCipherForV3RootSeedDecryption(): Cipher?
    suspend fun getCipherForV3PrivateKeysDecryption(): Cipher?
}

class CipherRepositoryImpl(
    val userRepository: UserRepository,
    val encryptionManager: EncryptionManager,
    val securePreferences: SecurePreferences
) : CipherRepository, BaseRepository() {

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
                initVector = encryptedData.initializationVector,
                keyName = SENTINEL_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, SENTINEL_KEY_NAME)
        }
    }

    override suspend fun getCipherForV2KeysDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveV2RootSeedAndPrivateKey(email)
            val storedKeyData = StoredKeyData.fromJson(encryptedData)
            val initVector = BaseWrapper.decode(storedKeyData.initVector)
            encryptionManager.getInitializedCipherForDecryption(
                initVector = initVector, keyName = BIO_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, BIO_KEY_NAME)
        }
    }

    override suspend fun getCipherForV3RootSeedDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveV3RootSeed(email)
            encryptionManager.getInitializedCipherForDecryption(
                initVector = encryptedData.initializationVector,
                keyName = ROOT_SEED_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, ROOT_SEED_KEY_NAME)
        }
    }

    override suspend fun getCipherForV3PrivateKeysDecryption(): Cipher? {
        return try {
            val email = userRepository.retrieveUserEmail()
            val encryptedData = securePreferences.retrieveV3PrivateKeys(email)
            val storedKeyData = StoredKeyData.fromJson(encryptedData)
            val initVector = BaseWrapper.decode(storedKeyData.initVector)
            encryptionManager.getInitializedCipherForDecryption(
                initVector = initVector,
                keyName = PRIVATE_KEYS_KEY_NAME
            )
        } catch (e: Exception) {
            handleCipherException(e, PRIVATE_KEYS_KEY_NAME)
        }
    }

    private suspend fun handleCipherException(e: Exception, keyName: String): Cipher? {
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
        encryptionManager.deleteBiometryKeyFromKeystore(PRIVATE_KEYS_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(ROOT_SEED_KEY_NAME)
        securePreferences.clearAllV2KeyData(email)
        securePreferences.clearAllV3KeyData(email)
        securePreferences.clearSentinelData(email)
        if (keyName == SENTINEL_KEY_NAME) {
            userRepository.setInvalidSentinelData()
        } else {
            userRepository.logOut()
            userRepository.setKeyInvalidated()
        }
    }
}