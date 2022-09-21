package com.strikeprotocols.mobile.presentation.semantic_version_check

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.BioCryptoUtil
import com.strikeprotocols.mobile.common.BioPromptFailedReason
import com.strikeprotocols.mobile.common.CrashReportingUtil.FORCE_UPGRADE_TAG
import com.strikeprotocols.mobile.common.CrashReportingUtil.MANUALLY_REPORTED_TAG
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.SemanticVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
data class SemVerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository
) : ViewModel() {

    var state by mutableStateOf(SemVerState())
        private set

    fun checkMinimumVersion() {
        try {
            viewModelScope.launch {
                val semanticVersion = userRepository.checkMinimumVersion()
                if (semanticVersion is Resource.Success) {
                    enforceMinimumVersion(
                        minimumSemanticVersion = semanticVersion.data?.androidVersion?.minimumVersion
                            ?: BACKUP_VERSION
                    )
                }
            }
        } catch (e: Exception) {
            RaygunClient.send(e, listOf(FORCE_UPGRADE_TAG, MANUALLY_REPORTED_TAG))
        }
    }

    fun onForeground() {
        viewModelScope.launch {
            if (userRepository.userLoggedIn() && keyRepository.haveSentinelData()) {
                launchBlockingForegroundBiometry()
            }
        }
    }

    private suspend fun launchBlockingForegroundBiometry() {
        val cipher = keyRepository.getCipherForBackgroundDecryption()
        if (cipher != null) {
            state = state.copy(
                bioPromptTrigger = Resource.Success(cipher),
                biometryUnavailable = false
            )
        }
    }

    fun biometryApproved(cipher: Cipher) {
        viewModelScope.launch {
            val sentinelData = keyRepository.retrieveSentinelData(cipher)
            state = if (sentinelData == EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA) {
                state.copy(bioPromptTrigger = Resource.Uninitialized)
            } else {
                //todo: this most likely means we have broken key info and need user to recreate the key
                state.copy(bioPromptTrigger = Resource.Error())
            }
        }
    }

    fun biometryFailed(errorCode: Int) {
        if (BioCryptoUtil.getBioPromptFailedReason(errorCode) == BioPromptFailedReason.FAILED_TOO_MANY_ATTEMPTS) {
            state = state.copy(biometryUnavailable = true)
        } else {
            viewModelScope.launch {
                launchBlockingForegroundBiometry()
            }
        }
    }

    fun setPromptTriggerToLoading() {
        state = state.copy(bioPromptTrigger = Resource.Loading())
    }

    private fun enforceMinimumVersion(minimumSemanticVersion: String) {
        val appVersion = SemanticVersion.parse(BuildConfig.VERSION_NAME)

        val minimumVersion = SemanticVersion.parse(minimumSemanticVersion)

        val forceUpdate = appVersion.compareTo(minimumVersion)

        if (forceUpdate < 0) {
            state = state.copy(shouldEnforceAppUpdate = Resource.Success(true))
        }
    }

    fun resetShouldEnforceAppUpdate() {
        state = state.copy(shouldEnforceAppUpdate = Resource.Uninitialized)
    }

    companion object {
        const val BACKUP_VERSION = "0.0.0"
    }
}