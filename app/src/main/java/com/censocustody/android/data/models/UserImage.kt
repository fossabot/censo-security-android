package com.censocustody.android.data.models

import com.google.gson.annotations.SerializedName

data class UserImage(
    val image: String,//Base64 encoded image data
    val type: LogoType,//Image type, we are using JPG mainly
    val signature: String//Signed image data
)

enum class LogoType(val value: String) {
    @SerializedName("jpeg")
    JPEG(value = "jpeg"),

    @SerializedName("png")
    PNG(value = "png")
}

