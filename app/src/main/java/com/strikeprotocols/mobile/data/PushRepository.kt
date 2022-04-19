package com.strikeprotocols.mobile.data

import android.content.Context
import android.provider.Settings
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.PushBody
import javax.inject.Inject

interface PushRepository {
    suspend fun addPushNotification(pushBody: PushBody): PushBody?
    suspend fun removePushNotification()
    suspend fun getDeviceId(): String
}

class PushRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val applicationContext: Context
) : PushRepository {

    companion object {
        const val DEVICE_TYPE = "android"
    }

    override suspend fun addPushNotification(pushBody: PushBody): PushBody? {
        return try {
            api.addPushNotificationToken(pushBody)
        } catch (e: Exception) {
            strikeLog(message = "registration failed")
            null
        }
    }

    override suspend fun removePushNotification() {
        val deviceId = getDeviceId()
        if (deviceId.isNotEmpty()) {
            try {
                api.removePushNotificationToken(deviceId, DEVICE_TYPE)
            } catch (e: Exception) {
                strikeLog(message = "un-registration failed")
            }
        }
    }

    @Suppress("HardwareIds")
    override suspend fun getDeviceId(): String {
        return Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )
    }


}