package com.censocustody.android.presentation.keys_upload

import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.WalletSigner

data class KeysUploadState(
    val finishedUpload: Boolean = false,
    val walletSigners: List<WalletSigner> = emptyList(),

    //API calls
    val addWalletSigner: Resource<Signers> = Resource.Uninitialized,

    //Utility state
    val triggerBioPrompt: Resource<CryptoObject> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED),
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val kickUserOut: Boolean = false,
    val showToast: Resource<String> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
)