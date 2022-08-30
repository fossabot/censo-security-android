package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.strikeprotocols.mobile.data.SecurePreferencesImpl.Companion.SHARED_PREF_NAME
import javax.inject.Inject

interface SecurePreferences {
    fun saveToken(token: String)
    fun retrieveToken(): String
    fun clearToken()
    fun retrieveDeprecatedPrivateKey(email: String): String
    fun retrieveDeprecatedRootSeed(email: String): String
    fun clearDeprecatedPrivateKey(email: String)
    fun clearDeprecatedRootSeed(email: String)
    fun retrieveEncryptedStoredKeys(email: String): String
    fun savePublicKey(email: String, publicKey: ByteArray)
    fun retrievePublicKey(email: String): String
    fun clearPublicKey(email: String)
    fun saveAllRelevantKeyData(
        email: String,
        publicKey: ByteArray,
        keyStorageJson: String,
    )

    fun clearAllRelevantKeyData(email: String)
}

class SecurePreferencesImpl @Inject constructor(applicationContext: Context) :
    SecurePreferences {

    private val masterKeyAlias: MasterKey =
        MasterKey.Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private var secureSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        applicationContext,
        SHARED_PREF_NAME,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveToken(token: String) {
        SharedPrefsHelper.saveToken(
            encryptedPrefs = secureSharedPreferences,
            token = token
        )
    }

    override fun retrieveToken() =
        SharedPrefsHelper.retrieveToken(encryptedPrefs = secureSharedPreferences)

    override fun clearToken() {
        SharedPrefsHelper.saveToken(secureSharedPreferences, "")
    }

    override fun retrieveDeprecatedPrivateKey(email: String) =
        SharedPrefsHelper.retrieveDeprecatedPrivateKey(secureSharedPreferences, email = email)

    override fun retrieveDeprecatedRootSeed(email: String) =
        SharedPrefsHelper.retrieveDeprecatedRootSeed(secureSharedPreferences, email = email)

    override fun clearDeprecatedPrivateKey(email: String) {
        SharedPrefsHelper.clearDeprecatedSolanaPrivateKey(secureSharedPreferences, email = email)
    }

    override fun clearDeprecatedRootSeed(email: String) {
        SharedPrefsHelper.clearDeprecatedRootSeed(secureSharedPreferences, email = email)
    }

    override fun retrieveEncryptedStoredKeys(email: String): String {
        return SharedPrefsHelper.retrieveKeyData(encryptedPrefs = secureSharedPreferences, email)
            ?: ""
    }

    override fun savePublicKey(email: String, publicKey: ByteArray) {
        SharedPrefsHelper.saveSolanaPublicKey(
            encryptedPrefs = secureSharedPreferences,
            email = email,
            publicKey = publicKey
        )
    }

    override fun retrievePublicKey(email: String) =
        SharedPrefsHelper.retrieveSolanaPublicKey(
            encryptedPrefs = secureSharedPreferences, email = email
        )


    override fun clearPublicKey(email: String) {
        SharedPrefsHelper.clearSolanaPublicKey(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )
    }

    override fun saveAllRelevantKeyData(
        email: String,
        publicKey: ByteArray,
        keyStorageJson: String,
    ) {
        savePublicKey(email = email, publicKey = publicKey)

        SharedPrefsHelper.saveKeyData(
            encryptedPrefs = secureSharedPreferences,
            email = email,
            keyData = keyStorageJson
        )
    }

    override fun clearAllRelevantKeyData(email: String) {
        clearPublicKey(email)
        SharedPrefsHelper.clearKeyData(secureSharedPreferences, email)
    }

    object Companion {
        const val SHARED_PREF_NAME = "strike_secure_shared_pref"
    }
}