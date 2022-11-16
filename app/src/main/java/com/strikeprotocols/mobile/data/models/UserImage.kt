package com.strikeprotocols.mobile.data.models

data class UserImage(
    val image: String,//Base64 encoded image data
    val type: LogoType,//Image type, we are using JPG mainly
    val signature: String//Signed image data
)

data class LogoType(
    val type: ImageType
)

enum class ImageType(val value: String) {
    JPEG(value = "jpg"), PNG(value = "png")
}