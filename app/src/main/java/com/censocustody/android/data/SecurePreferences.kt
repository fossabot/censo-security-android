package com.censocustody.android.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.censocustody.android.data.SecurePreferencesImpl.Companion.SHARED_PREF_NAME
import com.censocustody.android.data.models.StoredKeyData
import javax.inject.Inject

interface SecurePreferences {

    //user data
    fun saveToken(token: String)
    fun retrieveToken(): String
    fun clearToken()

    //v3 storage
    fun clearAllV3KeyData(email: String)
    fun retrieveV3RootSeed(email: String): ByteArray
    fun saveV3RootSeed(email: String, ciphertext: ByteArray)
    fun saveV3PublicKeys(email: String, keyJson: String)
    fun retrieveV3PublicKeys(email: String): HashMap<String, String>
    fun hasV3RootSeed(email: String) : Boolean

    //sentinel data
    fun retrieveSentinelData(email: String): EncryptedData
    fun saveSentinelData(email: String, encryptedData: EncryptedData)
    fun clearSentinelData(email: String)
    fun hasSentinelData(email: String): Boolean

    //device key data
    fun clearDeviceKeyData(email: String)
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

    //region User Data Storage

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

    //endregion

    //region V3 Storage
    override fun clearAllV3KeyData(email: String) {
        SharedPrefsHelper.clearV3PublicKeys(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )
        SharedPrefsHelper.clearV3RootSeed(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )
    }

    override fun retrieveV3RootSeed(email: String) =
        SharedPrefsHelper.retrieveV3RootSeed(
            encryptedPrefs = secureSharedPreferences, email = email
        )

    override fun hasV3RootSeed(email: String) =
        SharedPrefsHelper.hasV3RootSeed(
            encryptedPrefs = secureSharedPreferences, email = email
        )

    override fun saveV3RootSeed(email: String, ciphertext: ByteArray) {
        SharedPrefsHelper.saveV3RootSeed(
            email = email,
            encryptedPrefs = secureSharedPreferences,
            ciphertext = ciphertext
        )
    }

    override fun saveV3PublicKeys(email: String, keyJson: String) {
        SharedPrefsHelper.saveV3PublicKeys(
            encryptedPrefs = secureSharedPreferences,
            email = email,
            keyData = keyJson
        )
    }

    override fun retrieveV3PublicKeys(email: String): HashMap<String, String> {
        val publicKeys = SharedPrefsHelper.retrieveV3PublicKeys(
            encryptedPrefs = secureSharedPreferences,
            email
        ) ?: ""

        if (publicKeys.isEmpty()) return hashMapOf()

        return StoredKeyData.mapFromJson(publicKeys)
    }
    //endregion

    //region Sentinel Data Storage

    override fun saveSentinelData(email: String, encryptedData: EncryptedData) {
        SharedPrefsHelper.saveSentinelData(
            email = email,
            encryptedPrefs = secureSharedPreferences,
            encryptedData = encryptedData
        )
    }

    override fun retrieveSentinelData(email: String): EncryptedData =
        SharedPrefsHelper.retrieveSentinelData(
            encryptedPrefs = secureSharedPreferences, email = email
        )


    override fun clearSentinelData(email: String) {
        SharedPrefsHelper.clearSentinelData(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )
    }

    override fun hasSentinelData(email: String) =
        SharedPrefsHelper.hasSentinelData(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )

    //endregion

    //region Device Key Data
    override fun clearDeviceKeyData(email: String) {
        SharedPrefsHelper.clearDeviceId(email)
        SharedPrefsHelper.clearDevicePublicKey(email)
    }
    //endregion

    object Companion {
        const val SHARED_PREF_NAME = "censo_secure_shared_pref"
    }
}