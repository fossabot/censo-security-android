package com.censocustody.android.data.repository

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.models.PushBody
import com.censocustody.android.data.storage.SharedPrefsHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface PushRepository {
    suspend fun addPushNotification(pushBody: PushBody): Resource<PushBody?>
    suspend fun removePushNotification()
    suspend fun getDeviceId(): String
    suspend fun retrievePushToken(): String
    fun userHasSeenPushDialog() : Boolean
    fun setUserSeenPushDialog(seenDialog: Boolean)
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

    override fun userHasSeenPushDialog(): Boolean =
        SharedPrefsHelper.userHasSeenPermissionDialog()

    override fun setUserSeenPushDialog(seenDialog: Boolean) {
        SharedPrefsHelper.setUserSeenPermissionDialog(seenDialog)
    }

    override suspend fun addPushNotification(pushBody: PushBody): Resource<PushBody?> =
        retrieveApiResource { api.addPushNotificationToken(pushBody) }

    override suspend fun removePushNotification() {
        val deviceId = getDeviceId()
        if (deviceId.isNotEmpty()) {
            try {
                api.removePushNotificationToken(deviceId, DEVICE_TYPE)
            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.PUSH_NOTIFICATION_TAG)
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