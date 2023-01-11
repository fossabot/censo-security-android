package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt
import com.censocustody.android.common.ImageCaptureError
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.UserDevice
import java.security.Signature

data class DeviceRegistrationState(
    val addUserDevice: Resource<UserDevice> = Resource.Uninitialized,
    //User Photo
    val capturingDeviceKey: Resource<Boolean> = Resource.Uninitialized,
    val triggerImageCapture: Resource<Unit> = Resource.Uninitialized,
    val capturedUserPhoto: Bitmap? = null,
    val keyName: String = "",
    val publicKey: String = "",
    val triggerBioPrompt: Resource<Signature> = Resource.Uninitialized,
    val deviceRegistrationError: DeviceRegistrationError = DeviceRegistrationError.NONE,
    val userLoggedIn: Boolean = true
)


enum class DeviceRegistrationError {
    NONE, API, IMAGE_CAPTURE, SIGNING_IMAGE, BIOMETRY
}