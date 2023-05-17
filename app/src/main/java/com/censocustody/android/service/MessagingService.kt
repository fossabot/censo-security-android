package com.censocustody.android.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.BuildConfig
import com.censocustody.android.MainActivity
import com.censocustody.android.R
import com.censocustody.android.common.Resource
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.repository.PushRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.data.models.PushBody
import com.censocustody.android.presentation.Screen
import com.censocustody.android.service.MessagingService.Companion.APPROVAL_REQUEST_TYPE
import com.censocustody.android.service.MessagingService.Companion.BODY_KEY
import com.censocustody.android.service.MessagingService.Companion.DEFAULT_BODY
import com.censocustody.android.service.MessagingService.Companion.DEFAULT_TITLE
import com.censocustody.android.service.MessagingService.Companion.NOTIFICATION_DISPLAYED_KEY
import com.censocustody.android.service.MessagingService.Companion.PUSH_TYPE_KEY
import com.censocustody.android.service.MessagingService.Companion.SILENT_CLEAR_TYPE
import com.censocustody.android.service.MessagingService.Companion.TITLE_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@androidx.camera.core.ExperimentalGetImage
@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var pushRepository: PushRepository

    @Inject
    lateinit var userRepository: UserRepository

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(parsePushData(remoteMessage.data))
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        scope.launch {
            val userLoggedIn = try {
                userRepository.userLoggedIn()
            } catch (e: Exception) {
                false
            }

            if (userLoggedIn) {
                sendRegistrationToServer(token)
            }
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    @SuppressLint("HardwareIds")
    fun sendRegistrationToServer(token: String?) {
        token?.let {
            scope.launch {
                val deviceId = Settings.Secure.getString(
                    applicationContext.contentResolver, Settings.Secure.ANDROID_ID
                )

                val pushBody = PushBody(
                    deviceId = deviceId,
                    token = token
                )

                val pushResponse = pushRepository.addPushNotification(pushBody)

                if (pushResponse is Resource.Error) {
                    Exception("Failed to register token from automatic registration: ${pushResponse.censoError}")
                        .sendError(CrashReportingUtil.PUSH_NOTIFICATION_TAG)
                }
            }
        }
    }

    private fun parsePushData(data: Map<String, String>): PushData {
        return PushData(
            title = data.getOrDefault(TITLE_KEY, DEFAULT_TITLE),
            body = data.getOrDefault(BODY_KEY, DEFAULT_BODY),
            pushType = data.getOrDefault(PUSH_TYPE_KEY, "")
        )
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(pushData: PushData) {
        if (pushData.pushType == SILENT_CLEAR_TYPE) {
            sendBroadcastToRefreshApprovals()
            return
        }


        val deepLink = if (pushData.pushType == APPROVAL_REQUEST_TYPE) {
            Screen.ApprovalListRoute.buildScreenDeepLinkUri().toUri()
        } else {
            Screen.EntranceRoute.buildScreenDeepLinkUri().toUri()
        }


        val splashScreenIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            this,
            MainActivity::class.java
        )

        splashScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(splashScreenIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.censo_icon_color)
            .setContentTitle(pushData.title)
            .setContentText(pushData.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            getString(R.string.default_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = abs(Date().time.toInt())
        notificationManager.notify(notificationId, notificationBuilder.build())

        if (pushData.pushType == APPROVAL_REQUEST_TYPE) {
            sendBroadcastToRefreshApprovals()
        }
    }

    fun sendBroadcastToRefreshApprovals() {
        val notificationDisplayedIntent = Intent(BuildConfig.APPLICATION_ID)
        notificationDisplayedIntent.putExtra(NOTIFICATION_DISPLAYED_KEY, true)
        sendBroadcast(notificationDisplayedIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    object Companion {
        const val NOTIFICATION_DISPLAYED_KEY = "Notification Displayed Key"

        const val TITLE_KEY = "title"
        const val BODY_KEY = "body"
        const val PUSH_TYPE_KEY = "pushType"

        const val DEFAULT_TITLE = "Censo Custody"
        const val DEFAULT_BODY = "Verification Needed"

        const val APPROVAL_REQUEST_TYPE = "ApprovalRequest"
        const val DEVICE_APPROVED_TYPE = "DeviceApproved"
        const val SILENT_CLEAR_TYPE = "SilentClear"
    }
}

data class PushData(val body: String, val title: String, val pushType: String)