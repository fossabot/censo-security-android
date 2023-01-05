package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.SharedPrefsHelper
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.DeviceType
import com.censocustody.android.data.models.UserDevice
import com.censocustody.android.data.models.UserImage
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
        censoLog(message = "Biometry approved for device key registration")
        sendUserDeviceAndImageToBackend(cryptoObject.signature)
    }

    fun biometryFailed() {
        censoLog(message = "Biometry failed for device key registration")
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

                censoLog(message = "Verified: $verified")
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
                    //todo: show error to user
                    censoLog(message = "Error when creating device key: ${userDeviceAdded.exception}")
                    state = state.copy(addUserDevice = userDeviceAdded)
                }


            } else {
                //todo: broken flow data got wiped
                censoLog(message = "Error when creating device key: null data")
                state = state.copy(addUserDevice = Resource.Error(exception = Exception("Missing essential data for device registration")))
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
                censoLog(message = "Was able to create key: ${BaseWrapper.encode(devicePublicKey)}")

                //need to go get cipher authenticated
                val signature = cipherRepository.getSignatureForDeviceSigning(keyId)
                if (signature != null) {
                    triggerBioPrompt(signature)
                }

            } catch (e: Exception) {
                censoLog(message = "Failed to sign data or create key: $e")
            }
        }
    }

    fun handleCapturedUserPhoto(userPhoto: Bitmap) {
        state = state.copy(capturedUserPhoto = userPhoto)
        showUserDialogToSaveDeviceKey()
    }

    fun showUserDialogToSaveDeviceKey() {
        state = state.copy(userApproveSaveDeviceKey = Resource.Success(Unit))
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

    fun resetUserDevice() {
        state = state.copy(addUserDevice = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }

    fun resetImageCaptureFailedError() {
        state = state.copy(imageCaptureFailedError = Resource.Uninitialized)
    }

    fun resetUserDialogToSaveDeviceKey() {
        state = state.copy(userApproveSaveDeviceKey = Resource.Uninitialized)
    }
}