package com.example.testapp.domain.models.chat

import java.time.Duration
import java.time.Instant

data class ChatRestriction(
    val restrictionId: String?,
    val chatId: String,
    val userId: String,
    val type: String,
    val reason: String?,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val createdBy: String?
) {
    companion object {
        fun calculateRemainingTime(expiresAt: Instant?): Duration? {
            return expiresAt?.let {
                Duration.between(Instant.now(), it)
            }
        }
    }
}
