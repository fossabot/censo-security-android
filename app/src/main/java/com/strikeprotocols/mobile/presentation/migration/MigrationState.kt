package com.strikeprotocols.mobile.presentation.migration

import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.data.BioPromptData
import com.strikeprotocols.mobile.data.models.Signers
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import javax.crypto.Cipher

data class MigrationState(
    //initial data + key management flow data
    val initialData: VerifyUserInitialData? = null,
    val verifyUser: VerifyUser? = null,

    val finishedMigration: Boolean = false,
    val rootSeed: List<Byte>? = null,

    //API calls
    val addWalletSigner: Resource<Signers> = Resource.Uninitialized,

    //Utility state
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED),
    val kickUserOut: Boolean = false,
    val showToast: Resource<String> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
)