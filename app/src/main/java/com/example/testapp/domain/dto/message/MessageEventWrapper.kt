package com.example.testapp.domain.dto.message

import com.example.testapp.domain.models.message.Message

data class MessageEventWrapper(
    val type: String,
    val message: Message
)
