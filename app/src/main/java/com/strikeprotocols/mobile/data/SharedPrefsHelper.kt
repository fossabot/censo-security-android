package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"
    private const val SOLANA_KEY = "_solana_key"
    private const val ROOT_SEED = "_root_seed"

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

    fun saveSolanaKey(encryptedPrefs: SharedPreferences, email: String, solanaKey: ByteArray) {
        val data = if (solanaKey.isEmpty()) "" else BaseWrapper.encode(solanaKey)
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SOLANA_KEY", data)
        editor.apply()
    }

    fun retrieveSolanaKey(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("${email.lowercase().trim()}$SOLANA_KEY", "") ?: ""
    }

    fun clearSolanaKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SOLANA_KEY", "")
        editor.apply()
    }

    fun saveRootSeed(encryptedPrefs: SharedPreferences, email: String, rootSeed: ByteArray) {
        val data = if (rootSeed.isEmpty()) "" else BaseWrapper.encode(rootSeed)
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ROOT_SEED", data)
        editor.apply()
    }

    fun retrieveRootSeed(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("${email.lowercase().trim()}$ROOT_SEED", "") ?: ""
    }

    fun clearRootSeed(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ROOT_SEED", "")
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

    fun retrieveUserEmail(): String =
        sharedPrefs.getString(USER_EMAIL, "")?.lowercase()?.trim() ?: ""


}