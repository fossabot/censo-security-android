package com.censocustody.mobile.presentation.entrance

import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.models.VerifyUser

data class EntranceState(
    val userDestinationResult: Resource<UserDestination> = Resource.Uninitialized,
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
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