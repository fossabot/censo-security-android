package com.censocustody.android.presentation.key_creation

import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.Signers
import javax.crypto.Cipher

data class KeyCreationState(
    val keyGeneratedPhrase: String? = null,
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val finishedKeyUpload: Boolean = false,

    //API calls
    val uploadingKeyProcess: Resource<Signers> = Resource.Uninitialized,
)