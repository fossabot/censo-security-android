package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.StrikeEncryptedSharedPreferences.Companion.GENERATED_PASSWORD
import com.strikeprotocols.mobile.data.StrikeEncryptedSharedPreferences.Companion.SHARED_PREF_NAME
import javax.inject.Inject

interface SecurePreferences {
    fun saveGeneratedPassword(generatedPassword: ByteArray)
    fun retrieveGeneratedPassword(): String
    fun clearSavedPassword()
}


class StrikeEncryptedSharedPreferences @Inject constructor(applicationContext: Context) :
    SecurePreferences {
    private val masterKeyAlias: MasterKey =
        MasterKey.Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()


    var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        applicationContext,
        SHARED_PREF_NAME,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveGeneratedPassword(generatedPassword: ByteArray) {
        sharedPreferences
            .edit()
            .putString(GENERATED_PASSWORD, BaseWrapper.encode(generatedPassword))
            .apply()
    }

    override fun retrieveGeneratedPassword() =
        sharedPreferences.getString(GENERATED_PASSWORD, "") ?: ""

    override fun clearSavedPassword() {
        sharedPreferences.edit().putString(GENERATED_PASSWORD, "").apply()
    }

    object Companion {
        const val SHARED_PREF_NAME = "strike_secure_shared_pref"
        const val GENERATED_PASSWORD = "generated_password"
    }

}