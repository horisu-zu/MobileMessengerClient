package com.example.testapp.domain.dto.user

data class UserRequest(
    val email: String,
    val password_hash: String,
    val nickname: String,
    val first_name: String,
    val last_name: String?,
    val avatar_url: String?,
    val user_color: String?,
    val description: String?
)