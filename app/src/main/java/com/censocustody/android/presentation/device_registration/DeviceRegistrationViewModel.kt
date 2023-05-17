package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.common.ui.hashOfUserImage
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.cryptography.ECIESManager
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.data.repository.KeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository
) : ViewModel() {

    var state by mutableStateOf(DeviceRegistrationState())
        private set

    fun onStart(initialData: DeviceRegistrationInitialData) {
        if (initialData.verifyUser == null) {
            state = state.copy(
                kickUserToEntrance = true
            )
            Exception("Missing verify user data when entering device registration flow")
                .sendError(CrashReportingUtil.DEVICE_REGISTRATION)
            return
        }

        state = state.copy(
            isBootstrapUser = initialData.bootstrapUser,
            verifyUser = initialData.verifyUser
        )

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
        if (state.isBootstrapUser) {
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
                    val userImage = userRepository.createUserImage(
                        userPhoto = capturedUserPhoto,
                        keyName = keyName,
                    )

                    val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)
                    val hashOfImage = hashOfUserImage(imageByteArray)

                    val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)

                    val verified = keyRepository.verifySignature(
                        keyName = keyName,
                        signedData = hashOfImage,
                        signature = signatureToCheck
                    )

                    if (!verified) {
                        throw Exception("Device image signature not valid.")
                    }

                    val email = userRepository.retrieveUserEmail()
                    userRepository.saveDeviceId(email = email, deviceId = keyName)

                    val userDeviceAdded = userRepository.addUserDevice(
                        publicKey = state.standardPublicKey,
                        userImage = userImage
                    )

                    if (userDeviceAdded is Resource.Success) {
                        userRepository.clearLeftoverDeviceInfoIfPresent(email = email)
                        userRepository.clearPreviousDeviceId(email = email)
                        userRepository.saveDeviceId(email = email, deviceId = keyName)
                        userRepository.saveDevicePublicKey(
                            email = email,
                            publicKey = state.standardPublicKey
                        )

                        state = state.copy(addUserDevice = userDeviceAdded)

                    } else if (userDeviceAdded is Resource.Error) {
                        (userDeviceAdded.exception ?: Exception("Failed to add user device"))
                            .sendError(CrashReportingUtil.DEVICE_REGISTRATION)
                        state = state.copy(
                            addUserDevice = userDeviceAdded,
                            deviceRegistrationError = DeviceRegistrationError.API,
                            capturingDeviceKey = Resource.Uninitialized
                        )
                    }


                } else {
                    Exception("Missing essential data for device registration")
                        .sendError(CrashReportingUtil.DEVICE_REGISTRATION)
                    state = state.copy(
                        addUserDevice = Resource.Error(exception = Exception("Missing essential data for device registration")),
                        deviceRegistrationError = DeviceRegistrationError.API,
                        capturingDeviceKey = Resource.Uninitialized
                    )
                }
            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.DEVICE_REGISTRATION)
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

                if (capturedUserPhoto != null && state.fileUrl.isNotEmpty() && keyName.isNotEmpty()) {
                    //Save device id and device key
                    val email = userRepository.retrieveUserEmail()

                    //in case user was unable to upload previous keys, they will need to redo device image work
                    userRepository.clearLeftoverDeviceInfoIfPresent(email)

                    userRepository.saveDeviceId(email = email, deviceId = keyName)
                    userRepository.saveDevicePublicKey(
                        email = email,
                        publicKey = state.standardPublicKey
                    )
                    userRepository.saveBootstrapDeviceId(
                        email = email,
                        deviceId = state.bootstrapKeyName
                    )
                    userRepository.saveBootstrapDevicePublicKey(
                        email = email,
                        publicKey = state.bootstrapPublicKey
                    )
                    userRepository.saveBootstrapImageUrl(
                        email = email,
                        bootstrapImageUrl = state.fileUrl
                    )

                    //Send user to the key creation with the image data passed along...
                    state = state.copy(
                        addUserDevice = Resource.Success(Unit),
                    )
                } else {
                    Exception("Missing essential data for bootstrap device registration")
                        .sendError(CrashReportingUtil.DEVICE_REGISTRATION)
                    state = state.copy(
                        addUserDevice = Resource.Error(exception = Exception("Missing essential data for bootstrap device registration")),
                        deviceRegistrationError = DeviceRegistrationError.API,
                        capturingDeviceKey = Resource.Uninitialized
                    )
                }
            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.DEVICE_REGISTRATION)
                state = state.copy(
                    addUserDevice = Resource.Error(exception = e),
                    deviceRegistrationError = DeviceRegistrationError.BOOTSTRAP,
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
        if (state.isBootstrapUser) {
            //Standard Device Registration
            createBootstrapKeysForDevice()
        } else {
            //Standard Device Registration
            createStandardKeyForDevice()
        }
    }

    private fun createBootstrapKeysForDevice() {
        viewModelScope.launch {
            val standardKeyId = keyRepository.createDeviceKeyId()
            val bootstrapKeyId = keyRepository.createDeviceKeyId()

            state = state.copy(
                standardKeyName = standardKeyId,
                bootstrapKeyName = bootstrapKeyId
            )

            try {
                keyRepository.getOrCreateKey(keyName = standardKeyId)
                keyRepository.getOrCreateKey(keyName = bootstrapKeyId)

                val standardPublicKey =
                    keyRepository.getPublicKeyFromDeviceKey(keyName = standardKeyId)
                val bootstrapPublicKey =
                    keyRepository.getPublicKeyFromDeviceKey(keyName = bootstrapKeyId)

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
                e.sendError(CrashReportingUtil.DEVICE_REGISTRATION)
                state = state.copy(
                    deviceRegistrationError = DeviceRegistrationError.SIGNING_IMAGE,
                    capturingDeviceKey = Resource.Uninitialized
                )
            }
        }
    }

    private fun createStandardKeyForDevice() {
        viewModelScope.launch {
            val keyId = keyRepository.createDeviceKeyId()
            state = state.copy(standardKeyName = keyId)
            try {

                keyRepository.getOrCreateKey(keyName = keyId)

                val publicKey = keyRepository.getPublicKeyFromDeviceKey(keyName = keyId)
                val compressedPublicKey =
                    ECIESManager.extractUncompressedPublicKey(publicKey.encoded)

                state = state.copy(standardPublicKey = BaseWrapper.encode(compressedPublicKey))

                triggerBioPrompt()

            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.DEVICE_REGISTRATION)
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
        exception.sendError(CrashReportingUtil.IMAGE)
        state = state.copy(
            deviceRegistrationError = DeviceRegistrationError.IMAGE_CAPTURE,
            triggerImageCapture = Resource.Uninitialized,
            capturingDeviceKey = Resource.Uninitialized
        )
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