package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.ECIESManager
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.DeviceType
import com.censocustody.android.data.models.UserDevice
import com.raygun.raygun4android.RaygunClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cryptographyManager: CryptographyManager
) : ViewModel() {

    var state by mutableStateOf(DeviceRegistrationState())
        private set

    fun onStart(initialData: DeviceRegistrationInitialData) {
        state = state.copy(verifyUserDetails = initialData.verifyUserDetails)

        if (initialData.verifyUserDetails == null) {
            state = state.copy(kickUserToEntrance = true)
            RaygunClient.send(
                Exception("Missing verify user when entering device registration"), listOf(
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            return
        }

        viewModelScope.launch {
            val isUserLoggedIn = userRepository.userLoggedIn()

            if (!isUserLoggedIn) {
                state = state.copy(userLoggedIn = false)
            }
        }
    }

    fun retry() {
        state = DeviceRegistrationState()
        triggerImageCapture()
    }

    fun biometryApproved() {
        if (!isBootstrapUser()) {
            sendUserDeviceAndImageToBackend()
        }
    }

    fun biometryFailed() {
        state = state.copy(
            deviceRegistrationError = DeviceRegistrationError.BIOMETRY,
            capturingDeviceKey = Resource.Uninitialized
        )
    }

    private fun sendUserDeviceAndImageToBackend() {
        viewModelScope.launch {
            try {
                val capturedUserPhoto = state.capturedUserPhoto
                val keyName = state.standardKeyName

                if (capturedUserPhoto != null && keyName.isNotEmpty()) {
                    val userImage = generateUserImageObject(
                        userPhoto = capturedUserPhoto,
                        keyName = keyName,
                        cryptographyManager = cryptographyManager
                    )

                    val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)
                    val hashOfImage = hashOfUserImage(imageByteArray)

                    val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)

                    val verified = cryptographyManager.verifySignature(
                        keyName = keyName,
                        dataSigned = hashOfImage,
                        signatureToCheck = signatureToCheck
                    )

                    if (!verified) {
                        throw Exception("Device image signature not valid.")
                    }

                    val email = userRepository.retrieveUserEmail()

                    val userDeviceAdded = userRepository.addUserDevice(
                        UserDevice(
                            publicKey = state.standardPublicKey,
                            deviceType = DeviceType.ANDROID,
                            userImage = userImage
                        )
                    )

                    if (userDeviceAdded is Resource.Success) {
                        userRepository.saveDevicePublicKey(email = email, publicKey = state.standardPublicKey)

                        state = state.copy(addUserDevice = userDeviceAdded)

                    } else if (userDeviceAdded is Resource.Error) {
                        state = state.copy(
                            addUserDevice = userDeviceAdded,
                            deviceRegistrationError = DeviceRegistrationError.API,
                            capturingDeviceKey = Resource.Uninitialized
                        )
                    }


                } else {
                    state = state.copy(
                        addUserDevice = Resource.Error(exception = Exception("Missing essential data for device registration")),
                        deviceRegistrationError = DeviceRegistrationError.API,
                        capturingDeviceKey = Resource.Uninitialized
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    addUserDevice = Resource.Error(exception = e),
                    deviceRegistrationError = DeviceRegistrationError.SIGNING_IMAGE,
                    capturingDeviceKey = Resource.Uninitialized
                )
            }
        }
    }

    private fun sendBootstrapUserToKeyCreation() {
        state = if (state.capturedUserPhoto != null && state.fileUrl.isNotEmpty()) {
            //Send user to the key creation with the image data passed along...
            state.copy(
                createdBootstrapDeviceData = Resource.Success(Unit),
            )
        } else {
            state.copy(
                addUserDevice = Resource.Error(exception = Exception("Missing essential data for bootstrap device registration")),
                deviceRegistrationError = DeviceRegistrationError.API,
                capturingDeviceKey = Resource.Uninitialized
            )
        }
    }

    private fun triggerBioPrompt() {
        state =
            state.copy(triggerBioPrompt = Resource.Success(Unit))
    }


    fun triggerImageCapture() {
        state = state.copy(
            triggerImageCapture = Resource.Loading(),
            capturingDeviceKey = Resource.Loading()
        )
    }

    fun imageCaptured() {
        if (isBootstrapUser()) {
            sendBootstrapUserToKeyCreation()
        } else {
            createStandardKeyForDevice()
        }
    }

    private fun createStandardKeyForDevice() {
        viewModelScope.launch {
            val keyId = cryptographyManager.createDeviceKeyId()
            state = state.copy(standardKeyName = keyId)
            try {

                cryptographyManager.getOrCreateKey(keyName = keyId)

                val publicKey = cryptographyManager.getPublicKeyFromDeviceKey(keyName = keyId)
                val compressedPublicKey =
                    ECIESManager.extractUncompressedPublicKey(publicKey.encoded)

                state = state.copy(standardPublicKey = BaseWrapper.encode(compressedPublicKey))

                triggerBioPrompt()

            } catch (e: Exception) {
                state = state.copy(
                    deviceRegistrationError = DeviceRegistrationError.SIGNING_IMAGE,
                    capturingDeviceKey = Resource.Uninitialized
                )
            }
        }
    }

    fun capturedUserPhotoSuccess(userPhoto: Bitmap, fileUrl: String) {
        state = state.copy(
            capturedUserPhoto = userPhoto,
            fileUrl = fileUrl,
            triggerImageCapture = Resource.Success(Unit)
        )
    }

    fun capturedUserPhotoError(exception: Exception) {
        RaygunClient.send(
            exception, listOf(
                CrashReportingUtil.IMAGE,
                CrashReportingUtil.MANUALLY_REPORTED_TAG
            )
        )
        state = state.copy(
            deviceRegistrationError = DeviceRegistrationError.IMAGE_CAPTURE,
            triggerImageCapture = Resource.Uninitialized,
            capturingDeviceKey = Resource.Uninitialized
        )
    }

    private fun isBootstrapUser() = state.verifyUserDetails != null && state.verifyUserDetails?.shardingPolicy == null

    fun resetTriggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Uninitialized)
    }

    fun resetCapturedUserPhoto() {
        state = state.copy(capturedUserPhoto = null)
    }

    fun resetCreatedBootstrapTrigger() {
        state = state.copy(createdBootstrapDeviceData = Resource.Uninitialized)
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
        )
    }

    fun resetKickUserOut() {
        state = state.copy(
            kickUserToEntrance = false
        )
    }

    fun resetUserLoggedIn() {
        state = state.copy(userLoggedIn = true)
    }
}