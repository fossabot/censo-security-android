package com.strikeprotocols.mobile.presentation.account

import com.strikeprotocols.mobile.common.Resource

data class AccountState(
    val email: String = "",
    val name: String = "",
    val logoutResult: Resource<Boolean> = Resource.Uninitialized,
)