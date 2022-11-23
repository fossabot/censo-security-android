package com.strikeprotocols.mobile.presentation.device_registration

import android.graphics.Bitmap
import androidx.biometric.BiometricPrompt
import com.strikeprotocols.mobile.common.ImageCaptureError
import com.strikeprotocols.mobile.common.Resource
import java.security.Signature

data class DeviceRegistrationState(
    val addUserDevice: Resource<Boolean> = Resource.Uninitialized,
    //User Photo
    val triggerImageCapture: Resource<Unit> = Resource.Uninitialized,
    val capturedUserPhoto: Bitmap? = null,
    val keyName: String = "",
    val publicKey: String = "",
    val imageCaptureFailedError: Resource<ImageCaptureError> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Signature> = Resource.Uninitialized,
)