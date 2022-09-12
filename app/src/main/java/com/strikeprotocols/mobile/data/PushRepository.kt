package com.strikeprotocols.mobile.data

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.common.CrashReportingUtil
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.PushBody
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface PushRepository {
    suspend fun addPushNotification(pushBody: PushBody): Resource<PushBody?>
    suspend fun removePushNotification()
    suspend fun getDeviceId(): String
    suspend fun retrievePushToken(): String
}

class PushRepositoryImpl @Inject constructor(
    private val api: BrooklynApiService,
    private val applicationContext: Context
) : PushRepository, BaseRepository() {

    companion object {
        const val DEVICE_TYPE = "android"
    }

    override suspend fun retrievePushToken(): String =
        FirebaseMessaging.getInstance().token.await()

    override suspend fun addPushNotification(pushBody: PushBody): Resource<PushBody?> =
        retrieveApiResource { api.addPushNotificationToken(pushBody) }

    override suspend fun removePushNotification() {
        val deviceId = getDeviceId()
        if (deviceId.isNotEmpty()) {
            try {
                api.removePushNotificationToken(deviceId, DEVICE_TYPE)
            } catch (e: Exception) {
                RaygunClient.send(e,
                    listOf(
                        CrashReportingUtil.MANUALLY_REPORTED_TAG,
                        CrashReportingUtil.PUSH_NOTIFICATION_TAG
                    )
                )
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