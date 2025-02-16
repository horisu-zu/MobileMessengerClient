package com.example.testapp.domain.dto.message

import com.example.testapp.domain.models.message.Message

data class MessageInputState(
    val chatId: String,
    val senderId: String,
    val message: String? = null,
    val isEditing: Boolean = false,
    val editedMessageId: String? = null,
    val editingMessage: Message? = null,
    val isReplying: Boolean = false,
    val replyToMessage: Message? = null
)
