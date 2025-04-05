package com.example.testapp.domain.models.message

import java.time.Instant

data class MessageRead(
    val chatId: String,
    val userId: String,
    val lastReadMessageId: String,
    val readAt: Instant
)
