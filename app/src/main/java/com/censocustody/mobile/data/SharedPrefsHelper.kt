package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"

    //Sentinel Data Storage
    private const val BGRD_INIT_VECTOR = "_bgrd_init_vector"
    private const val BGRD_CIPHER_TEXT = "_bgrd_cipher_text"

    //V1 Key Storage
    private const val V1_SOLANA_KEY = "_solana_key"
    private const val V1_ROOT_SEED = "_root_seed"

    //V2 Key Storage
    private const val V2_ROOT_SEED_PRIVATE_KEYS = "_key_data"
    private const val V2_SOLANA_PUBLIC_KEY = "_solana_public_key"

    //V3 Key Storage
    private const val V3_ROOT_SEED = "_v3_root_seed"
    private const val V3_ROOT_SEED_INIT_VECTOR = "_v3_root_seed_init_vector"
    private const val V3_PUBLIC_KEYS = "_v3_public_keys_list"

    private lateinit var appContext: Context
    private lateinit var sharedPrefs: SharedPreferences

    fun setup(context: Context) {
        appContext = context
        sharedPrefs = appContext.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE)
    }

    //region Sentinel Data Storage

    fun saveSentinelData(
        encryptedPrefs: SharedPreferences,
        email: String,
        encryptedData: EncryptedData
    ) {
        val initVector =
            if (encryptedData.initializationVector.isEmpty()) {
                ""
            } else {
                BaseWrapper.encode(encryptedData.initializationVector)
            }
        val cipherText =
            if (encryptedData.ciphertext.isEmpty()) {
                ""
            } else {
                BaseWrapper.encode(encryptedData.ciphertext)
            }
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BGRD_INIT_VECTOR", initVector)
        editor.putString("${email.lowercase().trim()}$BGRD_CIPHER_TEXT", cipherText)
        editor.apply()
    }

    fun retrieveSentinelData(
        encryptedPrefs: SharedPreferences,
        email: String
    ): EncryptedData {
        val savedInitVector =
            encryptedPrefs.getString("${email.lowercase().trim()}$BGRD_INIT_VECTOR", "") ?: ""
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$BGRD_CIPHER_TEXT", "") ?: ""

        return EncryptedData(
            initializationVector = BaseWrapper.decode(savedInitVector),
            ciphertext = BaseWrapper.decode(cipherText)
        )
    }

    fun clearSentinelData(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BGRD_INIT_VECTOR", "")
        editor.putString("${email.lowercase().trim()}$BGRD_CIPHER_TEXT", "")
        editor.apply()
    }

    fun hasSentinelData(encryptedPrefs: SharedPreferences, email: String): Boolean {
        val savedInitVector =
            encryptedPrefs.getString("${email.lowercase().trim()}$BGRD_INIT_VECTOR", "") ?: ""
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$BGRD_CIPHER_TEXT", "") ?: ""

        return savedInitVector.isNotEmpty() && cipherText.isNotEmpty()
    }

    //endregion

    //region V1 Storage
    fun retrieveV1RootSeed(encryptedPrefs: SharedPreferences, email: String) =
        encryptedPrefs.getString("${email.lowercase().trim()}$V1_ROOT_SEED", "") ?: ""

    fun clearV1RootSeed(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V1_ROOT_SEED", "")
        editor.apply()
    }

    fun retrieveV1PrivateKey(encryptedPrefs: SharedPreferences, email: String) =
        encryptedPrefs.getString("${email.lowercase().trim()}$V1_SOLANA_KEY", "") ?: ""

    fun clearV1SolanaPrivateKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V1_SOLANA_KEY", "")
        editor.apply()
    }
    //endregion

    //region V2 Storage
    fun retrieveV2SolanaPublicKey(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("${email.lowercase().trim()}$V2_SOLANA_PUBLIC_KEY", "") ?: ""
    }

    fun clearV2SolanaPublicKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V2_SOLANA_PUBLIC_KEY", "")
        editor.apply()
    }

    fun retrieveV2RootSeedAndPrivateKey(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("${email.lowercase().trim()}$V2_ROOT_SEED_PRIVATE_KEYS", "") ?: ""
    }

    fun clearV2RootSeedAndPrivateKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V2_ROOT_SEED_PRIVATE_KEYS", "")
        editor.apply()
    }

    //endregion

    //region V3 Storage
    fun saveV3PublicKeys(encryptedPrefs: SharedPreferences, email: String, keyData: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V3_PUBLIC_KEYS", keyData)
        editor.apply()
    }

    fun retrieveV3PublicKeys(encryptedPrefs: SharedPreferences, email: String): String? {
        return encryptedPrefs.getString("${email.lowercase().trim()}$V3_PUBLIC_KEYS", "")
    }

    fun clearV3PublicKeys(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V3_PUBLIC_KEYS", "")
        editor.apply()
    }

    fun saveV3RootSeed(
        encryptedPrefs: SharedPreferences,
        email: String,
        encryptedData: EncryptedData
    ) {
        val initVector =
            if (encryptedData.initializationVector.isEmpty()) {
                ""
            } else {
                BaseWrapper.encode(encryptedData.initializationVector)
            }
        val cipherText =
            if (encryptedData.ciphertext.isEmpty()) {
                ""
            } else {
                BaseWrapper.encode(encryptedData.ciphertext)
            }
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V3_ROOT_SEED_INIT_VECTOR", initVector)
        editor.putString("${email.lowercase().trim()}$V3_ROOT_SEED", cipherText)
        editor.apply()
    }

    fun retrieveV3RootSeed(
        encryptedPrefs: SharedPreferences,
        email: String
    ): EncryptedData {
        val savedInitVector =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED_INIT_VECTOR", "") ?: ""
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED", "") ?: ""

        return EncryptedData(
            initializationVector = BaseWrapper.decode(savedInitVector),
            ciphertext = BaseWrapper.decode(cipherText)
        )
    }

    fun hasV3RootSeed(encryptedPrefs: SharedPreferences, email: String): Boolean {
        val savedInitVector =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED_INIT_VECTOR", "") ?: ""
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED", "") ?: ""

        return savedInitVector.isNotEmpty() && cipherText.isNotEmpty()
    }

    fun clearV3RootSeed(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V3_ROOT_SEED_INIT_VECTOR", "")
        editor.putString("${email.lowercase().trim()}$V3_ROOT_SEED", "")
        editor.apply()
    }

    //endregion

    //region User Storage

    fun saveUserEmail(email: String) {
        if (email.isEmpty()) return
        val editor = sharedPrefs.edit()
        editor.putString(USER_EMAIL, email.lowercase().trim())
        editor.apply()
    }

    fun clearEmail() {
        val editor = sharedPrefs.edit()
        editor.putString(USER_EMAIL, "")
        editor.apply()
    }

    fun retrieveUserEmail(): String =
        sharedPrefs.getString(USER_EMAIL, "")?.lowercase()?.trim() ?: ""

    fun isUserLoggedIn() = sharedPrefs.getBoolean(USER_LOGGED_IN, false)

    fun setUserLoggedIn(loggedIn: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(USER_LOGGED_IN, loggedIn)
        editor.apply()
    }

    fun saveToken(encryptedPrefs: SharedPreferences, token: String) {
        val editor = encryptedPrefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun retrieveToken(encryptedPrefs: SharedPreferences): String {
        return encryptedPrefs.getString(USER_TOKEN, "") ?: ""
    }
    //endregion
}