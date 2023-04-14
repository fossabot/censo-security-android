package com.censocustody.android.presentation.reauthenticate

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.LoginResponse

data class ReAuthenticateState(
    val loginResult: Resource<LoginResponse> = Resource.Uninitialized,
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,
)