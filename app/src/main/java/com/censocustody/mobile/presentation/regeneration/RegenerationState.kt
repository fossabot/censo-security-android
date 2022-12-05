package com.censocustody.mobile.presentation.regeneration

import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.CensoError
import com.censocustody.mobile.data.models.WalletSigner

data class RegenerationState(
    //initial data + key management flow data
    val finishedRegeneration: Boolean = false,

    //API calls
    val addWalletSigner: Resource<WalletSigner> = Resource.Uninitialized,
    val regenerationError: Resource<CensoError> = Resource.Uninitialized,
)