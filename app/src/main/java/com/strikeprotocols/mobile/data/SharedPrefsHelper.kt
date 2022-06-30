package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val MAIN_KEY = "_main_key"
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

    fun saveMainKey(encryptedPrefs: SharedPreferences, email: String, mainKey: ByteArray) {
        val data = if (mainKey.isEmpty()) "" else BaseWrapper.encode(mainKey)
        val editor = encryptedPrefs.edit()
        editor.putString("$email$MAIN_KEY", data)
        editor.apply()
    }

    fun retrieveMainKey(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("$email$MAIN_KEY", "") ?: ""
    }

    fun clearMainKey(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("$email$MAIN_KEY", "")
        editor.apply()
    }

    fun saveRootSeed(encryptedPrefs: SharedPreferences, email: String, rootSeed: ByteArray) {
        val data = if (rootSeed.isEmpty()) "" else BaseWrapper.encode(rootSeed)
        val editor = encryptedPrefs.edit()
        editor.putString("$email$ROOT_SEED", data)
        editor.apply()
    }

    fun retrieveRootSeed(encryptedPrefs: SharedPreferences, email: String): String {
        return encryptedPrefs.getString("$email$ROOT_SEED", "") ?: ""
    }

    fun clearRootSeed(encryptedPrefs: SharedPreferences, email: String) {
        val editor = encryptedPrefs.edit()
        editor.putString("$email$ROOT_SEED", "")
        editor.apply()
    }

    fun saveUserEmail(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun clearEmail() {
        val editor = sharedPrefs.edit()
        editor.putString(USER_EMAIL, "")
        editor.apply()
    }

    fun retrieveUserEmail(): String =
        sharedPrefs.getString(USER_EMAIL, "") ?: ""


}