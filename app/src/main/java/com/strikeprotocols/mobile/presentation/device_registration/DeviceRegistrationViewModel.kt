package com.strikeprotocols.mobile.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.CryptographyManager
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.SharedPrefsHelper
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.CipherRepository
import com.strikeprotocols.mobile.data.models.UserImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.crypto.Cipher
import javax.inject.Inject


@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cipherRepository: CipherRepository,
    private val cryptographyManager: CryptographyManager
) : ViewModel() {

    var state by mutableStateOf(DeviceRegistrationState())
        private set

    companion object {
        const val THUMBNAIL_DATA_KEY = "data"
    }

    fun onStart() {
        viewModelScope.launch {
            val isUserLoggedIn = userRepository.userLoggedIn()

            if (!isUserLoggedIn) {
                //todo: kick user to login
            } else {
                triggerImageCapture()
            }
        }
    }

    suspend fun createUserImage(
        capturedUserPhoto: Bitmap,
        cipher: Cipher,
        keyRepository: KeyRepository
    ): UserImage {
        return generateUserImageObject(
            userPhoto = capturedUserPhoto,
            cipher = cipher,
            keyRepository = keyRepository
        )
    }

    fun biometryApproved(cryptoObject: androidx.biometric.BiometricPrompt.CryptoObject) {
        strikeLog(message = "Biometry approved: ${cryptoObject}")
    }

    fun biometryFailed() {

    }

    private fun triggerBioPrompt(cipher: Cipher) {
        state =
            state.copy(triggerBioPrompt = Resource.Success(BiometricPrompt.CryptoObject(cipher)))
    }


    private fun triggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Success(Unit))
    }

    private fun createKeyForDevice() {
        viewModelScope.launch {
            val email = userRepository.retrieveUserEmail()
            val keyId = UUID.randomUUID().toString()
            SharedPrefsHelper.saveDeviceId(email, deviceId = keyId)
            try {
                val devicePublicKey =
                    cryptographyManager.createPublicDeviceKey(keyName = keyId)
                strikeLog(message = "Was able to create key: $devicePublicKey")

                //need to go get cipher authenticated
                val cipher = cipherRepository.getCipherForDeviceSigning(keyId)
                if (cipher != null) {
                    triggerBioPrompt(cipher)
                }

            } catch (e: Exception) {
                strikeLog(message = "Failed to sign data or create key: $e")
            }
        }
    }

    fun handleCapturedUserPhoto(userPhoto: Bitmap) {
        state = state.copy(capturedUserPhoto = userPhoto)
        createKeyForDevice()
    }

    fun handleImageCaptureError(imageCaptureError: ImageCaptureError) {
        state = state.copy(imageCaptureFailedError = Resource.Error(data = imageCaptureError))
    }

    fun resetTriggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Uninitialized)
    }

    fun resetCapturedUserPhoto() {
        state = state.copy(capturedUserPhoto = null)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }

    fun resetImageCaptureFailedError() {
        state = state.copy(imageCaptureFailedError = Resource.Uninitialized)
    }
}