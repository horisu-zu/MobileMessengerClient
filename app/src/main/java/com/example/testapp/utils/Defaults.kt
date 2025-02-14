package com.example.testapp.utils

import android.content.Context
import com.example.testapp.R
import com.example.testapp.domain.models.user.UserStatus
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Defaults {

    const val USER_SERVICE_PORT = 8081
    const val CHAT_SERVICE_PORT = 8082
    const val MESSAGE_SERVICE_PORT = 8083
    const val REACTION_SERVICE_PORT = 8084
    const val baseUrl = "http://192.168.1.106"
    const val webSocketBaseUrl = "ws://192.168.1.107"

    fun calculateUserStatus(context: Context, userStatus: UserStatus?): String {
        if (userStatus == null) {
            return ""
        }

        return if (userStatus.onlineStatus) {
            context.getString(R.string.status_online)
        } else {
            val lastSeenInstant = userStatus.lastSeen
            val currentTimeInstant = Instant.now()

            val duration = Duration.between(lastSeenInstant, currentTimeInstant)
            val diffMillis = duration.toMillis()

            when {
                diffMillis < 60000 -> context.getString(R.string.status_now)
                diffMillis < 3600000 -> {
                    val minutes = duration.toMinutes()
                    if (minutes == 1L) {
                        context.getString(R.string.status_minute_ago, minutes)
                    } else {
                        context.getString(R.string.status_minutes_ago, minutes)
                    }
                }
                diffMillis < 86400000 -> {
                    val hours = duration.toHours()
                    when (hours) {
                        1L -> context.getString(R.string.status_hour_ago, hours)
                        in 2..4 -> context.getString(R.string.status_hours_ago, hours)
                        else -> context.getString(R.string.status_hours_ago_v2, hours)
                    }
                }
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                    val lastSeenLocalDate = lastSeenInstant.atZone(ZoneId.systemDefault()).toLocalDate()
                    val formattedDate = formatter.format(lastSeenLocalDate)

                    context.getString(R.string.status_days, formattedDate)
                }
            }
        }
    }

    suspend fun fetchEmojiUrls(storage: FirebaseStorage): List<String> {
        return try {
            val storageRef = storage.reference.child("emojis")
            val result = storageRef.listAll().await()
            val urls = result.items.map { it.downloadUrl.await().toString() }
            urls
        } catch (e: Exception) {
            emptyList()
        }
    }
}