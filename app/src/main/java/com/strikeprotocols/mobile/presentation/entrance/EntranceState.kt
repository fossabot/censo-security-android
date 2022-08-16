package com.strikeprotocols.mobile.presentation.entrance

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementFlow

data class EntranceState(
    val userDestinationResult: Resource<UserDestination> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val walletSignersResult: Resource<List<WalletSigner?>> = Resource.Uninitialized
)

/**
 * Total of 6 different destinations for a user.
 * LOGIN
 * HOME
 * INVALID_KEY
 * KEY_MANAGEMENT_CREATION
 * KEY_MANAGEMENT_RECOVERY
 * KEY_MANAGEMENT_REGENERATION
 *
 */
enum class UserDestination {
    LOGIN, KEY_MANAGEMENT_CREATION, KEY_MANAGEMENT_RECOVERY,
    KEY_MANAGEMENT_REGENERATION, HOME, INVALID_KEY
}