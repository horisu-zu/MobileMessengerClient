package com.example.testapp.domain.models.chat

import java.time.Instant

data class ChatParticipant(
    val chatId: String,
    val userId: String,
    val role: GroupRole? = GroupRole.MEMBER,
    val joinedAt: Instant
)