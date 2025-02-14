package com.example.testapp.di.api

import com.example.testapp.domain.models.message.Message
import java.util.Locale

interface AIService {
    suspend fun summarizeMessages(messages: List<Message>, locale: Locale): String
}