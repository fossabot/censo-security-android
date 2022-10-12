package com.strikeprotocols.mobile.presentation.regeneration

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.data.models.WalletSigner

data class RegenerationState(
    //initial data + key management flow data
    val finishedRegeneration: Boolean = false,

    //API calls
    val addWalletSigner: Resource<WalletSigner> = Resource.Uninitialized,
    val regenerationError: Resource<StrikeError> = Resource.Uninitialized,
)