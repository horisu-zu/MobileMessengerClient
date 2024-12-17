package com.example.testapp.domain.dto.user

import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class UserResponse(
    val userId: String,
    val email: String,
    val avatarUrl: String,
    val nickname: String,
    val firstName: String,
    val lastName: String?,
    val createdAt: Instant,
    val userColor: String,
    val description: String?
)