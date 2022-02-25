package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.VerifyUser

data class SignInState(
    val email: String = "",
    val password: String = "",
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyResult: Resource<VerifyUser> = Resource.Uninitialized
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}