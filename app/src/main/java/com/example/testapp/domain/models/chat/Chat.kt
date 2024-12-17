package com.example.testapp.domain.models.chat

import java.time.Instant

data class Chat(
    val chatId: String? = null,
    val chatTypeId: Int,
    val createdAt: Instant
)
