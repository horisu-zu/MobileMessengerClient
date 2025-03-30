package com.example.testapp.domain.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService: FirebaseMessagingService() {

    private lateinit var notificationHandler: CustomNotification

    override fun onCreate() {
        super.onCreate()
        notificationHandler = CustomNotification(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationId = message.notification?.channelId?.hashCode() ?: message.sentTime.toInt()
        val title = message.notification?.title ?: ""
        val body = message.notification?.body ?: ""

        val chatId = message.data["chatId"] ?: ""
        val messageId = message.data["messageId"] ?: ""
        val avatarUrl = message.data["avatarUrl"] ?: ""

        notificationHandler.showNotification(
            notificationId = notificationId.hashCode(),
            title = title,
            body = body,
            chatId = chatId,
            messageId = messageId,
            avatarUrl = avatarUrl
        )

        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            Log.d(TAG, "Message Chat Id: $chatId")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        private const val TAG = "NotificationService"
    }
}