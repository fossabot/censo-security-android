package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.InitialAuthData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner

data class SignInState(
    val email: String = "",
    val password: String = "",
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val initialAuthData: InitialAuthData? = null,
    val loadingData : Boolean = false,
    val shouldAbortUserFromAuthFlow: Boolean = false,
    val shouldDisplaySmartLockDialog: Boolean = false,

    //Async Data
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
    val addWalletSignerResult: Resource<WalletSigner> = Resource.Uninitialized,
    val saveCredential: Resource<Unit> = Resource.Uninitialized,
    val retrieveCredential: Resource<String> = Resource.Uninitialized,
    val keyValid: Resource<Unit> = Resource.Uninitialized
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}