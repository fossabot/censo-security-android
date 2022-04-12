package com.strikeprotocols.mobile.data

import javax.inject.Inject

interface SecurePreferences {
    fun saveGeneratedPassword(email: String, generatedPassword: ByteArray)
    fun retrieveGeneratedPassword(email: String): String
    fun clearSavedPassword(email: String)
    fun savePrivateKey(email: String, privateKey: ByteArray)
    fun retrievePrivateKey(email: String): String
    fun clearPrivateKey(email: String)
}

class SecurePreferencesImpl @Inject constructor() :
    SecurePreferences {

    override fun saveGeneratedPassword(email: String, generatedPassword: ByteArray) {
        SharedPrefsHelper.saveGeneratedPassword(email, generatedPassword)
    }

    override fun retrieveGeneratedPassword(email: String) =
        SharedPrefsHelper.retrieveGeneratedPassword(email)

    override fun clearSavedPassword(email: String) {
        SharedPrefsHelper.clearGeneratedPassword(email)
    }

    override fun savePrivateKey(email: String, privateKey: ByteArray) {
        SharedPrefsHelper.saveEncryptedKey(email = email, encryptedKey = privateKey)
    }

    override fun retrievePrivateKey(email: String) =
        SharedPrefsHelper.retrieveEncryptedKey(email)

    override fun clearPrivateKey(email: String) {
        SharedPrefsHelper.saveEncryptedKey(email = email, encryptedKey = byteArrayOf())
    }
}