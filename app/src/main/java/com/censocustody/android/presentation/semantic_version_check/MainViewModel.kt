package com.censocustody.android.presentation.semantic_version_check

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.common.Resource
import com.censocustody.android.common.util.BiometricUtil
import com.censocustody.android.data.cryptography.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.presentation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
data class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    fun onForeground(biometricCapability: BiometricUtil.Companion.BiometricsStatus) {
        viewModelScope.launch {
            state = state.copy(biometryStatus = biometricCapability)

            val userLoggedIn = userRepository.userLoggedIn()
            val haveSentinelData = keyRepository.haveSentinelData()

            if (biometricCapability != BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED
                && !haveSentinelData) {
                return@launch
            }

            if (userLoggedIn && haveSentinelData) {
                launchBlockingForegroundBiometryRetrieval()
            }
        }
    }

    fun blockUIStatus(): BlockAppUI {
        val visibleBlockingUi = state.bioPromptTrigger !is Resource.Uninitialized
        val biometryDisabled =
            state.biometryStatus != null && state.biometryStatus != BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED
        val userOnForceUpdate = state.currentDestination == Screen.EnforceUpdateRoute.route

        return when {
            userOnForceUpdate -> BlockAppUI.NONE
            biometryDisabled -> BlockAppUI.BIOMETRY_DISABLED
            visibleBlockingUi -> BlockAppUI.FOREGROUND_BIOMETRY
            else -> BlockAppUI.NONE
        }
    }

    private suspend fun launchBlockingForegroundBiometryRetrieval() {
        state = state.copy(bioPromptTrigger = Resource.Loading())
        val cipher = keyRepository.getInitializedCipherForSentinelDecryption()

        if (cipher != null) {
            state = state.copy(
                bioPromptTrigger = Resource.Success(cipher),
                biometryTooManyAttempts = false,
                bioPromptReason = BioPromptReason.FOREGROUND_RETRIEVAL
            )
        }
    }

    fun biometryApproved(cipher: Cipher) {
        viewModelScope.launch {
            if (state.bioPromptReason == BioPromptReason.FOREGROUND_RETRIEVAL) {
                checkSentinelDataAfterBiometricApproval(cipher)
            }
        }
    }

    private suspend fun checkSentinelDataAfterBiometricApproval(cipher: Cipher) {
        state = try {
            val sentinelData = keyRepository.retrieveSentinelData(cipher)
            if (sentinelData == SENTINEL_STATIC_DATA) {
                biometrySuccessfulState()
            } else {
                keyRepository.handleKeyInvalidatedException(Exception("Incorrect sentinel data"))
                state.copy(bioPromptTrigger = Resource.Error())
            }
        } catch (e: Exception) {
            keyRepository.handleKeyInvalidatedException(e)
            state.copy(bioPromptTrigger = Resource.Error())
        }
    }

    private suspend fun saveSentinelDataAfterBiometricApproval(cipher: Cipher) {
        state = try {
            keyRepository.saveSentinelData(cipher)
            val updatedState = biometrySuccessfulState()
            updatedState.copy(
                sendUserToEntrance = true
            )
        } catch (e: Exception) {
            keyRepository.handleKeyInvalidatedException(e)
            state.copy(bioPromptTrigger = Resource.Error())
        }
    }

    fun biometryFailed(errorCode: Int) {
        state =
            if (BioCryptoUtil.getBioPromptFailedReason(errorCode) == BioPromptFailedReason.FAILED_TOO_MANY_ATTEMPTS) {
                state.copy(biometryTooManyAttempts = true, bioPromptTrigger = Resource.Error())
            } else {
                state.copy(bioPromptTrigger = Resource.Error())
            }
    }

    fun retryBiometricGate() {
        viewModelScope.launch {
            if (state.bioPromptReason == BioPromptReason.FOREGROUND_RETRIEVAL) {
                launchBlockingForegroundBiometryRetrieval()
            }
        }
    }


    fun setPromptTriggerToLoading() {
        state = state.copy(bioPromptTrigger = Resource.Loading())
    }

    private fun biometrySuccessfulState() : MainState =
        state.copy(
            bioPromptTrigger = Resource.Uninitialized,
            bioPromptReason = BioPromptReason.UNINITIALIZED
        )

    fun updateCurrentScreen(currentDestination: String?) {
        if (currentDestination != state.currentDestination) {
            state = state.copy(currentDestination = currentDestination)
        }
    }

    fun resetSendUserToEntrance() {
        state = state.copy(sendUserToEntrance = false)
    }

    fun resetBiometry() {
        state = biometrySuccessfulState()
    }

    companion object {
        const val BACKUP_VERSION = "0.0.0"
    }
}