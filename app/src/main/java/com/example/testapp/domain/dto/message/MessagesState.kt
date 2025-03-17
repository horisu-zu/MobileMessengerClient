package com.example.testapp.domain.dto.message

import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message

data class MessagesState(
    val messages: List<Message> = emptyList(),
    val attachments: Map<String, List<Attachment>> = emptyMap(),
    val replyMessages: Map<String, Message> = emptyMap(),
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
