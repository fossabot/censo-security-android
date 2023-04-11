package com.censocustody.android.presentation.keys_upload

import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.WalletSigner

data class KeysUploadState(
    val finishedUpload: Boolean = false,
    val walletSigners: List<WalletSigner> = emptyList(),

    //API calls
    val addWalletSigner: Resource<Unit> = Resource.Uninitialized,

    //Utility state
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED),
    val kickUserOut: Boolean = false,
    val showToast: Resource<String> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
)