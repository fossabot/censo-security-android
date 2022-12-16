package com.censocustody.android.presentation.regeneration

import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.data.models.WalletSigner

data class RegenerationState(
    //initial data + key management flow data
    val finishedRegeneration: Boolean = false,

    //API calls
    val addWalletSigner: Resource<WalletSigner> = Resource.Uninitialized,
    val regenerationError: Resource<CensoError> = Resource.Uninitialized,
)