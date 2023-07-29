package com.pjm.cours

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pjm.cours.data.PreferenceManager
import com.pjm.cours.ui.MainActivity
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.util.Constants

class CoursFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        notify(message)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
    }

    override fun onSendError(msgId: String, exception: Exception) {
        super.onSendError(msgId, exception)
    }

    private fun notify(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "제목"
        val sender = remoteMessage.data["sender"] ?: "보낸사람"
        val message = remoteMessage.data["message"] ?: "메시지"
        val postId = remoteMessage.data["chatRoomId"] ?: "방번호"

        if (preferenceManager.getString(Constants.CHAT_ROOM_ID, "").equals(postId)) {
        } else {
            val homeIntent = Intent(this, MainActivity::class.java)
            val chatIntent = Intent(this, ChatActivity::class.java).apply {
                putExtra(Constants.POST_ID, postId)
            }
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val intentArray = arrayOf(homeIntent, chatIntent)
            val pendingIntent = PendingIntent.getActivities(
                this,
                1,
                intentArray,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
            )

            val channelId = "my_channel"
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(sender)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(1, notificationBuilder.build())
        }
    }

}