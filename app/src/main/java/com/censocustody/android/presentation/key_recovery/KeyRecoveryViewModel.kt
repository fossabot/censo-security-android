package com.censocustody.android.presentation.key_recovery

import com.censocustody.android.presentation.key_creation.KeyCreationState
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
        //todo: call encryption manager to decrypt shards and reconstruct root seed
    }

    fun biometryFailed() {
        state = state.copy(recoverKeyProcess = Resource.Error())
    }

    suspend fun retrieveRecoveryShards() {
        val recoveryShardsResource = keyRepository.retrieveRecoveryShards()

        state = state.copy(recoverShardsResource = recoveryShardsResource)

        if (recoveryShardsResource is Resource.Success) {
            triggerBioPrompt()
        }
    }

    fun triggerBioPrompt() {
        state = state.copy(triggerBioPrompt = Resource.Success(Unit))
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}