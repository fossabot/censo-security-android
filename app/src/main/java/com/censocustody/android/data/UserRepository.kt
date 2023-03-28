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
    suspend fun addWalletSigner(
        walletSigners: List<WalletSigner>,
        policy: ShardingPolicy,
        rootSeed: ByteArray,
    ): Resource<Unit>
    suspend fun addBootstrapUser(userImage: UserImage, walletSigners: List<WalletSigner>, rootSeed: ByteArray): Resource<Unit>
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut(): Boolean
    suspend fun resetPassword(email: String) : ResponseBody
    suspend fun retrieveUserEmail(): String
    fun retrieveCachedUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun checkMinimumVersion(): Resource<SemanticVersionResponse>
    suspend fun setKeyInvalidated()
    suspend fun retrieveUserDeviceId(email: String) : String
    suspend fun saveDeviceId(email: String, deviceId: String)
    suspend fun saveDevicePublicKey(email: String, publicKey: String)
    suspend fun saveBootstrapDeviceId(email: String, deviceId: String)
    suspend fun saveBootstrapDevicePublicKey(email: String, publicKey: String)
    suspend fun userHasDeviceIdSaved(email: String) : Boolean
    suspend fun addUserDevice(userDevice: UserDevice) : Resource<Unit>
    suspend fun retrieveUserDevicePublicKey(email: String) : String
    suspend fun retrieveBootstrapDevicePublicKey(email: String) : String
    suspend fun clearPreviousDeviceInfo(email: String)
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val securePreferences: SecurePreferences,
    private val anchorApiService: AnchorApiService,
    private val versionApiService: SemVersionApiService,
    private val encryptionManager: EncryptionManager,
    private val cryptographyManager: CryptographyManager,
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

    override suspend fun addWalletSigner(
        walletSigners: List<WalletSigner>,
        policy: ShardingPolicy,
        rootSeed: ByteArray): Resource<Unit> {
        val email = retrieveUserEmail()
        val signedData = encryptionManager.signKeysForUpload(email, walletSigners)

        val share = encryptionManager.createShareForStandardUser(
            policy = policy,
            rootSeed = rootSeed
        )

        return retrieveApiResource {
            api.addWalletSigner(
                Signers(
                    signers = walletSigners,
                    signature = BaseWrapper.encodeToBase64(signedData),
                    share = null)
            )
        }
    }

    override suspend fun addBootstrapUser(
        userImage: UserImage,
        walletSigners: List<WalletSigner>,
        rootSeed: ByteArray
    ): Resource<Unit> {
        val email = retrieveUserEmail()

        val signedDeviceData = encryptionManager.signKeysForUpload(
            email = email,
            walletSigners = walletSigners,
            bootstrapSign = false
        )
        val signedBootstrapData = encryptionManager.signKeysForUpload(
            email = email,
            walletSigners = walletSigners,
            bootstrapSign = true
        )

        val bootstrapPublicKey = retrieveBootstrapDevicePublicKey(email)
        val devicePublicKey = retrieveUserDevicePublicKey(email)

        val userDevice = UserDevice(
            userImage = userImage,
            deviceType = DeviceType.ANDROID,
            publicKey = devicePublicKey
        )

        val bootstrapDevice = BootstrapDevice(
            publicKey = bootstrapPublicKey,
            signature = BaseWrapper.encodeToBase64(signedBootstrapData)
        )

        val share = encryptionManager.createShareForBootstrapUser(
            email = email,
            rootSeed = rootSeed
        )

        val signers = Signers(
            signers = walletSigners,
            signature = BaseWrapper.encodeToBase64(signedDeviceData),
            share = share
        )

        return retrieveApiResource {
            api.addBootstrapUserDeviceAndSigners(
                userDeviceAndSigners = BootstrapUserDeviceAndSigners(
                    userDevice = userDevice,
                    bootstrapDevice = bootstrapDevice,
                    signersInfo = signers
                )
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

    override suspend fun retrieveUserDeviceId(email: String) =
        SharedPrefsHelper.retrieveDeviceId(email)

    override suspend fun userHasDeviceIdSaved(email: String) =
        SharedPrefsHelper.userHasDeviceIdSaved(email)

    override suspend fun retrieveUserDevicePublicKey(email: String) =
        SharedPrefsHelper.retrieveDevicePublicKey(email)

    override suspend fun retrieveBootstrapDevicePublicKey(email: String) =
        SharedPrefsHelper.retrieveBootstrapDevicePublicKey(email)

    override suspend fun clearPreviousDeviceInfo(email: String) {
        if (SharedPrefsHelper.userHasDeviceIdSaved(email)) {
            val oldDeviceId = SharedPrefsHelper.retrieveDeviceId(email)

            cryptographyManager.deleteKeyIfPresent(oldDeviceId)

            SharedPrefsHelper.clearDeviceId(email)
            SharedPrefsHelper.clearDevicePublicKey(email)
        }

        if (SharedPrefsHelper.userHasBootstrapDeviceIdSaved(email)) {
            val oldBootstrapDeviceId = SharedPrefsHelper.retrieveBootstrapDeviceId(email)

            cryptographyManager.deleteKeyIfPresent(oldBootstrapDeviceId)

            SharedPrefsHelper.clearBootstrapDeviceId(email)
            SharedPrefsHelper.clearDeviceBootstrapPublicKey(email)
        }
    }


    override suspend fun saveDeviceId(email: String, deviceId: String) {
        SharedPrefsHelper.saveDeviceId(email = email, deviceId = deviceId)
    }

    override suspend fun saveDevicePublicKey(email: String, publicKey: String) {
        SharedPrefsHelper.saveDevicePublicKey(email = email, publicKey = publicKey)
    }


    override suspend fun addUserDevice(userDevice: UserDevice): Resource<Unit> {
        return retrieveApiResource {
            api.addUserDevice(userDevice)
        }
    }

    override suspend fun saveBootstrapDeviceId(email: String, deviceId: String) {
        SharedPrefsHelper.saveBootstrapDeviceId(email = email, deviceId = deviceId)
    }

    override suspend fun saveBootstrapDevicePublicKey(email: String, publicKey: String) {
        SharedPrefsHelper.saveBootstrapDevicePublicKey(email = email, publicKey = publicKey)
    }
}