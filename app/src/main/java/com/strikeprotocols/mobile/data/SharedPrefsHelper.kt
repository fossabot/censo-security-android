package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.strikeLog

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val GENERATED_PASSWORD = "_generated_password"
    private const val ENCRYPTED_KEY = "_encrypted_key"

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

    fun saveGeneratedPassword(email: String, generatedPassword: ByteArray) {
        val editor = sharedPrefs.edit()
        editor.putString("$email$GENERATED_PASSWORD", BaseWrapper.encode(generatedPassword))
        editor.apply()
    }

    fun clearGeneratedPassword(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("$email$GENERATED_PASSWORD", "")
        editor.apply()
    }

    fun retrieveGeneratedPassword(email: String) =
        sharedPrefs.getString("$email$GENERATED_PASSWORD", "") ?: ""

    fun saveEncryptedKey(email: String, encryptedKey: String) {
        val editor = sharedPrefs.edit()
        editor.putString("$email$ENCRYPTED_KEY", encryptedKey)
        editor.apply()
    }

    fun retrieveEncryptedKey(email: String) =
        sharedPrefs.getString("$email$ENCRYPTED_KEY", "") ?: ""

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