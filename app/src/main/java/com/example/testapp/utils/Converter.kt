package com.example.testapp.utils

import com.example.testapp.domain.models.message.Attachment

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
}