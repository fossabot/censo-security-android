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
 * KEY_MANAGEMENT_CREATION
 * KEY_MANAGEMENT_RECOVERY
 * KEY_MIGRATION
 * REGENERATION
 *
 */
enum class UserDestination {
    FORCE_UPDATE, LOGIN, KEY_MANAGEMENT_CREATION, KEY_MANAGEMENT_RECOVERY,
    UPLOAD_KEYS, HOME, INVALID_KEY, DEVICE_REGISTRATION
}