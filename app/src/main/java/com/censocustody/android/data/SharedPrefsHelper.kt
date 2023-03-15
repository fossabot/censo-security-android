package com.censocustody.android.data

import android.content.Context
import android.content.SharedPreferences
import com.censocustody.android.common.BaseWrapper

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_SEEN_PERMISSION_DIALOG = "user_seen_permission_dialog"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"
    private const val DEVICE_ID = "_device_id"
    private const val DEVICE_PUBLIC_KEY = "_device_public_key"

    //Sentinel Data Storage
    private const val BGRD_INIT_VECTOR = "_bgrd_init_vector"
    private const val BGRD_CIPHER_TEXT = "_bgrd_cipher_text"

    //V3 Key Storage
    private const val V3_ROOT_SEED = "_v3_root_seed"
    private const val V3_ROOT_SEED_INIT_VECTOR = "_v3_root_seed_init_vector"
    private const val V3_PUBLIC_KEYS = "_v3_public_keys_list"

    private const val SENTINEL_ID = "_sentinel_id"
    private const val ROOT_SEED_ID = "_root_seed_id"

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
        ciphertext: ByteArray
    ) {
        val cipherText =
            if (ciphertext.isEmpty()) {
                ""
            } else {
                BaseWrapper.encode(ciphertext)
            }
        val editor = encryptedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$V3_ROOT_SEED", cipherText)
        editor.apply()
    }

    fun retrieveV3RootSeed(
        encryptedPrefs: SharedPreferences,
        email: String
    ): ByteArray {
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED", "") ?: ""

        return BaseWrapper.decode(cipherText)
    }

    fun hasV3RootSeed(encryptedPrefs: SharedPreferences, email: String): Boolean {
        val cipherText =
            encryptedPrefs.getString("${email.lowercase().trim()}$V3_ROOT_SEED", "") ?: ""

        return cipherText.isNotEmpty()
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

    //region push dialog
    fun userHasSeenPermissionDialog(): Boolean {
        return sharedPrefs.getBoolean(USER_SEEN_PERMISSION_DIALOG, false)
    }

    fun setUserSeenPermissionDialog(seenDialog: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(USER_SEEN_PERMISSION_DIALOG, seenDialog)
        editor.apply()
    }
    //endregion

    //region device id
    fun clearDeviceId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEVICE_ID", "")
        editor.apply()
    }

    fun saveDeviceId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEVICE_ID", deviceId)
        editor.apply()
    }

    fun userHasDeviceIdSaved(email: String) = retrieveDeviceId(email).isNotEmpty()

    fun retrieveDeviceId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$DEVICE_ID", "") ?: ""
    }
    //endregion

    //region public key for device id
    fun clearDevicePublicKey(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEVICE_PUBLIC_KEY", "")
        editor.apply()
    }

    fun saveDevicePublicKey(
        email: String,
        publicKey: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$DEVICE_PUBLIC_KEY", publicKey)
        editor.apply()
    }

    fun retrieveDevicePublicKey(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$DEVICE_PUBLIC_KEY", "") ?: ""
    }
    //endregion

    //region root seed id
    fun clearRootSeedId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ROOT_SEED_ID", "")
        editor.apply()
    }

    fun saveRootSeedId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ROOT_SEED_ID", deviceId)
        editor.apply()
    }

    fun userHasRootSeedIdSaved(email: String) = retrieveDeviceId(email).isNotEmpty()

    fun retrieveRootSeedId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$ROOT_SEED_ID", "") ?: ""
    }
    //endregion

    //region sentinel id
    fun clearSentinelId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SENTINEL_ID", "")
        editor.apply()
    }

    fun saveSentinelId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$SENTINEL_ID", deviceId)
        editor.apply()
    }

    fun userHasSentinelIdSaved(email: String) = retrieveSentinelId(email).isNotEmpty()

    fun retrieveSentinelId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$SENTINEL_ID", "") ?: ""
    }
    //endregion
}