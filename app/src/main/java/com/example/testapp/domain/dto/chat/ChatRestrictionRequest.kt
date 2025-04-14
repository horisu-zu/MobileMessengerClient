package com.example.testapp.domain.dto.chat

import com.example.testapp.domain.models.chat.RestrictionType

data class ChatRestrictionRequest(
    val userId: String,
    val type: RestrictionType,
    val duration: String,
    val reason: String? = null,
    val createdBy: String
)
