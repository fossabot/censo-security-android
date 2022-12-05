package com.censocustody.mobile.presentation.entrance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.censocustody.mobile.BuildConfig
import com.censocustody.mobile.common.CrashReportingUtil
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.*
import com.censocustody.mobile.data.models.SemanticVersion
import com.censocustody.mobile.data.models.VerifyUser
import com.censocustody.mobile.presentation.semantic_version_check.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntranceViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val censoUserData: CensoUserData
) : ViewModel() {

    var state by mutableStateOf(EntranceState())
        private set


    fun onStart() {
        viewModelScope.launch {
            checkMinimumVersion()
        }
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
            censoUserData.setEmail(userRepository.retrieveCachedUserEmail())
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
                censoUserData.setCensoUser(verifyUser = verifyUser)
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
        censoUserData.setCensoUser(null)
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