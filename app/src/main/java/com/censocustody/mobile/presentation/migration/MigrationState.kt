package com.censocustody.mobile.presentation.migration

import com.censocustody.mobile.common.BioPromptReason
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.BioPromptData
import com.censocustody.mobile.data.models.Signers
import com.censocustody.mobile.data.models.VerifyUser
import javax.crypto.Cipher

data class MigrationState(
    //initial data + key management flow data
    val initialData: VerifyUserInitialData? = null,
    val verifyUser: VerifyUser? = null,

    val finishedMigration: Boolean = false,

    //API calls
    val addWalletSigner: Resource<Signers> = Resource.Uninitialized,

    //Utility state
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED),
    val kickUserOut: Boolean = false,
    val showToast: Resource<String> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
)