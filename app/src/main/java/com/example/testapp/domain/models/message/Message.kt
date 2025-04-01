package com.example.testapp.domain.models.message

import java.time.Instant

data class Message(
    val messageId: String? = null,
    val chatId: String,
    val senderId: String,
    val message: String? = null,
    val createdAt: Instant,
    val isRead: Boolean = false,
    val replyTo: String? = null,
    val isPinned: Boolean = false,
    val isTranslated: Boolean = false
)
