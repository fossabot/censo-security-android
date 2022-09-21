package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper
import org.web3j.abi.datatypes.Bool

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"
    private const val KEY_DATA = "_key_data"
    private const val SOLANA_PUBLIC_KEY = "_solana_public_key"
    private const val BGRD_INIT_VECTOR = "_bgrd_init_vector"
    private const val BGRD_CIPHER_TEXT = "_bgrd_cipher_text"

    private const val DEPRECATED_SOLANA_PRIVATE_KEY = "_solana_key"
    private const val DEPRECATED_ROOT_SEED = "_root_seed"

    private lateinit var appContext: Context
    private lateinit var sharedPrefs: SharedPreferences

    fun setup(context: Context) {
        appContext = context
        sharedPrefs = appContext.getSharedPreferences(MAIN_PREFS, Context.MODE_PRIVATE)
    }

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

    fun saveSolanaPublicKey(
        encryptedPrefs: SharedPreferences,
        email: String,
        publicKey: ByteArray
    ) {
        val data = if (publicKey.isEmpty()) "" else BaseWrapper.encode(publicKey)
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SOLANA_PUBLIC_KEY", data)
        editor.apply()
    }

    fun retrieveSolanaPublicKey(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("${email.lowercase().trim()}$SOLANA_PUBLIC_KEY", "") ?: ""
    }

    fun clearSolanaPublicKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SOLANA_PUBLIC_KEY", "")
        editor.apply()
    }

    fun saveBackgroundEncryptedData(
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

    fun retrieveBackgroundEncryptedData(
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

    fun clearBackgroundEncryptedData(encryptedPrefs: SharedPreferences, email: String) {
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

    fun saveKeyData(encryptedPrefs: SharedPreferences, email: String, keyData: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$KEY_DATA", keyData)
        editor.apply()
    }

    fun retrieveKeyData(encryptedPrefs: SharedPreferences, email: String): String? {
        return encryptedPrefs.getString("${email.lowercase().trim()}$KEY_DATA", "")
    }

    fun clearKeyData(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$KEY_DATA", "")
        editor.apply()
    }

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

    fun retrieveDeprecatedRootSeed(encryptedPrefs: SharedPreferences, email: String) =
        encryptedPrefs.getString("${email.lowercase().trim()}$DEPRECATED_ROOT_SEED", "") ?: ""

    fun clearDeprecatedRootSeed(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEPRECATED_ROOT_SEED", "")
        editor.apply()
    }

    fun retrieveDeprecatedPrivateKey(encryptedPrefs: SharedPreferences, email: String) =
        encryptedPrefs.getString("${email.lowercase().trim()}$DEPRECATED_SOLANA_PRIVATE_KEY", "") ?: ""

    fun clearDeprecatedSolanaPrivateKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEPRECATED_SOLANA_PRIVATE_KEY", "")
        editor.apply()
    }

    fun retrieveUserEmail(): String =
        sharedPrefs.getString(USER_EMAIL, "")?.lowercase()?.trim() ?: ""

}