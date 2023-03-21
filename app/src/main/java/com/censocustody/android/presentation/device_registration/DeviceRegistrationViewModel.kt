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
        state = state.copy(verifyUserDetails = initialData.verifyUserDetails?.copy(shardingPolicy = null))

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
        if (isBootstrapUser()) {
            sendBootstrapUserToKeyCreation()
        } else {
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
                    userRepository.saveDeviceId(email = email, deviceId = keyName)

                    val userDeviceAdded = userRepository.addUserDevice(
                        UserDevice(
                            publicKey = state.standardPublicKey,
                            deviceType = DeviceType.ANDROID,
                            userImage = userImage
                        )
                    )

                    if (userDeviceAdded is Resource.Success) {
                        userRepository.saveDeviceId(email = email, deviceId = keyName)
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
        viewModelScope.launch {
            try {
                val capturedUserPhoto = state.capturedUserPhoto
                val keyName = state.standardKeyName

                if (capturedUserPhoto != null && keyName.isNotEmpty()) {
                    //Get user image all ready
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

                    //Save device id and device key
                    val email = userRepository.retrieveUserEmail()

                    userRepository.saveDeviceId(email = email, deviceId = keyName)
                    userRepository.saveDevicePublicKey(email = email, publicKey = state.standardPublicKey)
                    userRepository.saveBootstrapDeviceId(email = email, deviceId = state.bootstrapKeyName)
                    userRepository.saveBootstrapDevicePublicKey(email = email, publicKey = state.bootstrapPublicKey)

                    //Send user to the key creation with the image data passed along...
                    state = state.copy(
                        userImage = userImage,
                        createdBootstrapDeviceData = Resource.Success(Unit),
                    )
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
        if (state.verifyUserDetails == null) {
            //todo: we need to pass this here...bail earlier in the flow
        }

        if (isBootstrapUser()) {
            //Standard Device Registration
            createBootstrapKeysForDevice()
        } else {
            //Standard Device Registration
            createStandardKeyForDevice()
        }
    }

    private fun createBootstrapKeysForDevice() {
        viewModelScope.launch {
            val standardKeyId = cryptographyManager.createDeviceKeyId()
            val bootstrapKeyId = cryptographyManager.createDeviceKeyId()

            state = state.copy(
                standardKeyName = standardKeyId,
                bootstrapKeyName = bootstrapKeyId
            )

            try {
                cryptographyManager.getOrCreateKey(keyName = standardKeyId)
                cryptographyManager.getOrCreateKey(keyName = bootstrapKeyId)

                val standardPublicKey =
                    cryptographyManager.getPublicKeyFromDeviceKey(keyName = standardKeyId)
                val bootstrapPublicKey =
                    cryptographyManager.getPublicKeyFromDeviceKey(keyName = bootstrapKeyId)

                val compressedStandardPublicKey =
                    ECIESManager.extractUncompressedPublicKey(standardPublicKey.encoded)

                val compressedBootstrapPublicKey =
                    ECIESManager.extractUncompressedPublicKey(bootstrapPublicKey.encoded)

                state = state.copy(
                    standardPublicKey = BaseWrapper.encode(compressedStandardPublicKey),
                    bootstrapPublicKey = BaseWrapper.encode(compressedBootstrapPublicKey)
                )

                triggerBioPrompt()

            } catch (e: Exception) {
                state = state.copy(
                    deviceRegistrationError = DeviceRegistrationError.SIGNING_IMAGE,
                    capturingDeviceKey = Resource.Uninitialized
                )
            }
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

    fun capturedUserPhotoSuccess(userPhoto: Bitmap) {
        state = state.copy(
            capturedUserPhoto = userPhoto,
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

    private fun isBootstrapUser() = state.verifyUserDetails?.shardingPolicy == null

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

    fun resetUserLoggedIn() {
        state = state.copy(userLoggedIn = true)
    }
}