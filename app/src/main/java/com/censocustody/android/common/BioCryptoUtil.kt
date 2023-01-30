package com.censocustody.android.common

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT_PERMANENT
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.R

object BioCryptoUtil {

    const val FAIL_ERROR = -1
    const val NO_CIPHER_CODE = -2

    fun createPromptInfo(context: Context) =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.complete_biometry))
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

    fun getBioPromptFailedReason(errorCode: Int): BioPromptFailedReason =
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
    UNINITIALIZED, RETURN_LOGIN, SAVE_SENTINEL, FOREGROUND_RETRIEVAL, FOREGROUND_SAVE,
    RETRIEVE_V3_ROOT_SEED, SAVE_V3_ROOT_SEED, RETRIEVE_DEVICE_SIGNATURE, RETRIEVE_KEY_AGREEMENT
}

enum class BioPromptFailedReason {
    BIOMETRY_FAILED, CIPHER_NULL, FAILED_TOO_MANY_ATTEMPTS
}