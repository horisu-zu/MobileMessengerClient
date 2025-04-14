package com.example.testapp.domain.dto.chat

data class ChatJoinRequest(
    val userId: String,
    val inviteCode: String? = null
)
