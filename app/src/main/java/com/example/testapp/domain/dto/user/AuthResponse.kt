package com.example.testapp.domain.dto.user

data class AuthResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String
)
