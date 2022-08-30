package com.strikeprotocols.mobile.common

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.strikeprotocols.mobile.R

object BioCryptoUtil {

    const val TOO_MANY_ATTEMPTS_CODE = 7
    const val FINGERPRINT_DISABLED_CODE = 9
    const val FAIL_ERROR = -1

    fun createPromptInfo(context: Context, isSavingData : Boolean, biometryLogin: Boolean = false): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                if(biometryLogin) {
                    context.getString(R.string.biometry_login_title)
                } else {
                    if (isSavingData)
                        context.getString(R.string.save_key)
                    else
                        context.getString(R.string.retrieve_key)
                }
            )
            .setSubtitle(
                if(biometryLogin) {
                    context.getString(R.string.biometry_login_subtitle)
                } else {
                    if (isSavingData)
                        context.getString(R.string.performing_biometry_check_save)
                    else
                        context.getString(R.string.performing_biometry_check_retrieve)
                }
            )
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()

    fun createBioPrompt(
        fragmentActivity: FragmentActivity,
        onSuccess: (cryptoObject: BiometricPrompt.CryptoObject?) -> Unit,
        onFail: (errorCode: Int) -> Unit) =
        BiometricPrompt(fragmentActivity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFail(errorCode)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result.cryptoObject)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail(FAIL_ERROR)
            }
        })
}

enum class BioPromptReason {
    CREATE, RECOVER, MIGRATION, UNINITIALIZED
}