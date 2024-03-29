package com.censocustody.android.data.models

import com.google.gson.annotations.SerializedName

data class UserDevice(
    val publicKey: String,
    val deviceType: DeviceType,
    val userImage: UserImage,
    val replacingDeviceIdentifier: String? = null,
    val name: String,
    val model: String
)

enum class DeviceType(val value: String) {
    @SerializedName("android") ANDROID("android"),
    @SerializedName("ios") IOS("ios");

    fun description(): String {
        return when (this) {
            ANDROID ->"Android"
            IOS -> "iOS"
        }
    }
}

data class NameAndModel(val name: String, val model: String)