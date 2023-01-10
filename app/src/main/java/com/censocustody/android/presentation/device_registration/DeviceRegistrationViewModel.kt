package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.SharedPrefsHelper
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.DeviceType
import com.censocustody.android.data.models.UserDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.Signature
import java.util.UUID
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
                state = state.copy(userLoggedIn = false)
            } else {
                triggerImageCapture()
            }
        }
    }

    fun retry() {
        state = DeviceRegistrationState()
        triggerImageCapture()
    }

    fun biometryApproved(cryptoObject: CryptoObject) {
        sendUserDeviceAndImageToBackend(cryptoObject.signature)
    }

    fun biometryFailed() {
        state = state.copy(deviceRegistrationError = DeviceRegistrationError.BIOMETRY)
    }

    private fun sendUserDeviceAndImageToBackend(signature: Signature?) {
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

                val email = userRepository.retrieveUserEmail()
                SharedPrefsHelper.saveDeviceId(email = email, deviceId = keyName)

                val userDeviceAdded = userRepository.addUserDevice(
                    UserDevice(
                        publicKey = state.publicKey,
                        deviceType = DeviceType.ANDROID,
                        userImage = userImage
                    )
                )

                if (userDeviceAdded is Resource.Success) {
                    SharedPrefsHelper.saveDeviceId(email, keyName)
                    SharedPrefsHelper.saveDevicePublicKey(email, state.publicKey)

                    state = state.copy(addUserDevice = userDeviceAdded)

                } else if (userDeviceAdded is Resource.Error) {
                    state = state.copy(
                        addUserDevice = userDeviceAdded,
                        deviceRegistrationError = DeviceRegistrationError.API
                    )
                }


            } else {
                state = state.copy(
                    addUserDevice = Resource.Error(exception = Exception("Missing essential data for device registration")),
                    deviceRegistrationError = DeviceRegistrationError.API
                )
            }
        }
    }

    private fun triggerBioPrompt(signature: Signature) {
        state =
            state.copy(triggerBioPrompt = Resource.Success(signature))
    }


    fun triggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Success(Unit))
    }

    fun createKeyForDevice() {
        viewModelScope.launch {
            val keyId = UUID.randomUUID().toString().replace("-", "")
            state = state.copy(keyName = keyId)
            try {
                val devicePublicKey =
                    cryptographyManager.createPublicDeviceKey(keyName = keyId)

                state = state.copy(publicKey = BaseWrapper.encode(devicePublicKey))

                val signature = cipherRepository.getSignatureForDeviceSigning(keyId)
                if (signature != null) {
                    triggerBioPrompt(signature)
                }

            } catch (e: Exception) {
                state = state.copy(deviceRegistrationError = DeviceRegistrationError.SIGNING_IMAGE)
            }
        }
    }

    fun capturedUserPhotoSuccess(userPhoto: Bitmap) {
        state = state.copy(capturedUserPhoto = userPhoto)
        showUserDialogToSaveDeviceKey()
    }

    fun capturedUserPhotoError(imageCaptureError: ImageCaptureError?) {
        state = state.copy(
            deviceRegistrationError = DeviceRegistrationError.IMAGE_CAPTURE,
            imageCaptureFailedError = Resource.Success(imageCaptureError)
        )
    }

    private fun showUserDialogToSaveDeviceKey() {
        state = state.copy(userApproveSaveDeviceKey = Resource.Success(Unit))
    }

    fun resetTriggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Uninitialized)
    }

    fun resetCapturedUserPhoto() {
        state = state.copy(capturedUserPhoto = null)
    }

    fun resetUserDevice() {
        state = state.copy(addUserDevice = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }

    fun resetErrorState() {
        state = state.copy(
            deviceRegistrationError = DeviceRegistrationError.NONE,
            imageCaptureFailedError = Resource.Uninitialized
        )
    }

    fun resetUserDialogToSaveDeviceKey() {
        state = state.copy(userApproveSaveDeviceKey = Resource.Uninitialized)
    }

    fun resetUserLoggedIn() {
        state = state.copy(userLoggedIn = true)
    }
}