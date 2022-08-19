package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import com.strikeprotocols.mobile.data.SecurePreferencesImpl.Companion.SHARED_PREF_NAME
import javax.inject.Inject

interface SecurePreferences {
    fun saveToken(token: String)
    fun retrieveToken() : String
    fun clearToken()
    fun saveSolanaKey(email: String, privateKey: ByteArray)
    fun retrieveSolanaKey(email: String): String
    fun clearSolanaKey(email: String)
    fun saveRootSeed(email: String, rootSeed: ByteArray)
    fun retrieveRootSeed(email: String) : String
    fun clearRootSeed(email: String)
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

    override fun saveSolanaKey(email: String, privateKey: ByteArray) {
        SharedPrefsHelper.saveSolanaKey(
            encryptedPrefs = secureSharedPreferences,
            email = email,
            solanaKey = privateKey
        )
    }

    override fun retrieveSolanaKey(email: String) =
        SharedPrefsHelper.retrieveSolanaKey(encryptedPrefs = secureSharedPreferences, email)

    override fun clearSolanaKey(email: String) {
        SharedPrefsHelper.clearSolanaKey(encryptedPrefs = secureSharedPreferences, email = email)
    }

    override fun saveRootSeed(email: String, rootSeed: ByteArray) {
        SharedPrefsHelper.saveRootSeed(
            encryptedPrefs = secureSharedPreferences,
            email = email,
            rootSeed = rootSeed
        )
    }

    override fun retrieveRootSeed(email: String) =
        SharedPrefsHelper.retrieveRootSeed(encryptedPrefs = secureSharedPreferences, email)

    override fun clearRootSeed(email: String) {
        SharedPrefsHelper.clearRootSeed(encryptedPrefs = secureSharedPreferences, email = email)
    }

    object Companion {
        const val SHARED_PREF_NAME = "strike_secure_shared_pref"
    }
}