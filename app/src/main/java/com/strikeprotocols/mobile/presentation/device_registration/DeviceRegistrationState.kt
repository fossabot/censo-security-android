package com.strikeprotocols.mobile.presentation.device_registration

import android.graphics.Bitmap
import com.strikeprotocols.mobile.common.ImageCaptureError
import com.strikeprotocols.mobile.common.Resource
import javax.crypto.Cipher

data class DeviceRegistrationState(
    val addUserDevice: Resource<Boolean> = Resource.Uninitialized,
    //User Photo
    val triggerImageCapture: Resource<Unit> = Resource.Uninitialized,
    val capturedUserPhoto: Bitmap? = null,
    val imageCaptureFailedError: Resource<ImageCaptureError> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
)