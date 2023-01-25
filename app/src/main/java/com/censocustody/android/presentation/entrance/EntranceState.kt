package com.censocustody.android.presentation.entrance

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.VerifyUser

data class EntranceState(
    val userDestinationResult: Resource<UserDestination> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
)

/**
 * Total of 7 different destinations for a user.
 * LOGIN
 * HOME
 * INVALID_KEY
 * KEY_CREATION
 * KEY_RECOVERY
 * KEY_MANAGEMENT
 * KEY_MIGRATION
 * REGENERATION
 *
 */
enum class UserDestination {
    FORCE_UPDATE, LOGIN, KEY_CREATION, KEY_RECOVERY, KEY_MANAGEMENT,
    REGENERATION, KEY_MIGRATION, HOME, INVALID_KEY, DEVICE_REGISTRATION
}