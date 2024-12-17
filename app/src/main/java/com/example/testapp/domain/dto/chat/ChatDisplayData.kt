package com.example.testapp.domain.dto.chat

data class ChatDisplayData(
    val chatId: String,
    val name: String,
    val avatarUrl: String?,
    val isLoading: Boolean
)
