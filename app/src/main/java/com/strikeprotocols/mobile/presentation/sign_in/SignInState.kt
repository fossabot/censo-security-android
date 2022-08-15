package com.strikeprotocols.mobile.presentation.sign_in

import android.util.Patterns
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.InitialAuthData
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner

data class SignInState(
    val email: String = "",
    val password: String = "",
    val phrase: String? = null,
    val pastedPhrase: String = "",
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val initialAuthData: InitialAuthData? = null,
    val manualAuthFlowLoading : Boolean = false,
    val autoAuthFlowLoading: Boolean = false,
    val keyRegenerationLoading: Boolean = false,
    val shouldAbortUserFromAuthFlow: Boolean = false,
    val showKeyCreationUI: Boolean = false,
    val showKeyRecoveryUI: Boolean = false,

    val keyCreationFlowStep : KeyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED,
    val keyRecoveryFlowStep : KeyRecoveryFlowStep = KeyRecoveryFlowStep.UNINITIALIZED,

    //Async Data
    //checking if user is logged in
    val loggedInStatusResult: Resource<Boolean> = Resource.Loading(),
    //logging the user in
    val loginResult: Resource<String> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
    val addWalletSignerResult: Resource<WalletSigner> = Resource.Uninitialized,
    val recoverKeyError: String? = null,
    val createKeyError: String? = null,
    val keyValid: Resource<Unit> = Resource.Uninitialized,
    val regenerateData: Resource<WalletSigner> = Resource.Uninitialized,
    val showToast: Resource<String> = Resource.Uninitialized,

    val finalizingKeyCreation: Resource<WalletSigner?> = Resource.Uninitialized,
    val finalizingKeyRecovery: Resource<WalletSigner?> = Resource.Uninitialized,

    //Exception Data
    val authFlowException: Resource<Exception> = Resource.Uninitialized,
    val recoverKeyFlowException: Resource<Exception> = Resource.Uninitialized,

    val wordIndex: Int = 0,

    val confirmPhraseWordsState: ConfirmPhraseWordsState = ConfirmPhraseWordsState()
) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}