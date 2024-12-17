package com.example.testapp.domain.models.user

import java.time.Instant

data class User(
    val userId: String? = null,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String,
    val firstName: String,
    val lastName: String?,
    val nickname: String,
    val createdAt: Instant = Instant.now(),
    val userColor: String,
    val description: String?
)
