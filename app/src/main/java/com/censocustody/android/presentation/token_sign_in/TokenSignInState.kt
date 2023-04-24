package com.censocustody.android.presentation.token_sign_in

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.LoginResponse
import javax.crypto.Cipher

data class TokenSignInState(
    val email: String = "",
    val verificationToken: String = "",
    val loginResult: Resource<LoginResponse> = Resource.Uninitialized,
    val exitLoginFlow : Resource<Unit> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    ) {
    fun verificationTokenValid() = verificationToken.isNotEmpty()
}