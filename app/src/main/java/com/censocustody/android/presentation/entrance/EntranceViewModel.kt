package com.censocustody.android.presentation.entrance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.BuildConfig
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.models.SemanticVersion
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.presentation.semantic_version_check.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntranceViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
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
                semanticVersion.exception ?: Exception("Error retrieving min version")
                    .sendError(CrashReportingUtil.FORCE_UPGRADE_TAG)
            }
        } catch (e: Exception) {
            checkLoggedIn()
            e.sendError(CrashReportingUtil.FORCE_UPGRADE_TAG)
        }
    }

    private suspend fun checkLoggedIn() {
        val userLoggedIn = try {
            userRepository.userLoggedIn()
        } catch (e: Exception) {
            false
        }

        if (userLoggedIn) {
            retrieveUserVerifyDetails()
        } else {
            //DESTINATION: Send user to login screen.
            state =
                state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
        }
    }

    private suspend fun doesDeviceNeedToBeRegistered(
        userEmail: String,
        verifyUser: VerifyUser
    ): Boolean {
        if (!userRepository.userHasDeviceIdSaved(userEmail)) return true

        if (userRepository.userHasBootstrapDeviceIdSaved(userEmail)) return false

        val devicePublicKey = userRepository.retrieveUserDevicePublicKey(userEmail)
        val backendPublicKey = verifyUser.deviceKeyInfo?.key

        return devicePublicKey.lowercase() != backendPublicKey?.lowercase()
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
        state = state.copy(verifyUserResult = verifyUserDataResource)
    }

    //2 part method.
    //Part 1: Do we need to update key/sentinel data?
    //Part 2: Is our local key valid? valid/invalid
    private suspend fun determineUserDestination(verifyUser: VerifyUser) {
        /**

        // User needs to add Sentinel data
        // User needs to register device
        // User needs to update backend with a new chain
        // User needs to create or recover root seed
            //Create
                // If cannot add signers and sharding policy: send to pending approval
                // If standard user and need to re-authenticate user: send to re-authenticate
                // Else: create key
            //Recover
                // If cannot add signers: send to pending approval
                // If need to re-auth: send to re-authenticate
                // Else: recover key
        // Bootstrap user needs to re-authenticate


         // None of above scenarios match:
            // User is good, check they have valid key and let them into app

        **/

        val email = userRepository.retrieveUserEmail()

        //Setup Data
        val userHasUploadedKeysToBackendBefore = !verifyUser.publicKeys.isNullOrEmpty()
        val userHasV3RootSeedSaved = keyRepository.hasV3RootSeedStored()
        val localPublicKeys = keyRepository.retrieveV3PublicKeys()

        //Device needs to be registered.
        val deviceNeedsToBeRegistered = doesDeviceNeedToBeRegistered(
            userEmail = email, verifyUser = verifyUser
        )

        //User's JWT token is email verified, so they need to do biometry login. This needs to happen before we send keys up to
        val doesUserNeedToReAuthenticate = userRepository.isTokenEmailVerified()

        val isBootstrapUser = userRepository.userHasBootstrapDeviceIdSaved(email)

        //Does this logged in user need to add sentinel data for background biometry
        val needToAddSentinelData = !keyRepository.haveSentinelData()

        //Backend missing one or more keys. This happens when we have added more keys to app since user initially uploaded keys.
        val needToUpdateKeysSavedOnBackend = userHasUploadedKeysToBackendBefore
                && userHasV3RootSeedSaved
                && verifyUser.userNeedsToUpdateKeyRegistration(localPublicKeys)

        //CREATE/RECOVER: This user does not have a local private key and needs to either create a key or recover a key
        val needToCreateRootSeed = !userHasUploadedKeysToBackendBefore
        val needToRecoverRootSeed = !userHasV3RootSeedSaved && userHasUploadedKeysToBackendBefore

        //DESTINATION: Any when clause will trigger a user navigating to destination
        when {
            needToAddSentinelData -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
                return
            }
            deviceNeedsToBeRegistered -> {
                state = state.copy(userDestinationResult = Resource.Success(UserDestination.DEVICE_REGISTRATION))
                return
            }

            needToUpdateKeysSavedOnBackend -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.UPLOAD_KEYS)
                )
                return
            }
            needToCreateRootSeed -> {
                state = if (!verifyUser.canAddSigners && verifyUser.shardingPolicy != null) {
                    state.copy(
                        userDestinationResult = Resource.Success(UserDestination.PENDING_APPROVAL)
                    )
                } else if (doesUserNeedToReAuthenticate && !isBootstrapUser) {
                    state =
                        state.copy(userDestinationResult = Resource.Success(UserDestination.RE_AUTHENTICATE))
                    return
                } else {
                    val bootstrapImageUrl =
                        if (verifyUser.shardingPolicy == null) userRepository.retrieveBootstrapImageUrl(
                            email
                        ) else ""

                    state.copy(
                        userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_CREATION),
                        bootstrapImageUrl = bootstrapImageUrl
                    )
                }
                return
            }
            needToRecoverRootSeed -> {
                state =
                    if (!verifyUser.canAddSigners) {
                        state.copy(
                            userDestinationResult = Resource.Success(UserDestination.PENDING_APPROVAL)
                        )
                    } else if (doesUserNeedToReAuthenticate) {
                        state =
                            state.copy(userDestinationResult = Resource.Success(UserDestination.RE_AUTHENTICATE))
                        return
                    } else {
                        state.copy(
                            userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_RECOVERY)
                        )
                    }
                return
            }

            isBootstrapUser && doesUserNeedToReAuthenticate -> {
                state =
                    state.copy(userDestinationResult = Resource.Success(UserDestination.RE_AUTHENTICATE))
                return
            }
        }
        //endregion

        //Passed all checks, lets check if our local key is valid.
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