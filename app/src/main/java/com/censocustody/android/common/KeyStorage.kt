package com.censocustody.android.common

import com.censocustody.android.data.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import javax.crypto.Cipher
import javax.inject.Inject

interface KeyStorage {
    fun savePublicKeys(email: String, keyJson: String)
    fun saveRootSeed(email: String, rootSeed: ByteArray)
    fun retrieveSentinelData(email: String, cipher: Cipher): ByteArray
    fun retrieveRootSeed(email: String): ByteArray
    fun saveSentinelData(email: String, cipher: Cipher)
    fun hasSentinelData(email: String): Boolean
}

class KeyStorageImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val cryptographyManager: CryptographyManager,
) : KeyStorage {

    override fun savePublicKeys(email: String, keyJson: String) {
        securePreferences.saveV3PublicKeys(email = email, keyJson = keyJson)
    }

    override fun saveRootSeed(email: String, rootSeed: ByteArray) {
        cryptographyManager.getOrCreateKey(ROOT_SEED_KEY_NAME)
        val encryptedRootSeed =
            cryptographyManager.encryptDataLocal(
                keyName = ROOT_SEED_KEY_NAME,
                plainText = BaseWrapper.encode(rootSeed)
            )

        securePreferences.saveV3RootSeed(email = email, ciphertext = encryptedRootSeed)
    }

    override fun retrieveRootSeed(email: String): ByteArray {
        val savedRootSeedData = securePreferences.retrieveV3RootSeed(email)

        val decryptedRootSeed = cryptographyManager.decryptData(
            keyName = ROOT_SEED_KEY_NAME,
            ciphertext = savedRootSeedData,
        )

        return BaseWrapper.decode(String(decryptedRootSeed, Charsets.UTF_8))
    }

    override fun retrieveSentinelData(email: String, cipher: Cipher): ByteArray {
        val savedSentinelData = securePreferences.retrieveSentinelData(email)

        return cryptographyManager.decryptSentinelData(
            ciphertext = savedSentinelData.ciphertext,
            cipher = cipher
        )
    }

    override fun saveSentinelData(email: String, cipher: Cipher) {
        cryptographyManager.getOrCreateSentinelKey(email)
        val encryptedSentinelData =
            cryptographyManager.encryptSentinelData(cipher)

        securePreferences.saveSentinelData(
            email = email, encryptedData = encryptedSentinelData
        )
    }

    override fun hasSentinelData(email: String) = securePreferences.hasSentinelData(email)
}