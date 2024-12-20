package com.example.testapp.domain.models.reaction

import java.time.Instant

data class Reaction(
    val reactionId: String? = null,
    val messageId: String,
    val userId: String,
    val emojiReaction: String,
    val createdAt: Instant
)
