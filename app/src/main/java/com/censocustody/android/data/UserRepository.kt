package com.censocustody.android.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.*
import com.censocustody.android.data.models.*
import okhttp3.ResponseBody
import java.security.Signature

interface UserRepository {
    suspend fun loginWithPassword(email: String, password: String): Resource<LoginResponse>
    suspend fun loginWithTimestamp(email: String, timestamp: String, signedTimestamp: String): Resource<LoginResponse>
    suspend fun saveToken(token: String)
    suspend fun verifyUser(): Resource<VerifyUser>
    suspend fun getWalletSigners(): Resource<List<WalletSigner?>>
    suspend fun addWalletSigner(walletSigners: List<WalletSigner>, signature: Signature): Resource<Signers>
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut(): Boolean
    suspend fun resetPassword(email: String) : ResponseBody
    suspend fun retrieveUserEmail(): String
    fun retrieveCachedUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun checkMinimumVersion(): Resource<SemanticVersionResponse>
    suspend fun setKeyInvalidated()
    suspend fun setInvalidSentinelData()
    suspend fun retrieveUserDeviceId(email: String) : String
    suspend fun userHasDeviceIdSaved(email: String) : Boolean
    suspend fun addUserDevice(userDevice: UserDevice) : Resource<UserDevice>
    suspend fun retrieveUserDevicePublicKey(email: String) : String
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val securePreferences: SecurePreferences,
    private val anchorApiService: AnchorApiService,
    private val versionApiService: SemVersionApiService,
    private val encryptionManager: EncryptionManager,
    private val applicationContext: Context
) : UserRepository, BaseRepository() {

    override suspend fun resetPassword(email: String) = anchorApiService.recoverPassword(email)

    @SuppressLint("HardwareIds")
    override suspend fun loginWithPassword(
        email: String,
        password: String
    ): Resource<LoginResponse> {
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        val loginBody = LoginBody(
            deviceId = deviceId,
            credentials = LoginCredentials(
                type = LoginType.PASSWORD_BASED,
                email = email,
                password = password
            )
        )

        return retrieveApiResource { api.login(loginBody = loginBody) }
    }

    @SuppressLint("HardwareIds")
    override suspend fun loginWithTimestamp(
        email: String,
        timestamp: String,
        signedTimestamp: String
    ): Resource<LoginResponse> {
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        val loginBody = LoginBody(
            deviceId = deviceId,
            credentials = LoginCredentials(
                type = LoginType.SIGNATURE_BASED,
                email = email,
                timestamp = timestamp,
                timestampSignature = signedTimestamp,
            )
        )

        return retrieveApiResource { api.login(loginBody = loginBody) }
    }

    override suspend fun saveToken(token: String) {
        securePreferences.saveToken(token = token)
    }

    override suspend fun verifyUser(): Resource<VerifyUser> =
        retrieveApiResource { api.verifyUser() }

    override suspend fun getWalletSigners(): Resource<List<WalletSigner?>> =
        retrieveApiResource { api.walletSigners() }

    override suspend fun addWalletSigner(walletSigners: List<WalletSigner>, signature: Signature): Resource<Signers> {
        val email = retrieveUserEmail()
        val signedData = encryptionManager.signKeysForUpload(email, signature, walletSigners)
        return retrieveApiResource {
            api.addWalletSigner(
                Signers(walletSigners, BaseWrapper.encodeToBase64(signedData))
            )
        }
    }

    override suspend fun retrieveUserEmail(): String {
        return try {
            authProvider.retrieveUserEmail()
        } catch (e: Exception) {
            ""
        }
    }

    override fun retrieveCachedUserEmail() = SharedPrefsHelper.retrieveUserEmail()

    override suspend fun saveUserEmail(email: String) {
        SharedPrefsHelper.saveUserEmail(email)
    }

    override suspend fun userLoggedIn() = SharedPrefsHelper.isUserLoggedIn()
    override suspend fun setUserLoggedIn() = SharedPrefsHelper.setUserLoggedIn(true)

    override suspend fun logOut(): Boolean {
        return try {
            authProvider.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun checkMinimumVersion(): Resource<SemanticVersionResponse> {
        return retrieveApiResource {
            versionApiService.getMinimumVersion()
        }
    }

    override suspend fun setKeyInvalidated() {
        authProvider.setUserState(userState = UserState.INVALIDATED_KEY)
    }

    override suspend fun setInvalidSentinelData() {
        authProvider.setUserState(userState = UserState.INVALID_SENTINEL_DATA)
    }

    override suspend fun retrieveUserDeviceId(email: String) =
        SharedPrefsHelper.retrieveDeviceId(email)

    override suspend fun userHasDeviceIdSaved(email: String) =
        SharedPrefsHelper.userHasDeviceIdSaved(email)

    override suspend fun retrieveUserDevicePublicKey(email: String): String {
        return SharedPrefsHelper.retrieveDevicePublicKey(email)
    }

    override suspend fun addUserDevice(userDevice: UserDevice): Resource<UserDevice> {
        return retrieveApiResource {
            api.addUserDevice(userDevice)
        }
    }
}