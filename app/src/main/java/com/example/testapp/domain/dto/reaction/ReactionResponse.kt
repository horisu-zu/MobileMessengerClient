package com.example.testapp.domain.dto.reaction

import java.time.Instant

data class ReactionResponse(
    val action: String,
    val reactionId: String,
    val messageId: String,
    val userId: String,
    val emoji: String,
    val createdAt: Instant
)
