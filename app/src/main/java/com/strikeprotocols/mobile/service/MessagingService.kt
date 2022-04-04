package com.strikeprotocols.mobile.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.strikeprotocols.mobile.MainActivity
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.PushRepository
import com.strikeprotocols.mobile.data.models.PushBody
import com.strikeprotocols.mobile.service.MessagingService.Companion.BODY_KEY
import com.strikeprotocols.mobile.service.MessagingService.Companion.DEFAULT_BODY
import com.strikeprotocols.mobile.service.MessagingService.Companion.DEFAULT_KEY_ONE
import com.strikeprotocols.mobile.service.MessagingService.Companion.DEFAULT_KEY_TWO
import com.strikeprotocols.mobile.service.MessagingService.Companion.DEFAULT_TITLE
import com.strikeprotocols.mobile.service.MessagingService.Companion.KEY_ONE_KEY
import com.strikeprotocols.mobile.service.MessagingService.Companion.KEY_TWO_KEY
import com.strikeprotocols.mobile.service.MessagingService.Companion.TITLE_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var pushRepository: PushRepository

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
        //sendRegistrationToServer(token)
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
                strikeLog(message = "sendRegistrationTokenToServer($token)")

                val deviceId = Settings.Secure.getString(
                    applicationContext.contentResolver, Settings.Secure.ANDROID_ID
                )

                val pushBody = PushBody(
                    deviceId = deviceId,
                    token = token
                )

                val pushResponse = pushRepository.addPushNotification(pushBody)

                if (pushResponse != null) {
                    strikeLog(message = "Successfully registered push token")
                } else {
                    strikeLog(message = "Failed to register push token")
                }
            }
        }
    }

    private fun parsePushData(data: Map<String, String>): PushData {
        return PushData(
            title = data.getOrDefault(TITLE_KEY, DEFAULT_TITLE),
            body = data.getOrDefault(BODY_KEY, DEFAULT_BODY),
            keyOne = data.getOrDefault(KEY_ONE_KEY, DEFAULT_KEY_ONE),
            keyTwo = data.getOrDefault(KEY_TWO_KEY, DEFAULT_KEY_TWO)
        )
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(pushData: PushData) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.strike_main_logo)
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
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = abs(Date().time.toInt())
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    object Companion {
        const val TITLE_KEY = "title"
        const val BODY_KEY = "body"
        const val KEY_ONE_KEY = "key_1"
        const val KEY_TWO_KEY = "key_2"


        const val DEFAULT_TITLE = "Strike Mobile"
        const val DEFAULT_BODY = "Verification Needed"
        const val DEFAULT_KEY_ONE = ""
        const val DEFAULT_KEY_TWO = ""
    }
}

data class PushData(val body: String, val title: String, val keyOne: String, val keyTwo: String)