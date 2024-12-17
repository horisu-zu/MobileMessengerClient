package com.example.testapp.domain.models.chat


data class ChatType(
    val id: Int,
    val name: String,
    val description: String?,
    val isRestricted: Boolean
)
