package com.censocustody.android.common

import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.EncryptionManagerImpl
import com.censocustody.android.data.SecurePreferences
import javax.crypto.Cipher
import javax.inject.Inject

interface KeyStorage {
    fun savePublicKeys(email: String, keyJson: String)
    fun saveRootSeed(email: String, cipher: Cipher, rootSeed: ByteArray)
    fun retrieveSentinelData(email: String, cipher: Cipher): ByteArray
    fun retrieveRootSeed(email: String, cipher: Cipher): ByteArray
    fun saveSentinelData(email: String, cipher: Cipher)
    fun hasSentinelData(email: String): Boolean
}

class KeyStorageImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val cryptographyManager: CryptographyManager
) : KeyStorage {
    override fun savePublicKeys(email: String, keyJson: String) {
        securePreferences.saveV3PublicKeys(email = email, keyJson = keyJson)
    }

    override fun saveRootSeed(email: String, cipher: Cipher, rootSeed: ByteArray) {
        val encryptedRootSeed =
            cryptographyManager.encryptData(data = BaseWrapper.encode(rootSeed), cipher = cipher)

        securePreferences.saveV3RootSeed(email = email, encryptedData = encryptedRootSeed)
    }

    override fun retrieveSentinelData(email: String, cipher: Cipher): ByteArray {
        val savedSentinelData = securePreferences.retrieveSentinelData(email)

        return cryptographyManager.decryptData(
            ciphertext = savedSentinelData.ciphertext,
            cipher = cipher
        )
    }

    override fun retrieveRootSeed(email: String, cipher: Cipher): ByteArray {
        val savedRootSeedData = securePreferences.retrieveV3RootSeed(email)

        //todo: Byte array format
        return cryptographyManager.decryptData(
            ciphertext = savedRootSeedData.ciphertext,
            cipher = cipher
        )
    }

    override fun saveSentinelData(email: String, cipher: Cipher) {
        val encryptedSentinelData =
            cryptographyManager.encryptData(
                data = EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA,
                cipher = cipher
            )

        securePreferences.saveSentinelData(email = email, encryptedData = encryptedSentinelData)
    }

    override fun hasSentinelData(email: String) = securePreferences.hasSentinelData(email)


}