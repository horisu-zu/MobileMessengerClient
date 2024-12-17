package com.example.testapp.domain.dto.message

data class MessageStatusUpdateRequest(
    val isRead: Boolean? = null,
    val isPinned: Boolean? = null
)


