package com.example.testapp.domain.repository

import android.util.Log
import com.example.testapp.di.api.AIService
import com.example.testapp.domain.models.message.Message
import com.google.ai.client.generativeai.GenerativeModel
import java.util.Locale
import javax.inject.Inject

class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel
): AIService {
    override suspend fun summarizeMessages(messages: List<Pair<String, Message>>, locale: Locale): String {
        return try {
            val messageText = messages
                .sortedBy { it.second.createdAt }
                .joinToString("\n") {
                    "Message from ${it.first}: ${it.second.message}"
                }

            Log.d("GeminiService", "Message Text: $messageText")

            val prompt = """
                You are an assistant that creates message summaries.
                Analyze the following messages and create a brief summary,
                highlighting the main topics and important points.
                Return the response in ${locale.displayLanguage} language.
                Messages:
                $messageText
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: throw Exception("Empty Response from API")
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }
}