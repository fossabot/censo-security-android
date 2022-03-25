package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_LOGGED_IN = "skipped_login"

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
}