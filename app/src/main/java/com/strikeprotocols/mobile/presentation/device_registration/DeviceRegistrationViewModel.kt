package com.strikeprotocols.mobile.presentation.device_registration

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.CryptographyManager
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.UserImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject


@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
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


    private fun triggerImageCapture() {
        state = state.copy(triggerImageCapture = Resource.Success(Unit))
    }

    private fun triggerBioPrompt() {
        //todo see what we need to do to create key then sign data with it
    }

    fun handleCapturedUserPhoto(userPhoto: Bitmap) {
        state = state.copy(capturedUserPhoto = userPhoto)
        triggerBioPrompt()
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

    fun resetImageCaptureFailedError() {
        state = state.copy(imageCaptureFailedError = Resource.Uninitialized)
    }
}