package com.example.testapp.domain.repository

import android.util.Log
import com.example.testapp.di.api.AIService
import com.example.testapp.domain.dto.user.UserPortrait
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

    override suspend fun translateMessage(messageText: String, locale: Locale): String {
        return try {
            Log.d("GeminiService", "Message Text: $messageText")

            val prompt = """
                You are assistant that translates messages.
                Analyze the following message and translate it in ${locale.displayLanguage} language.
                Response should consist only of the translated text.
                Message:
                $messageText
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: throw Exception("Empty Response from API")
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }

    override suspend fun createUserPortrait(messages: List<Message>, locale: Locale): UserPortrait {
        return try {
            val prompt = """
                You are assistant that helps to create user portraits.
                Analyze the following messages sended by one user and create his portrait.
                Structure of this portrait must be like this:
                2-5 characteristics and percentage that user have of it, short summary of user's portrait after it.
                Response about characteristics should be in the following format: {emoji} {characteristic} — {percentage}%.
                Return the response in ${locale.displayLanguage} language.
                User Messages: $messages
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text ?: throw Exception("Empty Response from API")

            UserPortrait.parseUserPortrait(response.text!!)
        } catch (e: Exception) {
            throw Exception("Error: ${e.message}")
        }
    }
}