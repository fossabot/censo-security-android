package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.data.PushRepositoryImpl.Companion.DEVICE_TYPE

data class PushBody(
    val deviceId: String, val deviceType: String = DEVICE_TYPE, val token: String)