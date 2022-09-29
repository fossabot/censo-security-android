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
import com.strikeprotocols.mobile.data.models.WalletSigner
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
        resetWalletSignersCall()
        viewModelScope.launch {
            retrieveUserVerifyDetails()
        }
    }

    private fun handleVerifyUserError(verifyUserDataResource: Resource<VerifyUser>) {
        strikeUserData.setStrikeUser(null)
        state = state.copy(verifyUserResult = verifyUserDataResource)
    }

    private fun handleWalletSignersError(walletSignersDataResource: Resource<List<WalletSigner?>>) {
        strikeUserData.setStrikeUser(null)
        state = if (walletSignersDataResource is Resource.Error) {
            state.copy(walletSignersResult = walletSignersDataResource)
        } else {
            state.copy(
                walletSignersResult =
                Resource.Error(exception = Exception("Null wallet signers"))
            )
        }
    }

    //2 part method.
    //Part 1: Do we need to update key data? create/upload/regenerate
    //Part 2: Is our local key valid? valid/invalid
    private suspend fun determineUserDestination(verifyUser: VerifyUser) {
        val userDoesNotHaveSentinelData = !keyRepository.haveSentinelData()
        val userSavedPrivateKey = keyRepository.havePrivateKey()
        val publicKeysPresent = !verifyUser.publicKeys.isNullOrEmpty()

        //region PART 1: Do we need to update our key data or save sentinel data?
        val doesUserNeedToCreateKey = !publicKeysPresent && !userSavedPrivateKey
        val doesUserNeedToUploadKeyToBackend = !publicKeysPresent && userSavedPrivateKey
        val doesUserNeedToRecoverKey = !userSavedPrivateKey && publicKeysPresent
        val oldKeyPresent = keyRepository.getDeprecatedPrivateKey().isNotEmpty()

        when {
            userDoesNotHaveSentinelData -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.LOGIN)
                )
                return
            }
            oldKeyPresent -> {
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
            doesUserNeedToUploadKeyToBackend -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_REGENERATION),
                )
                return
            }
            doesUserNeedToRecoverKey -> {
                state = state.copy(
                    userDestinationResult = Resource.Success(UserDestination.KEY_MANAGEMENT_RECOVERY),
                )
                return
            }
        }
        //endregion

        //region PART 2: Is our local key valid?
        val walletSigners = getWalletSigners() ?: return

        val doesUserHaveValidLocalKey =
            keyRepository.doesUserHaveValidLocalKey(verifyUser, walletSigners)

        //DESTINATION: User has valid key saved and we let them into the app
        state = if (doesUserHaveValidLocalKey) {
            state.copy(
                userDestinationResult = Resource.Success(UserDestination.HOME)
            )
            //6. DESTINATION: User has invalid local key saved and we send them to support screen
        } else {
            state.copy(
                userDestinationResult = Resource.Success(UserDestination.INVALID_KEY)
            )
        }
        //endregion
    }

    private suspend fun getWalletSigners(): List<WalletSigner?>? {
        val walletSignerResource: Resource<List<WalletSigner?>> = userRepository.getWalletSigners()

        return if (walletSignerResource is Resource.Error || walletSignerResource.data == null) {
            handleWalletSignersError(walletSignerResource)
            null
        } else {
            state = state.copy(walletSignersResult = Resource.Success(walletSignerResource.data))
            walletSignerResource.data
        }
    }

    fun resetUserDestinationResult() {
        state = state.copy(userDestinationResult = Resource.Uninitialized)
    }

    private fun resetVerifyUserResult() {
        state = state.copy(verifyUserResult = Resource.Uninitialized)
    }

    private fun resetWalletSignersCall() {
        state = state.copy(walletSignersResult = Resource.Uninitialized)
    }
}