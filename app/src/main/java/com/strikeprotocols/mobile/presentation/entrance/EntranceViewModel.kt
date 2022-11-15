package com.strikeprotocols.mobile.presentation.entrance

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.CrashReportingUtil
import com.strikeprotocols.mobile.common.CrashReportingUtil.HARDWARE_BACKED_TAG
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.models.SemanticVersion
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.presentation.semantic_version_check.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.inject.Inject

@HiltViewModel
class EntranceViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val strikeUserData: StrikeUserData
) : ViewModel() {

    var state by mutableStateOf(EntranceState())
        private set


    fun onStart() {
        viewModelScope.launch {
            checkIfHardwareBackedStorage()
        }
    }

    private fun checkIfHardwareBackedStorage() {
        try {
            val secretKey = getOrCreateSecretKey("TESTER_KEY")
            val factory = SecretKeyFactory.getInstance(secretKey.algorithm, "AndroidKeyStore")
            val keyInfo: KeyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                state =
                    if (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT || keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX) {
                        state.copy(isKeyHardwareBacked = Resource.Success(true))
                    } else {
                        state.copy(isKeyHardwareBacked = Resource.Success(false))
                    }
            } else {
                @Suppress("DEPRECATION")
                state =
                    state.copy(isKeyHardwareBacked = Resource.Success(keyInfo.isInsideSecureHardware))
            }

        } catch (e: Exception) {
            RaygunClient.send(e, listOf(HARDWARE_BACKED_TAG))
            state = state.copy(isKeyHardwareBacked = Resource.Error(exception = e))
            strikeLog(message = e.stackTraceToString())
        }
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        val key = keyStore.getKey(keyName, null)

        if (key != null) return key as SecretKey

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE

        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(256)
            setUserAuthenticationRequired(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    private suspend fun checkMinimumVersion() {
        try {
            val semanticVersion = userRepository.checkMinimumVersion()
            if (semanticVersion is Resource.Success) {
                enforceMinimumVersion(
                    minimumSemanticVersion = semanticVersion.data?.androidVersion?.minimumVersion
                        ?: MainViewModel.BACKUP_VERSION
                )
            } else if (semanticVersion is Resource.Error) {
                checkLoggedIn()
                RaygunClient.send(
                    semanticVersion.exception ?: Exception("Error retrieving min version"), listOf(
                        CrashReportingUtil.FORCE_UPGRADE_TAG,
                        CrashReportingUtil.MANUALLY_REPORTED_TAG
                    )
                )
            }
        } catch (e: Exception) {
            checkLoggedIn()
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.FORCE_UPGRADE_TAG,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
        }
    }

    private suspend fun checkLoggedIn() {
        val userLoggedIn = try {
            userRepository.userLoggedIn()
        } catch (e: Exception) {
            false
        }

        if (userLoggedIn) {
            strikeUserData.setEmail(userRepository.retrieveCachedUserEmail())
            retrieveUserVerifyDetails()
        } else {
            //DESTINATION: Send user to login screen.
            state =
                state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
        }
    }

    private suspend fun enforceMinimumVersion(minimumSemanticVersion: String) {
        val appVersion = SemanticVersion.parse(BuildConfig.VERSION_NAME)

        val minimumVersion = SemanticVersion.parse(minimumSemanticVersion)

        val forceUpdate = appVersion.compareTo(minimumVersion)

        if (forceUpdate < 0) {
            //DESTINATION: Send user to force update screen.
            state =
                state.copy(
                    userDestinationResult = Resource.Success(UserDestination.FORCE_UPDATE)
                )
        } else {
            checkLoggedIn()
        }
    }

    private suspend fun retrieveUserVerifyDetails() {
        val verifyUserDataResource = userRepository.verifyUser()

        if (verifyUserDataResource is Resource.Success) {
            val verifyUser = verifyUserDataResource.data

            if (verifyUser != null) {
                strikeUserData.setStrikeUser(verifyUser = verifyUser)
                state = state.copy(verifyUserResult = verifyUserDataResource)
                determineUserDestination(verifyUser)
            } else {
                handleVerifyUserError(verifyUserDataResource)
            }

        } else if (verifyUserDataResource is Resource.Error) {
            handleVerifyUserError(verifyUserDataResource)
        }

    }

    fun retryRetrieveVerifyUserDetails() {
        resetVerifyUserResult()
        viewModelScope.launch {
            retrieveUserVerifyDetails()
        }
    }

    private fun handleVerifyUserError(verifyUserDataResource: Resource<VerifyUser>) {
        strikeUserData.setStrikeUser(null)
        state = state.copy(verifyUserResult = verifyUserDataResource)
    }

    //2 part method.
    //Part 1: Do we need to update key/sentinel data?
    //Part 2: Is our local key valid? valid/invalid
    private suspend fun determineUserDestination(verifyUser: VerifyUser) {
        //region Part 1:
        // Check 4 scenarios:
        // 1. User needs to add Sentinel data,
        // 2. User needs to either migrate data or update backend with a new chain,
        // 3. User needs to create or recover root seed
        // 4. User has local data save but never uploaded to backend

        //Setup Data
        val userHasUploadedKeysToBackendBefore = !verifyUser.publicKeys.isNullOrEmpty()
        val userHasLocallySavedRootSeed = keyRepository.haveARootSeedStored()
        val userHasV3RootSeedSaved = keyRepository.hasV3RootSeedStored()
        val localPublicKeys = keyRepository.retrieveV3PublicKeys()

        //Check 1 SENTINEL: Does this logged in user need to add sentinel data for background biometry
        val needToAddSentinelData = !keyRepository.haveSentinelData()

        //Check 2 MIGRATION/UPDATE: Migration of either v1/v2 data or need to add a new chain to public keys
        val needToUpdateKeysSavedOnBackend = userHasUploadedKeysToBackendBefore
                && userHasLocallySavedRootSeed
                && verifyUser.userNeedsToUpdateKeyRegistration(localPublicKeys)

        //Check 3 CREATE/RECOVER: This user does not have a local private key and needs to either create a key or recover a key
        val needToCreateRootSeed = !userHasV3RootSeedSaved && !userHasUploadedKeysToBackendBefore
        val needToRecoverRootSeed = !userHasV3RootSeedSaved && userHasUploadedKeysToBackendBefore

        //Check 4 REGENERATE: User has saved local keys but has not uploaded them to backend (rare, but can happen when a user failed to upload data API call)
        val needToUploadPublicKeyData = !userHasUploadedKeysToBackendBefore && userHasV3RootSeedSaved

        //DESTINATION: Any when clause will trigger a user navigating to destination
        when {
            needToAddSentinelData -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
                return
            }
            needToUpdateKeysSavedOnBackend -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MIGRATION)
                )
                return
            }
            needToCreateRootSeed -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_CREATION)
                )
                return
            }
            needToRecoverRootSeed -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_RECOVERY),
                )
                return
            }
            needToUploadPublicKeyData -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.REGENERATION)
                )
                return
            }
        }
        //endregion

        //region Part 2: Passed all checks, lets check if our our local key valid?
        val doesUserHaveValidLocalKey =
            keyRepository.doesUserHaveValidLocalKey(verifyUser)

        //DESTINATION: User has valid key saved and we let them into the app
        state = if (doesUserHaveValidLocalKey) {
            state.copy(
                userDestinationResult = Resource.Success(UserDestination.HOME)
            )
            //DESTINATION: User has invalid local key saved and we send them to support screen
        } else {
            state.copy(
                userDestinationResult = Resource.Success(UserDestination.INVALID_KEY)
            )
        }
        //endregion
    }

    fun resetUserDestinationResult() {
        state = state.copy(userDestinationResult = Resource.Uninitialized)
    }

    private fun resetVerifyUserResult() {
        state = state.copy(verifyUserResult = Resource.Uninitialized)
    }
}