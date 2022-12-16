package com.censocustody.android.presentation.account

import com.censocustody.android.common.Resource

data class AccountState(
    val email: String = "",
    val name: String = "",
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
)