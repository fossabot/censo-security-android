package com.censocustody.android.presentation.device_registration

import android.graphics.Bitmap
import com.censocustody.android.common.Resource
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.BootstrapDevice
import com.censocustody.android.data.models.UserDevice
import com.censocustody.android.data.models.VerifyUser
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

data class DeviceRegistrationState(

    //Initial state
    val userLoggedIn: Boolean = true,
    val verifyUserDetails: VerifyUser? = null,

    //Util state
    val capturingDeviceKey: Resource<Boolean> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,

    //Image Data
    val triggerImageCapture: Resource<Unit> = Resource.Uninitialized,
    val capturedUserPhoto: Bitmap? = null,

    //API call to add standard device key
    val addUserDevice: Resource<Unit> = Resource.Uninitialized,
    val deviceRegistrationError: DeviceRegistrationError = DeviceRegistrationError.NONE,

    //Standard device key information
    val standardKeyName: String = "",
    val standardPublicKey: String = "",

    //Bootstrap device key information
    val bootstrapKeyName: String = "",
    val bootstrapPublicKey: String = "",

    //Bootstrap user information
    val userDevice: UserDevice? = null,
    val bootstrapDevice: BootstrapDevice? = null,
)


enum class DeviceRegistrationError {
    NONE, API, IMAGE_CAPTURE, SIGNING_IMAGE, BIOMETRY
}

data class DeviceRegistrationInitialData(
    val verifyUserDetails: VerifyUser?,
) {
    companion object {
        fun toJson(
            deviceRegistrationInitialData: DeviceRegistrationInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(deviceRegistrationInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): DeviceRegistrationInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, DeviceRegistrationInitialData::class.java)
        }
    }
}