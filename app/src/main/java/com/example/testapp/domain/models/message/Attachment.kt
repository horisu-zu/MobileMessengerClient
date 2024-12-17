package com.example.testapp.domain.models.message

data class Attachment(
    val attachmentId: String? = null,
    val messageId: String,
    val name: String,
    val url: String,
    val fileType: String,
    val fileSize: Double
)
