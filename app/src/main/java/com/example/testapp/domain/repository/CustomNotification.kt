package com.example.testapp.domain.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import com.example.testapp.MainActivity
import com.example.testapp.MessengerApp
import com.example.testapp.R
import com.example.testapp.domain.models.notification.NotificationBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomNotification(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun showNotification(
        notificationId: Int,
        title: String,
        body: String,
        messageId: String,
        chatId: String,
        avatarUrl: String
    ) {
        if(MessengerApp.isAppInForeground) {
            CoroutineScope(Dispatchers.Main).launch {
                NotificationBus.emitNotificationEvent(notificationId, title, body, chatId, messageId, avatarUrl)
            }
        } else {
            showSystemNotification(notificationId, title, body, messageId, chatId)
        }
    }

    private fun showSystemNotification(
        notificationId: Int,
        title: String,
        body: String,
        messageId: String,
        chatId: String
    ) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("NOTIFICATION_CHAT_ID", chatId)
            putExtra("NOTIFICATION_MESSAGE_ID", messageId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationLayout = RemoteViews(context.packageName, R.layout.foreground_notification_collapsed)
        //val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.foreground_notification_expanded)

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        notificationLayout.setTextViewText(R.id.notification_title, chatId)
        notificationLayout.setTextViewText(R.id.notification_time, currentTime)
        notificationLayout.setTextViewText(R.id.notification_message, body)
        notificationLayout.setTextViewText(R.id.notification_sender, title)

        val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chat_bubble)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            //.setAutoCancel(true)
            .setCustomContentView(notificationLayout)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            //.setCustomBigContentView(notificationLayoutExpanded)
            .build()

        notificationManager.notify(notificationId, customNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(context, R.string.default_notification_channel_id),
            getString(context, R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Chat messages"
            enableLights(true)
            lightColor = Color.GREEN
            setShowBadge(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "chat_messages_channel"
    }
}