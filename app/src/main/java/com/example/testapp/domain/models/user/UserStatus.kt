package com.example.testapp.domain.models.user

import java.time.Instant

data class UserStatus(
    val userId: String,
    val onlineStatus: Boolean,
    val lastSeen: Instant = Instant.now()
)
