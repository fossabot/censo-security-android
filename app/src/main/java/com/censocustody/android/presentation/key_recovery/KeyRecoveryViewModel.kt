package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.GetRecoveryShardsResponse
import com.censocustody.android.data.repository.KeyRepository
import com.raygun.raygun4android.RaygunClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
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
            val recoveryData = state.recoverShardsData
            val verifyUserDetails = state.verifyUserDetails

            if (recoveryData == null || verifyUserDetails == null) {
                state =
                    state.copy(
                        recoverKeyProcess = Resource.Error(data = RecoveryError.MISSING_DATA)
                    )
                return@launch
            }

            val rootSeedFromShards = recoverRootSeedFromShards(recoveryData) ?: return@launch

            val rootSeedValid = keyRepository.validateRecoveredRootSeed(
                rootSeed = rootSeedFromShards,
                verifyUser = verifyUserDetails
            )

            state = if (rootSeedValid) {
                val savedSeed = saveRootSeed(rootSeedFromShards)

                if (savedSeed) {
                    state.copy(recoverKeyProcess = Resource.Success(null))
                } else {
                    state.copy(
                        recoverKeyProcess = Resource.Error(data = RecoveryError.INVALID_ROOT_SEED)
                    )
                }

            } else {
                state.copy(
                    recoverKeyProcess = Resource.Error(data = RecoveryError.INVALID_ROOT_SEED)
                )
            }
        }
    }

    private suspend fun recoverRootSeedFromShards(recoveryData: GetRecoveryShardsResponse): ByteArray? {
        return try {
            keyRepository.recoverRootSeed(
                shards = recoveryData.shards,
                ancestors = recoveryData.ancestors
            )
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.RECOVER_KEY,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            state =
                state.copy(
                    recoverKeyProcess = Resource.Error(
                        data = RecoveryError.FAILED_DECRYPT,
                        exception = e
                    )
                )
            null
        }
    }

    private suspend fun saveRootSeed(rootSeed: ByteArray): Boolean {
        return try {
            keyRepository.saveV3RootKey(
                rootSeed = rootSeed,
                mnemonic = null
            )

            keyRepository.saveV3PublicKeys(
                rootSeed = rootSeed
            )
            true
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.RECOVER_KEY,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            state = state.copy(
                recoverKeyProcess = Resource.Error(
                    data = RecoveryError.SAVE_FAILED,
                    exception = e
                )
            )
            false
        }
    }

    fun biometryFailed() {
        state = state.copy(recoverKeyProcess = Resource.Error(data = RecoveryError.BIOMETRY_FAILED))
    }

    fun retry() {
        viewModelScope.launch {
            retrieveRecoveryShards()
        }
    }

    private suspend fun retrieveRecoveryShards() {
        val recoveryShardsResource = keyRepository.retrieveRecoveryShards()

        if (recoveryShardsResource is Resource.Success) {
            state = state.copy(
                recoverShardsData = recoveryShardsResource.data
            )
            triggerBioPrompt()
        } else if (recoveryShardsResource is Resource.Error) {
            RaygunClient.send(
                recoveryShardsResource.exception
                    ?: Exception("Failed retrieving recovery shards from API"), listOf(
                    CrashReportingUtil.RECOVER_KEY,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            state =
                state.copy(
                    recoverKeyProcess = Resource.Error(
                        data = RecoveryError.FAILED_RETRIEVE_SHARDS
                    )
                )
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