package com.example.testapp.domain.usecase

import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.ChatRestrictionRequest
import com.example.testapp.domain.models.chat.ChatRestriction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateChatRestrictionUseCase @Inject constructor(
    private val chatRepository: ChatApiService
) {
    suspend fun execute(
        chatId: String,
        chatRestrictionRequest: ChatRestrictionRequest
    ): Result<ChatRestriction> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = chatRepository.createRestriction(chatId, chatRestrictionRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}