package com.censocustody.mobile.data.models

import com.censocustody.mobile.data.PushRepositoryImpl.Companion.DEVICE_TYPE

data class PushBody(
    val deviceId: String, val deviceType: String = DEVICE_TYPE, val token: String)