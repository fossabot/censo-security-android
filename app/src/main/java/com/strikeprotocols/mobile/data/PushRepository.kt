package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.PushBody
import javax.inject.Inject

interface PushRepository {
    suspend fun addPushNotification(pushBody: PushBody): PushBody?
}

class PushRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService
) : PushRepository {

    override suspend fun addPushNotification(pushBody: PushBody): PushBody? {
        return try {
            api.addPushNotificationToken(pushBody)
        } catch (e: Exception) {
            strikeLog(message = "registration failed")
            null
        }
    }
}