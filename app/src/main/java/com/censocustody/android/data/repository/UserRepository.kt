package com.censocustody.android.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.provider.Settings
import com.censocustody.android.common.*
import com.censocustody.android.common.ui.generateUserImageObject
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.api.AnchorApiService
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.api.SemVersionApiService
import com.censocustody.android.data.cryptography.CryptographyManager
import com.censocustody.android.data.cryptography.EncryptionManager
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.recovery.OrgAdminRecoveredDeviceAndSigners
import com.censocustody.android.data.storage.AuthProvider
import com.censocustody.android.data.storage.SecurePreferences
import com.censocustody.android.data.storage.SharedPrefsHelper
import com.censocustody.android.data.storage.UserState
import com.censocustody.android.data.validator.NameAndModelProvider
import okhttp3.ResponseBody

interface UserRepository {
    suspend fun loginWithVerificationToken(email: String, token: String): Resource<LoginResponse>
    suspend fun loginWithTimestamp(email: String, timestamp: String, signedTimestamp: String): Resource<LoginResponse>
    suspend fun saveToken(token: String)
    suspend fun verifyUser(): Resource<VerifyUser>
    suspend fun getWalletSigners(): Resource<List<WalletSigner?>>
    suspend fun addWalletSigner(
        walletSigners: List<WalletSigner>,
        policy: ShardingPolicy?,
        rootSeed: ByteArray?,
    ): Resource<Unit>
    suspend fun addBootstrapUser(userImage: UserImage, walletSigners: List<WalletSigner>, rootSeed: ByteArray): Resource<Unit>
    suspend fun addOrgAdminRecoveredDevice(
        userImage: UserImage,
        walletSigners: List<WalletSigner>,
        rootSeed: ByteArray,
        policy: ShardingPolicy,
        shardingParticipantId: String,
        bootstrapShardingParticipantId: String?
    ): Resource<Unit>
    suspend fun userLoggedIn(): Boolean
    suspend fun setUserLoggedIn()
    suspend fun logOut(): Boolean
    suspend fun resetPassword(email: String) : ResponseBody
    suspend fun sendVerificationEmail(email: String) : Resource<ResponseBody>
    suspend fun retrieveUserEmail(): String
    suspend fun saveUserEmail(email: String)
    suspend fun checkMinimumVersion(): Resource<SemanticVersionResponse>
    suspend fun setKeyInvalidated()
    suspend fun retrieveUserDeviceId(email: String) : String
    suspend fun saveDeviceId(email: String, deviceId: String)
    suspend fun saveDevicePublicKey(email: String, publicKey: String)
    suspend fun saveBootstrapDeviceId(email: String, deviceId: String)
    suspend fun saveBootstrapDevicePublicKey(email: String, publicKey: String)
    suspend fun userHasDeviceIdSaved(email: String) : Boolean
    suspend fun userHasBootstrapDeviceIdSaved(email: String) : Boolean
    suspend fun addUserDevice(publicKey: String, userImage: UserImage) : Resource<Unit>
    suspend fun retrieveUserDevicePublicKey(email: String) : String
    suspend fun retrieveBootstrapDevicePublicKey(email: String) : String
    suspend fun clearLeftoverDeviceInfoIfPresent(email: String)
    suspend fun isTokenEmailVerified() : Boolean
    fun saveBootstrapImageUrl(email: String, bootstrapImageUrl: String)
    fun clearBootstrapImageUrl(email: String)
    fun retrieveBootstrapImageUrl(email: String) : String
    fun createUserImage(userPhoto: Bitmap, keyName: String) : UserImage
    fun getCachedUser() : VerifyUser?
    fun setCachedUser(verifyUser: VerifyUser?)
    fun clearPreviousDeviceId(email: String)
}

class UserRepositoryImpl(
    private val authProvider: AuthProvider,
    private val api: BrooklynApiService,
    private val securePreferences: SecurePreferences,
    private val anchorApiService: AnchorApiService,
    private val versionApiService: SemVersionApiService,
    private val encryptionManager: EncryptionManager,
    private val cryptographyManager: CryptographyManager,
    private val nameAndModelProvider: NameAndModelProvider,
    private val applicationContext: Context
) : UserRepository, BaseRepository() {

    private var censoUser: VerifyUser? = null

    override fun getCachedUser() = censoUser

    override fun setCachedUser(verifyUser: VerifyUser?) {
        censoUser = verifyUser
    }

    override fun clearPreviousDeviceId(email: String) {
        SharedPrefsHelper.clearPreviousDeviceId(email)
    }

    override suspend fun resetPassword(email: String) = anchorApiService.recoverPassword(email)

    override suspend fun sendVerificationEmail(email: String) =
        retrieveApiResource {
            api.sendVerificationEmail(TokenBody(email))
        }

    @SuppressLint("HardwareIds")
    override suspend fun loginWithVerificationToken(
        email: String,
        token: String
    ): Resource<LoginResponse> {
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        val loginBody = LoginBody(
            deviceId = deviceId,
            credentials = LoginCredentials(
                type = LoginType.EMAIL_VERIFICATION_BASED,
                email = email,
                verificationToken = token
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
        policy: ShardingPolicy?,
        rootSeed: ByteArray?
    ): Resource<Unit> {
        val email = retrieveUserEmail()
        val signedData = encryptionManager.signKeysForUpload(email, walletSigners)

        val share: Share? = policy?.let { _ ->
            rootSeed?.let { _ ->
                encryptionManager.createShare(
                    shardingPolicy = policy,
                    rootSeed = rootSeed
                )
            }
        }

        return retrieveApiResource {
            api.addWalletSigner(
                Signers(
                    signers = walletSigners,
                    signature = BaseWrapper.encodeToBase64(signedData),
                    share = share,
                )
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

        val nameAndModel = nameAndModelProvider.retrieveNameAndModel()

        val userDevice = UserDevice(
            userImage = userImage,
            deviceType = DeviceType.ANDROID,
            publicKey = devicePublicKey,
            name = nameAndModel.name,
            model = nameAndModel.model
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

    override suspend fun addOrgAdminRecoveredDevice(
        userImage: UserImage,
        walletSigners: List<WalletSigner>,
        rootSeed: ByteArray,
        policy: ShardingPolicy,
        shardingParticipantId: String,
        bootstrapShardingParticipantId: String?
    ): Resource<Unit> {
        val email = retrieveUserEmail()

        val signedDeviceData = encryptionManager.signKeysForUpload(
            email = email,
            walletSigners = walletSigners,
            bootstrapSign = false
        )

        val devicePublicKey = retrieveUserDevicePublicKey(email)

        val nameAndModel = nameAndModelProvider.retrieveNameAndModel()

        val userDevice = UserDevice(
            userImage = userImage,
            deviceType = DeviceType.ANDROID,
            publicKey = devicePublicKey,
            name = nameAndModel.name,
            model = nameAndModel.model
        )

        val share = encryptionManager.createShare(
            shardingPolicy = ShardingPolicy(
                policy.policyRevisionGuid,
                if (bootstrapShardingParticipantId != null) 1 else policy.threshold,
                policy.participants.filterNot {
                        participant -> participant.participantId == bootstrapShardingParticipantId
                }.map { participant ->
                    if (participant.participantId == shardingParticipantId) {
                        ShardingParticipant(participant.participantId, listOf(devicePublicKey))
                    } else participant
                }
            ),
            rootSeed = rootSeed
        )

        val signers = Signers(
            signers = walletSigners,
            signature = BaseWrapper.encodeToBase64(signedDeviceData),
            share = share
        )

        return retrieveApiResource {
            api.addOrgAdminRecoveredDeviceAndSigners(
                orgAdminRecoveredDeviceAndSigners = OrgAdminRecoveredDeviceAndSigners(
                    userDevice = userDevice,
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

    override fun saveBootstrapImageUrl(email: String, bootstrapImageUrl: String) {
        SharedPrefsHelper.saveBootstrapImageUrl(
            email = email,
            imageUrl = bootstrapImageUrl
        )
    }

    override fun retrieveBootstrapImageUrl(email: String) =
        SharedPrefsHelper.retrieveBootstrapImageUrl(email)

    override fun createUserImage(userPhoto: Bitmap, keyName: String) =
        generateUserImageObject(
            userPhoto = userPhoto,
            keyName = keyName,
            cryptographyManager = cryptographyManager
        )

    override fun clearBootstrapImageUrl(email: String) {
        SharedPrefsHelper.clearBootstrapImageUrl(email)
    }

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

    override suspend fun userHasBootstrapDeviceIdSaved(email: String) =
        SharedPrefsHelper.userHasBootstrapDeviceIdSaved(email)

    override suspend fun retrieveUserDevicePublicKey(email: String) =
        SharedPrefsHelper.retrieveDevicePublicKey(email)

    override suspend fun retrieveBootstrapDevicePublicKey(email: String) =
        SharedPrefsHelper.retrieveBootstrapDevicePublicKey(email)

    override suspend fun clearLeftoverDeviceInfoIfPresent(email: String) {
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

    override suspend fun isTokenEmailVerified(): Boolean {
        val token = authProvider.retrieveToken()
        return authProvider.isEmailVerifiedToken(token)
    }


    override suspend fun saveDeviceId(email: String, deviceId: String) {
        SharedPrefsHelper.saveDeviceId(email = email, deviceId = deviceId)
    }

    override suspend fun saveDevicePublicKey(email: String, publicKey: String) {
        SharedPrefsHelper.saveDevicePublicKey(email = email, publicKey = publicKey)
    }


    override suspend fun addUserDevice(publicKey: String, userImage: UserImage): Resource<Unit> {
        return retrieveApiResource {
            val email = retrieveUserEmail()
            val previousDeviceId = SharedPrefsHelper.retrievePreviousDeviceId(email)

            val nameAndModel = nameAndModelProvider.retrieveNameAndModel()

            val userDevice = UserDevice(
                publicKey = publicKey,
                userImage = userImage,
                deviceType = DeviceType.ANDROID,
                name = nameAndModel.name,
                model = nameAndModel.model,
                replacingDeviceIdentifier = previousDeviceId.ifEmpty { null }
            )

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