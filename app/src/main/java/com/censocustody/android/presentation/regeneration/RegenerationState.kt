package com.censocustody.android.presentation.regeneration

import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.WalletSigner
import javax.crypto.Cipher

data class RegenerationState(
    //initial data + key management flow data
    val finishedRegeneration: Boolean = false,

    val triggerBioPrompt: Resource<CryptoObject> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED),
    val showToast: Resource<String> = Resource.Uninitialized,

    //API calls
    val addWalletSigner: Resource<WalletSigner> = Resource.Uninitialized,
    val regenerationError: Resource<CensoError> = Resource.Uninitialized,
)