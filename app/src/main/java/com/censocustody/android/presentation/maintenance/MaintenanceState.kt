package com.censocustody.android.presentation.maintenance

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.VerifyUser

data class MaintenanceState(
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val sendUserToEntrance: Resource<Boolean> = Resource.Uninitialized,
    val userLoggedIn: Boolean = false
)