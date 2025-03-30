package com.example.testapp.domain.models.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationBus {

    private val _events = MutableSharedFlow<InAppNotification>()
    val events = _events.asSharedFlow()

    suspend fun emitNotificationEvent(
        notificationId: Int,
        title: String,
        body: String,
        chatId: String,
        messageId: String,
        avatarUrl: String
    ) {
        _events.emit(InAppNotification(notificationId, title, body, chatId, messageId, avatarUrl))
    }
}