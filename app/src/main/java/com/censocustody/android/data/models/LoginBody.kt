package com.censocustody.android.data.models

import com.google.gson.annotations.SerializedName

data class TokenBody(
    val email: String
)

data class LoginBody(
    val credentials: LoginCredentials,
    val deviceId: String
)

data class LoginCredentials(
    val type: LoginType,
    val email: String,
    val verificationToken: String? = null,
    val timestamp: String? = null,
    val timestampSignature: String? = null,
)

enum class LoginType(val value: String) {
    @SerializedName("SignatureBased") SIGNATURE_BASED("SignatureBased"),
    @SerializedName("EmailVerificationBased") EMAIL_VERIFICATION_BASED("EmailVerificationBased")
}
