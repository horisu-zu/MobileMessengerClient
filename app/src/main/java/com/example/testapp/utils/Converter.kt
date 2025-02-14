package com.example.testapp.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.templates.Avatar
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Converter {
    fun getAttachmentDescription(attachments: List<Attachment>): String {
        val counts = attachments.groupBy {
            when (it.url.substringAfterLast('.', "").lowercase()) {
                in setOf("jpg", "jpeg", "png") -> "image"
                in setOf("gif") -> "GIF"
                in setOf("mp4", "mov", "avi") -> "video"
                in setOf("mp3", "wav", "aac") -> "audio"
                in setOf("pdf", "doc", "docx", "txt") -> "document"
                else -> "other"
            }
        }.mapValues { it.value.size }

        return when {
            counts.size == 1 -> {
                val (type, count) = counts.entries.first()
                if (type != "other") "$type${if (count > 1) "s" else ""}"
                else "${attachments.size} medias"
            }
            counts.size > 1 -> "${attachments.size} medias"
            else -> "No attachments"
        }
    }

    fun groupReactions(reactions: List<Reaction>): Map<String, List<Reaction>> {
        Log.d("ReactionsList", "Reactions: $reactions")
        return reactions
            .sortedBy { it.createdAt }
            .groupBy { it.emojiReaction }
    }

    fun groupMessagesInSequence(messages: Map<String, Message>): List<List<Message>> {
        val sortedMessages = messages.values.sortedByDescending { it.createdAt }
        return sortedMessages.fold(mutableListOf<MutableList<Message>>()) { groups, message ->
            if (groups.isEmpty() || groups.last().first().senderId != message.senderId) {
                groups.add(mutableListOf(message))
            } else {
                groups.last().add(message)
            }
            groups
        }
    }

    @Composable
    fun ChatAvatar(
        avatarUrl: String, chatType: Int, modifier: Modifier = Modifier
    ) {
        when (chatType) {
            1 -> Avatar(avatarUrl = avatarUrl, isGroupChat = false, modifier = modifier)
            2 -> Avatar(avatarUrl = avatarUrl, isGroupChat = true, modifier = modifier)
        }
    }

    @SuppressLint("NewApi")
    fun formatTimestamp(instant: Instant): String {
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val today = LocalDate.now()

        return when {
            dateTime.toLocalDate() == today -> {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            dateTime.toLocalDate() == today.minusDays(1) -> {
                "Yesterday"
            }
            dateTime.toLocalDate().year == today.year -> {
                dateTime.format(DateTimeFormatter.ofPattern("d MMM"))
            }
            else -> {
                dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            }
        }
    }
}