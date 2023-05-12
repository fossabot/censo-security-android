package com.censocustody.android.presentation.pending_approval

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.VerifyUser

data class PendingApprovalState(
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val sendUserToEntrance: Resource<Boolean> = Resource.Uninitialized,
)