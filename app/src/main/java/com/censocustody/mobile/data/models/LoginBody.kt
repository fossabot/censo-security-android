package com.censocustody.mobile.data.models

import com.google.gson.annotations.SerializedName

data class LoginBody(
    val credentials: LoginCredentials,
    val deviceId: String
)

data class LoginCredentials(
    val type: LoginType,
    val email: String,
    val password: String? = null,
    val timestamp: String? = null,
    val timestampSignature: String? = null,
)

enum class LoginType(val value: String) {
    @SerializedName("SignatureBased") SIGNATURE_BASED("SignatureBased"),
    @SerializedName("PasswordBased") PASSWORD_BASED("PasswordBased")
}
