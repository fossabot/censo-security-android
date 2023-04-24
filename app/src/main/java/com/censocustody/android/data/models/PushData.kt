package com.censocustody.android.data.models

import com.censocustody.android.data.repository.PushRepositoryImpl.Companion.DEVICE_TYPE

data class PushBody(
    val deviceId: String, val deviceType: String = DEVICE_TYPE, val token: String)