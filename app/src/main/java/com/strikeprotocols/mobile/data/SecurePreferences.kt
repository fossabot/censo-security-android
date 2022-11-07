package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.strikeprotocols.mobile.data.SecurePreferencesImpl.Companion.SHARED_PREF_NAME
import com.strikeprotocols.mobile.data.models.StoredKeyData
import javax.inject.Inject

interface SecurePreferences {

    //user data
    fun saveToken(token: String)
    fun retrieveToken(): String
    fun clearToken()

    //v1 storage
    fun retrieveV1SolanaKey(email: String): String
    fun retrieveV1RootSeed(email: String): String
    fun clearV1SolanaKey(email: String)
    fun clearV1RootSeed(email: String)
    fun userHasV1RootSeedStored(email: String) : Boolean
    fun clearAllV1KeyData(email: String)

    //v2 storage
    fun clearAllV2KeyData(email: String)
    fun retrieveV2RootSeedAndPrivateKey(email: String): String
    fun retrieveV2SolanaPublicKey(email: String): String
    fun clearV2SolanaPublicKey(email: String)
    fun userHasV2RootSeedStored(email: String) : Boolean

    //v3 storage
    fun clearAllV3KeyData(email: String)
    fun retrieveV3RootSeed(email: String): EncryptedData
    fun saveV3RootSeed(email: String, encryptedData: EncryptedData)
    fun saveV3PublicKeys(email: String, keyJson: String)
    fun retrieveV3PublicKeys(email: String): HashMap<String, String>
    fun hasV3RootSeed(email: String) : Boolean

    //sentinel data
    fun retrieveSentinelData(email: String): EncryptedData
    fun saveSentinelData(email: String, encryptedData: EncryptedData)
    fun clearSentinelData(email: String)
    fun hasSentinelData(email: String): Boolean
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

    //region V1 Storage
    override fun retrieveV1SolanaKey(email: String) =
        SharedPrefsHelper.retrieveV1PrivateKey(secureSharedPreferences, email = email)

    override fun retrieveV1RootSeed(email: String) =
        SharedPrefsHelper.retrieveV1RootSeed(secureSharedPreferences, email = email)

    override fun clearAllV1KeyData(email: String) {
        clearV1RootSeed(email)
        clearV1SolanaKey(email)
    }

    override fun clearV1SolanaKey(email: String) {
        SharedPrefsHelper.clearV1SolanaPrivateKey(secureSharedPreferences, email = email)
    }

    override fun clearV1RootSeed(email: String) {
        SharedPrefsHelper.clearV1RootSeed(secureSharedPreferences, email = email)
    }

    override fun userHasV1RootSeedStored(email: String): Boolean {
        return SharedPrefsHelper.retrieveV1RootSeed(secureSharedPreferences, email = email).isNotEmpty()
    }
    //endregion

    //region V2 Storage
    override fun retrieveV2RootSeedAndPrivateKey(email: String): String {
        return SharedPrefsHelper.retrieveV2RootSeedAndPrivateKey(
            encryptedPrefs = secureSharedPreferences,
            email
        )
    }

    override fun retrieveV2SolanaPublicKey(email: String) =
        SharedPrefsHelper.retrieveV2SolanaPublicKey(
            encryptedPrefs = secureSharedPreferences, email = email
        )


    override fun clearV2SolanaPublicKey(email: String) {
        SharedPrefsHelper.clearV2SolanaPublicKey(
            encryptedPrefs = secureSharedPreferences,
            email = email
        )
    }

    override fun userHasV2RootSeedStored(email: String): Boolean {
        return SharedPrefsHelper.retrieveV2RootSeedAndPrivateKey(
            encryptedPrefs = secureSharedPreferences,
            email = email
        ).isNotEmpty()
    }

    override fun clearAllV2KeyData(email: String) {
        clearV2SolanaPublicKey(email)
        SharedPrefsHelper.clearV2RootSeedAndPrivateKey(secureSharedPreferences, email)
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

    override fun saveV3RootSeed(email: String, encryptedData: EncryptedData) {
        SharedPrefsHelper.saveV3RootSeed(
            email = email,
            encryptedPrefs = secureSharedPreferences,
            encryptedData = encryptedData
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

    object Companion {
        const val SHARED_PREF_NAME = "strike_secure_shared_pref"
    }
}