package com.example.testapp.domain.usecase

import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.ChatRestrictionUpdateRequest
import com.example.testapp.domain.models.chat.ChatRestriction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

class UpdateChatRestrictionUseCase @Inject constructor(
    private val chatRepository: ChatApiService
) {
    suspend fun execute(
        restrictionId: String,
        newDuration: Duration
    ): Result<ChatRestriction> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = chatRepository.updateRestriction(
                restrictionId,
                ChatRestrictionUpdateRequest(
                    newDuration.toString()
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}