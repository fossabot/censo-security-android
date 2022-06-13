package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.strikeprotocols.mobile.common.BaseWrapper

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val MAIN_KEY = "_main_key"

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

    fun saveMainKey(email: String, mainKey: ByteArray) {
        val data = if(mainKey.isEmpty()) "" else BaseWrapper.encode(mainKey)
        val editor = sharedPrefs.edit()
        editor.putString("$email$MAIN_KEY", data)
        editor.apply()
    }

    fun retrieveMainKey(email: String) : String {
        return sharedPrefs.getString("$email$MAIN_KEY", "") ?: ""
    }

    fun clearMainKey(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("$email$MAIN_KEY", "")
        editor.apply()    }

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