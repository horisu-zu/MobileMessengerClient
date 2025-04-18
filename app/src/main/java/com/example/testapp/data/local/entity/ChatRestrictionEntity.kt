package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testapp.domain.models.chat.ChatRestriction
import java.time.Instant

@Entity(tableName = "chat_restriction")
data class ChatRestrictionEntity(
    @PrimaryKey val restrictionId: String,
    val chatId: String,
    val userId: String,
    val type: String,
    val reason: String?,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val createdBy: String?
) {
    companion object {
        fun ChatRestriction.toEntity(): ChatRestrictionEntity {
            return ChatRestrictionEntity(
                restrictionId = this.restrictionId,
                chatId = this.chatId,
                userId = this.userId,
                type = this.type,
                reason = this.reason,
                createdAt = this.createdAt,
                expiresAt = this.expiresAt,
                createdBy = this.createdBy
            )
        }

        fun ChatRestrictionEntity.toModel(): ChatRestriction {
            return ChatRestriction(
                restrictionId = this.restrictionId,
                chatId = this.chatId,
                userId = this.userId,
                type = this.type,
                reason = this.reason,
                createdAt = this.createdAt,
                expiresAt = this.expiresAt,
                createdBy = this.createdBy
            )
        }
    }
}
