package com.strikeprotocols.mobile.presentation.splash

import com.strikeprotocols.mobile.common.Resource

data class SplashState(
    val userLoggedInResult: Resource<Boolean> = Resource.Uninitialized,
)
