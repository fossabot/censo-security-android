package com.censocustody.android.data.models

import android.security.keystore.KeyPermanentlyInvalidatedException
import com.censocustody.android.data.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import java.security.InvalidAlgorithmParameterException

interface CipherRepository

class CipherRepositoryImpl(
    val userRepository: UserRepository,
    val encryptionManager: EncryptionManager,
    val securePreferences: SecurePreferences
) : CipherRepository, BaseRepository() {

    private suspend fun handleCipherException(e: Exception) {
        when (e) {
            is KeyPermanentlyInvalidatedException,
            is InvalidAlgorithmParameterException,
            is InvalidKeyPhraseException -> {
                wipeAllDataAfterKeyInvalidatedException()
            }
            else -> throw  e
        }
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