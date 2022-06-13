package com.strikeprotocols.mobile.data

import javax.inject.Inject

interface SecurePreferences {
    fun savePrivateKey(email: String, privateKey: ByteArray)
    fun retrievePrivateKey(email: String): String
    fun clearPrivateKey(email: String)
}

class SecurePreferencesImpl @Inject constructor() :
    SecurePreferences {

    override fun savePrivateKey(email: String, privateKey: ByteArray) {
        SharedPrefsHelper.saveMainKey(email = email, mainKey = privateKey)
    }

    override fun retrievePrivateKey(email: String) =
        SharedPrefsHelper.retrieveMainKey(email)

    override fun clearPrivateKey(email: String) {
        SharedPrefsHelper.clearMainKey(email = email)
    }
}