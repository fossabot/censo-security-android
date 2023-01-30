package com.censocustody.android.data.models

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.censocustody.android.data.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import java.security.InvalidAlgorithmParameterException
import java.security.Signature
import javax.crypto.Cipher

interface CipherRepository {
    suspend fun getCipherForEncryption(keyName: String): Cipher?
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
            handleCipherException(e)
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
            handleCipherException(e)
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
            handleCipherException(e)
        }
    }

    override suspend fun getSignatureForDeviceSigning(keyName: String): Signature? {
        return try {
            encryptionManager.getSignatureForDeviceSigning(keyName)
        } catch (e: Exception) {
            handleCipherException(e = e)
            null
        }
    }

    private suspend fun handleCipherException(e: Exception): Cipher? {
        when (e) {
            is KeyPermanentlyInvalidatedException,
            is InvalidAlgorithmParameterException,
            is InvalidKeyPhraseException -> {
                wipeAllDataAfterKeyInvalidatedException()
            }
            else -> throw  e
        }
        return null
    }

    private suspend fun wipeAllDataAfterKeyInvalidatedException() {
        val email = userRepository.retrieveUserEmail()
        encryptionManager.deleteBiometryKeyFromKeystore(BIO_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(SENTINEL_KEY_NAME)
        encryptionManager.deleteBiometryKeyFromKeystore(ROOT_SEED_KEY_NAME)
        securePreferences.clearAllV3KeyData(email)
        securePreferences.clearSentinelData(email)
        deleteDeviceKeyInfoWhenBiometryInvalidated(email)
        userRepository.logOut()
        userRepository.setKeyInvalidated()
    }

    private fun deleteDeviceKeyInfoWhenBiometryInvalidated(email: String) {
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email)

        if (deviceId.isNotEmpty()) {
            encryptionManager.deleteKeyIfInKeystore(deviceId)
        }
        securePreferences.clearDeviceKeyData(email)
    }
}