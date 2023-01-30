package com.censocustody.android.presentation.key_recovery

import androidx.biometric.BiometricPrompt
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource

data class KeyRecoveryState(
    val keyRecoveryData: Resource<KeyRecoveryData> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<BiometricPrompt.CryptoObject> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
)


data class KeyRecoveryData(
    val ephemeralPublicKey: String,
    val encryptedData: String
)