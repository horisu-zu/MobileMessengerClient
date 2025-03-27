package com.example.testapp.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.domain.dto.message.MessageDateGroup
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.templates.Avatar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        //Log.d("ReactionsList", "Reactions: $reactions")
        return reactions
            .sortedBy { it.createdAt }
            .groupBy { it.emojiReaction }
    }

    fun groupMessages(messages: List<Message>): List<MessageDateGroup> {
        val groupedByDate = messages
            .groupBy { formatDate(it.createdAt) }
            .toSortedMap(compareByDescending { parseDate(it) })

        return groupedByDate.map { (date, messages) ->
            val sortedMessages = messages.sortedByDescending { it.createdAt }
            val senderGroups = mutableListOf<List<Message>>()
            var currentGroup = mutableListOf<Message>()

            for (message in sortedMessages) {
                if (currentGroup.isEmpty() || currentGroup.last().senderId == message.senderId) {
                    currentGroup.add(message)
                } else {
                    senderGroups.add(currentGroup)
                    currentGroup = mutableListOf(message)
                }
            }
            if (currentGroup.isNotEmpty()) {
                senderGroups.add(currentGroup)
            }
            MessageDateGroup(date, senderGroups)
        }
    }

    private fun parseDate(formattedDate: String): Date {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return try {
            formatter.parse(formattedDate)
        } catch (e: ParseException) {
            formatter.parse("$formattedDate $currentYear")
        } ?: throw IllegalArgumentException("Unable to parse date: $formattedDate")
    }

    private fun formatDate(instant: Instant): String {
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val currentYear = Year.now().value

        return if (dateTime.year == currentYear) {
            dateTime.format(DateTimeFormatter.ofPattern("d MMMM"))
        } else {
            dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
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

    fun formatAudioProgress(currentPositionMs: Long, durationMs: Long): String {
        val currentSeconds = currentPositionMs / 1000
        val totalSeconds = durationMs / 1000

        val currentMinutes = currentSeconds / 60
        val currentSecondsPart = currentSeconds % 60

        val totalMinutes = totalSeconds / 60
        val totalSecondsPart = totalSeconds % 60

        return "${currentMinutes}:${currentSecondsPart.toString().padStart(2, '0')} " +
                "/ ${totalMinutes}:${totalSecondsPart.toString().padStart(2, '0')}"
    }

    fun formatFileSize(size: Double): String {
        return when {
            size < 1024 -> "%.0f B".format(size)
            size < 1024 * 1024 -> "%.2f KB".format(size / 1024)
            size < 1024 * 1024 * 1024 -> "%.2f MB".format(size / (1024 * 1024))
            else -> "%.2f GB".format(size / (1024 * 1024 * 1024))
        }
    }

    fun formatDuration(durationMs: Long): String {
        val durationSeconds = durationMs / 1000

        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60

        return "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}