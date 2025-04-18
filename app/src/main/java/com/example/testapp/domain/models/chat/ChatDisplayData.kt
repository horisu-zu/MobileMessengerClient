package com.example.testapp.domain.models.chat

import com.example.testapp.domain.models.message.Message

data class ChatDisplayData(
    val chatId: String,
    val chatTypeId: Int,
    val name: String,
    val senderName: String = "...",
    val avatarUrl: String?,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)
