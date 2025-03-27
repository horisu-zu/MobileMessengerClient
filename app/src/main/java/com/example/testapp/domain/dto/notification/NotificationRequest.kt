package com.example.testapp.domain.dto.notification

data class NotificationRequest(
    val tokens: List<String>,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)
