package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.InitialAuthData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.WalletSigners

data class SignInState(
    val email: String = "",
    val password: String = "",
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val initialAuthData: InitialAuthData? = null,

    //Async Data
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<WalletSigners> = Resource.Uninitialized,
    val addWalletSignerResult: Resource<WalletSigner> = Resource.Uninitialized,
    val saveCredential: Resource<Unit> = Resource.Uninitialized,
    val retrieveCredential: Resource<String> = Resource.Uninitialized
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    val userHasPublicKey = verifyUserResult is Resource.Success && !verifyUserResult.data?.publicKeys.isNullOrEmpty()

    val loadingData = loginResult is Resource.Loading || verifyUserResult is Resource.Loading
            || addWalletSignerResult is Resource.Loading || saveCredential is Resource.Loading

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}