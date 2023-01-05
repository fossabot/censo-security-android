package com.censocustody.android.presentation.regeneration

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.common.censoLog
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.MigrationRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.mapToPublicKeysList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.Signature
import javax.inject.Inject

@HiltViewModel
class RegenerationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val cipherRepository: CipherRepository
) : ViewModel() {

    var state by mutableStateOf(RegenerationState())
        private set

    //region VM SETUP
    fun onStart() {
        retrieveDeviceKey()
    }

    private fun retrieveDeviceKey() {
        viewModelScope.launch {
            val userEmail = userRepository.retrieveUserEmail()
            val deviceKeyId = userRepository.retrieveUserDeviceId(userEmail)
            val signature = cipherRepository.getSignatureForDeviceSigning(deviceKeyId)
            if (signature != null) {
                state =
                    state.copy(
                        triggerBioPrompt =
                            Resource.Success(BiometricPrompt.CryptoObject(signature)),
                    )
            }
        }
    }

    fun biometryApproved(cryptoObject: BiometricPrompt.CryptoObject) {
        viewModelScope.launch {
            censoLog(message = "Signature passed back from biometry: ${cryptoObject.signature}")
            handleKeyReUploadKeys(cryptoObject.signature!!)
        }
    }

    fun biometryFailed(failedReason: Int? = null) {
        if (failedReason != BioCryptoUtil.FAIL_ERROR) {
            state = state.copy(showToast = Resource.Success(""))
            retrieveDeviceKey()
        }
    }

    private suspend fun handleKeyReUploadKeys(signature: Signature) {
        val publicKeys = keyRepository.retrieveV3PublicKeys()

        val walletSignerResource =
            userRepository.addWalletSigner(publicKeys, signature)

        if (walletSignerResource is Resource.Success) {
            state = state.copy(finishedRegeneration = true)
        } else if (walletSignerResource is Resource.Error) {
            state =
                state.copy(regenerationError = Resource.Success(walletSignerResource.censoError))
        }
    }

    fun retryRegeneration() {
        viewModelScope.launch {
            retrieveDeviceKey()
        }
    }

    fun resetAddWalletSignerCall() {
        state = state.copy(
            addWalletSigner = Resource.Uninitialized,
            finishedRegeneration = false
        )
    }

    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}