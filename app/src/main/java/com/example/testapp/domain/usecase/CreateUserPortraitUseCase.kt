package com.example.testapp.domain.usecase

import com.example.testapp.di.api.AIService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.domain.dto.user.UserPortrait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class CreateUserPortraitUseCase @Inject constructor(
    private val aiService: AIService,
    private val messageService: MessageApiService
) {
   suspend fun execute(chatId: String, userId: String): Result<UserPortrait> = withContext(Dispatchers.IO) {
       return@withContext try {
           val userMessages = messageService.getUserMessagesInChat(chatId, userId)

           val response = aiService.createUserPortrait(
               messages = userMessages,
               locale = Locale.getDefault()
           )

           Result.success(response)
       } catch (e: Exception) {
           Result.failure(e)
       }
   }
}