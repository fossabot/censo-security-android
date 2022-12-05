package com.censocustody.mobile.presentation.sign_in

import android.util.Patterns
import com.censocustody.mobile.common.BioPromptReason
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.models.LoginResponse
import com.censocustody.mobile.data.models.VerifyUser
import com.censocustody.mobile.data.models.WalletSigner
import javax.crypto.Cipher

data class SignInState(
    val email: String = "",
    val password: String = "",
    val loginStep: LoginStep = LoginStep.EMAIL_ENTRY,
    val emailErrorEnabled: Boolean = false,
    val passwordErrorEnabled: Boolean = false,
    val loginResult: Resource<LoginResponse> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val exitLoginFlow : Resource<Unit> = Resource.Uninitialized
    ) {
    val signInButtonEnabled = email.isNotEmpty() && password.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun passwordValid() = password.isNotEmpty()
}

enum class LoginStep {
    EMAIL_ENTRY, PASSWORD_ENTRY
}