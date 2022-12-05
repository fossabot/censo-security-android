package com.censocustody.mobile.service

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
import com.censocustody.mobile.BuildConfig
import com.censocustody.mobile.MainActivity
import com.censocustody.mobile.R
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.PushRepository
import com.censocustody.mobile.data.UserRepository
import com.censocustody.mobile.data.models.PushBody
import com.censocustody.mobile.presentation.Screen
import com.censocustody.mobile.service.MessagingService.Companion.BODY_KEY
import com.censocustody.mobile.service.MessagingService.Companion.DEFAULT_BODY
import com.censocustody.mobile.service.MessagingService.Companion.DEFAULT_TITLE
import com.censocustody.mobile.service.MessagingService.Companion.NOTIFICATION_DISPLAYED_KEY
import com.censocustody.mobile.service.MessagingService.Companion.TITLE_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

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
                    RaygunClient.send(Exception("Failed to register token from automatic registration: ${pushResponse.censoError}"))
                }
            }
        }
    }

    private fun parsePushData(data: Map<String, String>): PushData {
        //TODO: str-258 https://linear.app/strike-android/issue/STR-258/consume-the-push-data-image-in-the-notification-icon
        // Consume image data for notification icon
        return PushData(
            title = data.getOrDefault(TITLE_KEY, DEFAULT_TITLE),
            body = data.getOrDefault(BODY_KEY, DEFAULT_BODY)
        )
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(pushData: PushData) {
        val splashScreenIntent = Intent(
            Intent.ACTION_VIEW,
            Screen.ApprovalListRoute.buildScreenDeepLinkUri().toUri(),
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
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            getString(R.string.default_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = abs(Date().time.toInt())
        notificationManager.notify(notificationId, notificationBuilder.build())

        //Send a broadcast to the main activity to update the approvals data
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


        const val DEFAULT_TITLE = "Censo Custody"
        const val DEFAULT_BODY = "Verification Needed"
    }
}

data class PushData(val body: String, val title: String)