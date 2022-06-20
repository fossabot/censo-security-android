package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.InitialAuthData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import java.util.*

data class SignInState(
    val email: String = "",
    val password: String = "",
    val phrase: String? = null,
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val initialAuthData: InitialAuthData? = null,
    val manualAuthFlowLoading : Boolean = false,
    val autoAuthFlowLoading: Boolean = false,
    val keyRegenerationLoading: Boolean = false,
    val shouldAbortUserFromAuthFlow: Boolean = false,
    val showPhraseVerificationDialog: Boolean = false,
    val showPhraseVerificationUI: Boolean = false,
    val showPhraseKeyRegenerationUI: Boolean = false,

    //Async Data
    //checking if user is logged in
    val loggedInStatusResult: Resource<Boolean> = Resource.Loading(),
    //logging the user in
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
    val addWalletSignerResult: Resource<WalletSigner> = Resource.Uninitialized,
    val regenerateKeyFromPhrase: Resource<Unit> = Resource.Uninitialized,
    val verifiedPhrase: Resource<Unit> = Resource.Uninitialized,
    val keyValid: Resource<Unit> = Resource.Uninitialized,
    val regenerateData: Resource<WalletSigner> = Resource.Uninitialized,

    //Exception Data
    val authFlowException: Resource<Exception> = Resource.Uninitialized,

    //Autocomplete State
    val wordQuery: String = "",
    val wordPredictions: List<String> = emptyList()
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()

    companion object {
        const val DEFAULT_SIGN_IN_ERROR_MESSAGE = "Something Went Wrong"
    }
}