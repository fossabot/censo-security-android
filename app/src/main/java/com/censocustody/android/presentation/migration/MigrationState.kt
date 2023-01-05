package com.censocustody.android.presentation.migration

import androidx.biometric.BiometricPrompt.CryptoObject
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.models.Signers
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import javax.crypto.Cipher

data class MigrationState(
    //initial data + key management flow data
    val initialData: VerifyUserInitialData? = null,
    val verifyUser: VerifyUser? = null,

    val finishedMigration: Boolean = false,
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