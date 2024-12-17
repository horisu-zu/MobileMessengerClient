package com.example.testapp.domain.dto.message

data class MessageRequest(
    val chatId: String,
    val senderId: String,
    val message: String?,
    val replyTo: String? = null
)

