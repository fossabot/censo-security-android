package com.censocustody.android.presentation.token_sign_in

import android.util.Patterns
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.LoginResponse
import javax.crypto.Cipher

data class TokenSignInState(
    val email: String = "",
    val verificationToken: String = "",
    val emailErrorEnabled: Boolean = false,
    val verificationTokenErrorEnabled: Boolean = false,
    val loginResult: Resource<LoginResponse> = Resource.Uninitialized,
    val exitLoginFlow : Resource<Unit> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    ) {
    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun verificationTokenValid() = verificationToken.isNotEmpty()
}