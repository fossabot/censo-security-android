package com.censocustody.mobile.presentation.account

import com.censocustody.mobile.common.Resource

data class AccountState(
    val email: String = "",
    val name: String = "",
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
)