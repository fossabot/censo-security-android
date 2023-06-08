package com.censocustody.android.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.cryptography.EncryptedData

object SharedPrefsHelper {

    private const val MAIN_PREFS = "main_prefs"

    private const val USER_SEEN_PERMISSION_DIALOG = "user_seen_permission_dialog"

    private const val USER_LOGGED_IN = "skipped_login"
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"
    private const val DEVICE_ID = "_device_id"
    private const val DEVICE_PUBLIC_KEY = "_device_public_key"
    private const val BOOTSTRAP_IMAGE_LOCATION = "_bootstrap_image_location"

    //Sentinel Data Storage
    private const val BGRD_INIT_VECTOR = "_bgrd_init_vector"
    private const val BGRD_CIPHER_TEXT = "_bgrd_cipher_text"

    //Bootstrap Device Data Storage
    private const val BOOTSTRAP_DEVICE_ID = "_bootstrap_device_id"
    private const val BOOTSTRAP_DEVICE_PUBLIC_KEY = "_bootstrap_device_public_key"

    //Bootstrap Device Data Storage
    private const val ORG_DEVICE_ID = "_org_device_id"
    private const val ORG_DEVICE_PUBLIC_KEY = "_org_device_public_key"


    //V3 Key Storage
    private const val V3_ROOT_SEED = "_v3_root_seed"
    private const val V3_ROOT_SEED_INIT_VECTOR = "_v3_root_seed_init_vector"
    private const val V3_PUBLIC_KEYS = "_v3_public_keys_list"

    private const val PREVIOUS_DEVICE_ID = "_previous_device_id"

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


    //region bootstrap device id
    fun clearBootstrapDeviceId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_ID", "")
        editor.apply()
    }

    fun saveBootstrapDeviceId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_ID", deviceId)
        editor.apply()
    }

    fun userHasBootstrapDeviceIdSaved(email: String) = retrieveBootstrapDeviceId(email).isNotEmpty()

    fun retrieveBootstrapDeviceId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_ID", "") ?: ""
    }
    //endregion


    //region public key for bootstrap device id
    fun clearDeviceBootstrapPublicKey(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_PUBLIC_KEY", "")
        editor.apply()
    }

    fun saveBootstrapDevicePublicKey(
        email: String,
        publicKey: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_PUBLIC_KEY", publicKey)
        editor.apply()
    }

    fun retrieveBootstrapDevicePublicKey(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$BOOTSTRAP_DEVICE_PUBLIC_KEY", "") ?: ""
    }
    //endregion

    //region bootstrap user image URL
    fun clearBootstrapImageUrl(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_IMAGE_LOCATION", "")
        editor.apply()
    }

    fun saveBootstrapImageUrl(
        email: String,
        imageUrl: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$BOOTSTRAP_IMAGE_LOCATION", imageUrl)
        editor.apply()
    }

    fun userHasBootstrapImageUrl(email: String) = retrieveBootstrapImageUrl(email).isNotEmpty()

    fun retrieveBootstrapImageUrl(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$BOOTSTRAP_IMAGE_LOCATION", "") ?: ""
    }
    //endregion


    //region previous device id
    fun clearPreviousDeviceId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$PREVIOUS_DEVICE_ID", "")
        editor.apply()
    }

    fun savePreviousDeviceId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$PREVIOUS_DEVICE_ID", deviceId)
        editor.apply()
    }

    fun userHasPreviousDeviceIdSaved(email: String) = retrievePreviousDeviceId(email).isNotEmpty()

    fun retrievePreviousDeviceId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$PREVIOUS_DEVICE_ID", "") ?: ""
    }
    //endregion

    //region org device id
    fun clearOrgDeviceId(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ORG_DEVICE_ID", "")
        editor.apply()
    }

    fun saveOrgDeviceId(
        email: String,
        deviceId: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ORG_DEVICE_ID", deviceId)
        editor.apply()
    }

    fun userHasOrgDeviceIdSaved(email: String) = retrieveOrgDeviceId(email).isNotEmpty()

    fun retrieveOrgDeviceId(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$ORG_DEVICE_ID", "") ?: ""
    }
    //endregion


    //region public key for org device id
    fun clearDeviceOrgPublicKey(email: String) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ORG_DEVICE_PUBLIC_KEY", "")
        editor.apply()
    }

    fun saveOrgDevicePublicKey(
        email: String,
        publicKey: String
    ) {
        val editor = sharedPrefs.edit()
        editor.putString("${email.lowercase().trim()}$ORG_DEVICE_PUBLIC_KEY", publicKey)
        editor.apply()
    }

    fun retrieveOrgDevicePublicKey(
        email: String
    ): String {
        return sharedPrefs.getString("${email.lowercase().trim()}$ORG_DEVICE_PUBLIC_KEY", "") ?: ""
    }
    //endregion
}