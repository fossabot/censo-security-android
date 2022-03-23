package com.strikeprotocols.mobile.data.models

data class PushBody(
    val deviceId: String, val deviceType: String = "android", val token: String)