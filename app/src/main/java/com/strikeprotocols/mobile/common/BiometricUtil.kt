package com.strikeprotocols.mobile.common

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.strikeprotocols.mobile.R

object BiometricUtil {

    object Companion {
        enum class BiometricsStatus {
            BIOMETRICS_ENABLED, BIOMETRICS_DISABLED, BIOMETRICS_NOT_AVAILABLE
        }
    }

    fun getBasicBiometricPromptBuilder(context: Context) =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometry_check))
            .setSubtitle(context.getString(R.string.performing_biometry_check))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)

    fun createBioPrompt(fragmentActivity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit) =
        BiometricPrompt(fragmentActivity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFail()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail()
            }
        })

    fun checkForBiometricFeaturesOnDevice(context: Context): Companion.BiometricsStatus {
        //Checking the biometric status of the device, if it is enabled, disabled, or not available
        return when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Companion.BiometricsStatus.BIOMETRICS_ENABLED
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Companion.BiometricsStatus.BIOMETRICS_DISABLED
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Companion.BiometricsStatus.BIOMETRICS_NOT_AVAILABLE
            }
            else -> {
                Companion.BiometricsStatus.BIOMETRICS_NOT_AVAILABLE
            }
        }
    }
}