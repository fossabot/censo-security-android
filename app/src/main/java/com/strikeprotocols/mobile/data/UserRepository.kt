package com.strikeprotocols.mobile.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody

interface UserRepository {
    suspend fun loginWithPassword(email: String, password: String): Resource<LoginResponse>
    suspend fun loginWithTimestamp(email: String, timestamp: String, signedTimestamp: String): Resource<LoginResponse>
    suspend fun saveToken(token: String)
    suspend fun verifyUser(): Resource<VerifyUser>
    suspend fun getWalletSigners(): Resource<List<WalletSigner?>>
    suspend fun addWalletSigner(walletSignerBody: List<WalletSigner?>?): Resource<Signers>
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
    suspend fun retrieveUserDevicePublicKey(email: String) : String
    suspend fun addUserDevice(userDevice: UserDevice) : Resource<UserDevice>
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val securePreferences: SecurePreferences,
    private val anchorApiService: AnchorApiService,
    private val versionApiService: SemVersionApiService,
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

    override suspend fun addWalletSigner(walletSignerBody: List<WalletSigner?>?): Resource<Signers> =
        retrieveApiResource {
            api.addWalletSigner(
                Signers(walletSignerBody)
            )
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

    override suspend fun retrieveUserDevicePublicKey(email: String) =
        SharedPrefsHelper.retrieveDeviceId(email)

    override suspend fun addUserDevice(userDevice: UserDevice): Resource<UserDevice> {
        return retrieveApiResource {
            api.addUserDevice(userDevice)
        }
    }
}