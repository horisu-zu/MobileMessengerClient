package com.example.testapp.domain.dto.message

import com.example.testapp.domain.models.message.Message

data class MessageDateGroup(
    val date: String,
    val messages: List<List<Message>>
)
