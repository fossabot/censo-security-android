package com.censocustody.android.presentation.reset_password

import com.censocustody.android.common.Resource

data class ResetPasswordState(
    val email: String = "",
    val emailErrorEnabled: Boolean = false,
    val resetPasswordResult: Resource<String> = Resource.Uninitialized,
) {
    val resetButtonEnabled = email.isNotEmpty()
}