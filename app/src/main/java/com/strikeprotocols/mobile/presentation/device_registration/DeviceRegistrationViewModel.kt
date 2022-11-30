package com.strikeprotocols.mobile.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
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
import com.strikeprotocols.mobile.data.models.DeviceType
import com.strikeprotocols.mobile.data.models.UserDevice
import com.strikeprotocols.mobile.data.models.UserImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.Signature
import java.util.UUID
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.math.sign


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

    fun biometryApproved(cryptoObject: BiometricPrompt.CryptoObject) {
        sendUserDeviceAndImageToBackend(cryptoObject.signature)
    }

    fun biometryFailed() {

    }

    fun sendUserDeviceAndImageToBackend(signature: Signature?) {
        viewModelScope.launch {
            val capturedUserPhoto = state.capturedUserPhoto
            val keyName = state.keyName

            if (signature != null && capturedUserPhoto != null && keyName.isNotEmpty()) {
                val userImage = generateUserImageObject(
                    userPhoto = capturedUserPhoto,
                    signature = signature,
                    keyName = keyName,
                    cryptographyManager = cryptographyManager
                )

                val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)


                val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)
                val verified = cryptographyManager.verifySignature(
                    keyName = keyName,
                    dataSigned = imageByteArray,
                    signatureToCheck = signatureToCheck
                )

                strikeLog(message = "Verified: $verified")


                userRepository.addUserDevice(
                    UserDevice(
                        publicKey = state.publicKey,
                        deviceType = DeviceType.ANDROID,
                        userImage = userImage
                    )
                )


            } else {
                //todo: broken flow data got wiped
            }
        }
    }

    private fun triggerBioPrompt(signature: Signature) {
        state =
            state.copy(triggerBioPrompt = Resource.Success(signature))
    }


    private fun triggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Success(Unit))
    }

    private fun createKeyForDevice() {
        viewModelScope.launch {
            val email = userRepository.retrieveUserEmail()
            val keyId = UUID.randomUUID().toString()
            SharedPrefsHelper.saveDeviceId(email, deviceId = keyId)
            state = state.copy(keyName = keyId)
            try {
                val devicePublicKey =
                    cryptographyManager.createPublicDeviceKey(keyName = keyId)

                state = state.copy(publicKey = BaseWrapper.encodeToBase64(devicePublicKey))
                strikeLog(message = "Was able to create key: ${BaseWrapper.encodeToBase64(devicePublicKey)}")

                //need to go get cipher authenticated
                val signature = cipherRepository.getSignatureForDeviceSigning(keyId)
                if (signature != null) {
                    triggerBioPrompt(signature)
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