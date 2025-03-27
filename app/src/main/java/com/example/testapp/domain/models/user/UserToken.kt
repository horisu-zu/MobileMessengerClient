package com.example.testapp.domain.models.user

import java.time.Instant

data class UserToken(
    val tokenId: String? = null,
    val userId: String,
    val token: String,
    val createdAt: Instant
)
