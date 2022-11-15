package com.strikeprotocols.mobile.presentation.entrance

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.VerifyUser

data class EntranceState(
    val userDestinationResult: Resource<UserDestination> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val isKeyHardwareBacked: Resource<Boolean> = Resource.Uninitialized,
)

/**
 * Total of 7 different destinations for a user.
 * LOGIN
 * HOME
 * INVALID_KEY
 * KEY_MANAGEMENT_CREATION
 * KEY_MANAGEMENT_RECOVERY
 * KEY_MIGRATION
 * REGENERATION
 *
 */
enum class UserDestination {
    FORCE_UPDATE, LOGIN, KEY_MANAGEMENT_CREATION, KEY_MANAGEMENT_RECOVERY,
    REGENERATION, KEY_MIGRATION, HOME, INVALID_KEY
}