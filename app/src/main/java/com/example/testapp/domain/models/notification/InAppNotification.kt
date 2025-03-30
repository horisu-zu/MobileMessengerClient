package com.example.testapp.domain.models.notification

data class InAppNotification(
    val notificationId: Int,
    val title: String,
    val body: String,
    val chatId: String,
    val messageId: String,
    val avatarUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)