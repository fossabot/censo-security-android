package com.strikeprotocols.mobile.common

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT_PERMANENT
import androidx.fragment.app.FragmentActivity
import com.strikeprotocols.mobile.R

object BioCryptoUtil {

    const val FAIL_ERROR = -1
    const val NO_CIPHER_CODE = -2

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

    fun handleBioPromptOnFail(context: Context, errorCode: Int, handleFailure: () -> Unit) {
        val bioPromptFailedReason = getBioPromptFailedReason(errorCode)

        val message = getBioPromptMessage(bioPromptFailedReason, context)

        if (message.isNotEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        handleFailure()
    }

    private fun getBioPromptFailedReason(errorCode: Int): BioPromptFailedReason =
        when (errorCode) {
            ERROR_LOCKOUT, ERROR_LOCKOUT_PERMANENT -> BioPromptFailedReason.FAILED_TOO_MANY_ATTEMPTS
            NO_CIPHER_CODE -> BioPromptFailedReason.CIPHER_NULL
            else -> BioPromptFailedReason.BIOMETRY_FAILED
        }

    private fun getBioPromptMessage(
        bioPromptReason: BioPromptFailedReason,
        context: Context
    ): String =
        when (bioPromptReason) {
            BioPromptFailedReason.BIOMETRY_FAILED -> ""
            BioPromptFailedReason.CIPHER_NULL -> context.getString(R.string.key_management_bio_canceled)
            BioPromptFailedReason.FAILED_TOO_MANY_ATTEMPTS -> context.getString(R.string.too_many_failed_attempts)
        }
}

enum class BioPromptReason {
    CREATE, RECOVER, MIGRATION, UNINITIALIZED
}

enum class BioPromptFailedReason {
    BIOMETRY_FAILED, CIPHER_NULL, FAILED_TOO_MANY_ATTEMPTS
}