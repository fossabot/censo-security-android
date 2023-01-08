package com.censocustody.android.data.models

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.censocustody.android.data.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import java.security.InvalidAlgorithmParameterException
import java.security.Signature
import javax.crypto.Cipher

interface CipherRepository {
    suspend fun getCipherForEncryption(keyName: String): Cipher?
    suspend fun getCipherForDeviceKeyEncryption(keyName: String): Cipher?
    suspend fun getCipherForBackgroundDecryption(): Cipher?
    suspend fun getCipherForV3RootSeedDecryption(): Cipher?
    suspend fun getSignatureForDeviceSigning(keyName: String): Signature?
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

    override suspend fun getCipherForDeviceKeyEncryption(keyName: String): Cipher? {
        return try {
            encryptionManager.getInitializedCipherForDeviceKeyEncryption(keyName)
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
        var deviceId = ""
        return try {
            val email = userRepository.retrieveUserEmail()
            deviceId = userRepository.retrieveUserDeviceId(email)
            val encryptedData = securePreferences.retrieveV3RootSeed(email)
            encryptionManager.getInitializedCipherForDeviceKeyDecryption(
                initVector = encryptedData.initializationVector,
                keyName = deviceId
            )
        } catch (e: Exception) {
            handleCipherException(e, deviceId)
        }
    }

    override suspend fun getSignatureForDeviceSigning(keyName: String): Signature {
        return encryptionManager.getSignatureForDeviceSigning(keyName)
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
        val deviceId = userRepository.retrieveUserDeviceId(email)
        encryptionManager.deleteBiometryKeyFromKeystore(BIO_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(SENTINEL_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(deviceId)
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