package com.strikeprotocols.mobile.presentation.entrance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.CrashReportingUtil
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.models.SemanticVersion
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.presentation.semantic_version_check.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    //Part 1: Do we need to update key data? create/upload/regenerate/migrate
    //Part 2: Is our local key valid? valid/invalid
    private suspend fun determineUserDestination(verifyUser: VerifyUser) {
        //region Part 1: Check 5 scenarios: Sentinel data, migration, missing keys, never uploaded, and create/recover.
        //Setup Data
        val userHasUploadedKeysToBackendBefore = !verifyUser.publicKeys.isNullOrEmpty()
        val userHasLocallySavedPrivateKeys = keyRepository.havePrivateKeys()

        val localPublicKeys = keyRepository.retrieveV3PublicKeys()

        //Check 1: Does this logged in user need to add sentinel data for background biometry
        val userDoesNotHaveSentinelData = !keyRepository.haveSentinelData()

        //Check 2: Does this user have any leftover v1/v2 data and need to migrate to v3 storage
        val userHasV1KeyData = keyRepository.doesUserHaveV1KeyData()
        val userHasV2KeyData = keyRepository.doesUserHaveV2KeyData()

        //Check 3: Does this user have extra local keys and need to sign + upload these keys to backend
        val needToUpdateKeysSavedOnBackend = userHasUploadedKeysToBackendBefore &&
                verifyUser.determineKeysUserNeedsToUpload(localPublicKeys).isNotEmpty()

        //Check 4: User has saved local keys but has not uploaded them to backend
        val doesUserNeedToUploadKeyToBackend = !userHasUploadedKeysToBackendBefore && userHasLocallySavedPrivateKeys

        //Check 5: This user does not have a local private key and needs to either create a key or recover a key
        val doesUserNeedToCreateKey = !userHasUploadedKeysToBackendBefore && !userHasLocallySavedPrivateKeys
        val doesUserNeedToRecoverKey = !userHasLocallySavedPrivateKeys && userHasUploadedKeysToBackendBefore

        when {
            userDoesNotHaveSentinelData -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
                return
            }
            userHasV1KeyData || userHasV2KeyData || needToUpdateKeysSavedOnBackend -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MIGRATION)
                )
                return
            }
            doesUserNeedToCreateKey -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_CREATION)
                )
                return
            }
            doesUserNeedToRecoverKey -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_RECOVERY),
                )
                return
            }
            doesUserNeedToUploadKeyToBackend -> {
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