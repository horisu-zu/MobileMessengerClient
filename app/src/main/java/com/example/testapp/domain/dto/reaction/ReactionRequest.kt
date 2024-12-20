package com.example.testapp.domain.dto.reaction

data class ReactionRequest(
    val messageId: String,
    val userId: String,
    val emoji: String
)
