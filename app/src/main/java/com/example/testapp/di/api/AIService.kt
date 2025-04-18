package com.example.testapp.di.api

import com.example.testapp.domain.dto.user.UserPortrait
import com.example.testapp.domain.models.message.Message
import java.util.Locale

interface AIService {
    suspend fun summarizeMessages(messages: List<Pair<String, Message>>, locale: Locale): String

    suspend fun translateMessage(messageText: String, locale: Locale): String

    suspend fun createUserPortrait(messages: List<Message>, locale: Locale): UserPortrait
}