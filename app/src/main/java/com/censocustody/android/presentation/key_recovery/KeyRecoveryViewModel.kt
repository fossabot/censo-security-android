package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
) : ViewModel() {

    var state by mutableStateOf(KeyRecoveryState())
        private set

    //region VM SETUP
    fun onStart(initialData: KeyRecoveryInitialData) {
        if (state.verifyUserDetails == null) {
            state = state.copy(verifyUserDetails = initialData.verifyUserDetails)

            viewModelScope.launch {
                retrieveRecoveryShards()
            }
        }
    }

    fun cleanUp() {
        state = KeyRecoveryState()
    }

    fun biometryApproved() {
        decryptShardsAndSaveRootSeed()
    }

    private fun decryptShardsAndSaveRootSeed() {
        viewModelScope.launch {
            val recoveryData = state.recoverShardsResource.data
            val verifyUserDetails = state.verifyUserDetails

            if (recoveryData == null || verifyUserDetails == null) {
                state =
                    state.copy(
                        recoverKeyProcess = Resource.Error(data = RecoveryError.MISSING_DATA)
                    )
                return@launch
            }

            val rootSeedFromShards = try {
                keyRepository.recoverRootSeed(
                    shards = recoveryData.shards,
                    ancestors = recoveryData.ancestors
                )
            } catch (e: Exception) {
                state =
                    state.copy(
                        recoverKeyProcess = Resource.Error(data = RecoveryError.FAILED_DECRYPT)
                    )
                return@launch
            }

            val rootSeedValid = keyRepository.validateRecoveredRootSeed(
                rootSeed = rootSeedFromShards,
                verifyUser = verifyUserDetails
            )

            if (rootSeedValid) {
                state = state.copy(recoverKeyProcess = Resource.Success(null))
            } else {
                state =
                    state.copy(
                        recoverKeyProcess = Resource.Error(data = RecoveryError.INVALID_ROOT_SEED)
                    )
            }
        }
    }

    fun biometryFailed() {
        state = state.copy(recoverKeyProcess = Resource.Error(data = RecoveryError.BIOMETRY_FAILED))
    }

    private suspend fun retrieveRecoveryShards() {
        val recoveryShardsResource = keyRepository.retrieveRecoveryShards()

        state = state.copy(recoverShardsResource = recoveryShardsResource)

        if (recoveryShardsResource is Resource.Success) {
            triggerBioPrompt()
        } else if (recoveryShardsResource is Resource.Error) {
            state =
                state.copy(recoverKeyProcess = Resource.Error(data = RecoveryError.FAILED_RETRIEVE_SHARDS))
        }
    }

    fun triggerBioPrompt() {
        state = state.copy(triggerBioPrompt = Resource.Success(Unit))
    }

    fun resetKeyProcess() {
        state = state.copy(recoverKeyProcess = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}