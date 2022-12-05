package com.censocustody.mobile.presentation.reset_password

import android.util.Patterns
import com.censocustody.mobile.common.Resource

data class ResetPasswordState(
    val email: String = "",
    val emailErrorEnabled: Boolean = false,
    val resetPasswordResult: Resource<String> = Resource.Uninitialized,
) {
    val resetButtonEnabled = email.isNotEmpty()
    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}