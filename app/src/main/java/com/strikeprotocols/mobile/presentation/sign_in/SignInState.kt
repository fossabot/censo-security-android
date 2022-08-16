package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner

data class SignInState(
    val email: String = "sharris@blue.rock",
    val password: String = "ez4GK8testing",
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}