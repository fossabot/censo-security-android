package com.censocustody.android.presentation.sign_in

import android.util.Patterns
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import okhttp3.ResponseBody
import javax.crypto.Cipher

data class SignInState(
    val email: String = "",
    val verificationToken: String = "",
    val loginStep: LoginStep = LoginStep.EMAIL_ENTRY,
    val emailErrorEnabled: Boolean = false,
    val verificationTokenErrorEnabled: Boolean = false,
    val sendVerificationEmail: Resource<ResponseBody> = Resource.Uninitialized,
    val loginResult: Resource<LoginResponse> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Cipher?> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val exitLoginFlow : Resource<Unit> = Resource.Uninitialized,
    ) {
    val signInButtonEnabled = email.isNotEmpty() && verificationToken.isNotEmpty()

    fun emailValid() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun verificationTokenValid() = verificationToken.isNotEmpty()
}

enum class LoginStep {
    EMAIL_ENTRY, TOKEN_ENTRY
}