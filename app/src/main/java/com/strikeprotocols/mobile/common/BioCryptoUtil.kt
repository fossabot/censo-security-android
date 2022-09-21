package com.strikeprotocols.mobile.common

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT_PERMANENT
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import com.strikeprotocols.mobile.R

object BioCryptoUtil {

    const val FAIL_ERROR = -1
    const val NO_CIPHER_CODE = -2

    fun createPromptInfo(context: Context, bioPromptReason: BioPromptReason) =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle(context, bioPromptReason))
            .setSubtitle(promptMessage(context, bioPromptReason))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(false)
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()

    private fun promptTitle(context: Context, bioPromptReason: BioPromptReason): String =
        when (bioPromptReason) {
            BioPromptReason.CREATE_KEY,
            BioPromptReason.RECOVER_KEY,
            BioPromptReason.MIGRATE_BIOMETRIC_KEY -> context.getString(R.string.save_key)
            BioPromptReason.UNINITIALIZED -> context.getString(R.string.save_key)
            BioPromptReason.RETURN_LOGIN -> context.getString(R.string.biometry_login_title)
            BioPromptReason.SAVE_SENTINEL -> context.getString(R.string.save_key)
            BioPromptReason.FOREGROUND_RETRIEVAL -> context.getString(R.string.retrieve_key)
            BioPromptReason.FOREGROUND_SAVE -> context.getString(R.string.save_key)
            BioPromptReason.APPROVAL -> context.getString(R.string.retrieve_key)
        }

    private fun promptMessage(context: Context, bioPromptReason: BioPromptReason): String =
        when (bioPromptReason) {
            BioPromptReason.CREATE_KEY -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.RECOVER_KEY -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.MIGRATE_BIOMETRIC_KEY -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.UNINITIALIZED -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.RETURN_LOGIN -> context.getString(R.string.biometry_login_subtitle)
            BioPromptReason.SAVE_SENTINEL -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.FOREGROUND_RETRIEVAL -> context.getString(R.string.performing_biometry_check_retrieve)
            BioPromptReason.FOREGROUND_SAVE -> context.getString(R.string.performing_biometry_check_save)
            BioPromptReason.APPROVAL -> context.getString(R.string.performing_biometry_check_retrieve)
        }


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
    CREATE_KEY, RECOVER_KEY, MIGRATE_BIOMETRIC_KEY, UNINITIALIZED, RETURN_LOGIN,
    SAVE_SENTINEL, FOREGROUND_RETRIEVAL, FOREGROUND_SAVE, APPROVAL
}

enum class BioPromptFailedReason {
    BIOMETRY_FAILED, CIPHER_NULL, FAILED_TOO_MANY_ATTEMPTS
}