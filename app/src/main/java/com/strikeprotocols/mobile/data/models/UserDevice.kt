package com.strikeprotocols.mobile.data.models

import com.google.gson.annotations.SerializedName

data class UserDevice(
    val publicKey: String,
    val deviceType: DeviceType,
    val userImage: UserImage
)

enum class DeviceType(val value: String) {
    @SerializedName("android") ANDROID("android")
}
