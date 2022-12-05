package com.censocustody.mobile.data.models

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.censocustody.mobile.data.*
import com.censocustody.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.censocustody.mobile.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import com.censocustody.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import java.security.InvalidAlgorithmParameterException
import javax.crypto.Cipher

interface CipherRepository {
    suspend fun getCipherForEncryption(keyName: String): Cipher?
    suspend fun getCipherForBackgroundDecryption(): Cipher?
    suspend fun getCipherForV3RootSeedDecryption(): Cipher?
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
        encryptionManager.deleteBiometryKeyFromKeystore(ROOT_SEED_KEY_NAME)
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