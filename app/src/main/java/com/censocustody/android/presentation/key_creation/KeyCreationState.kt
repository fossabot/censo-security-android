package com.censocustody.android.presentation.key_creation

import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.WalletSigner
import javax.crypto.Cipher

data class KeyCreationState(
    val keyGeneratedPhrase: String? = null,
    val triggerBioPrompt: Resource<CryptoObject> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val finishedKeyUpload: Boolean = false,

    val walletSigners: List<WalletSigner> = emptyList(),

    //API calls
    val uploadingKeyProcess: Resource<Signers> = Resource.Uninitialized,
)