package com.example.testapp.domain.usecase

import android.content.Context
import android.util.Log
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.NotificationApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.message.MessageInputState
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.dto.notification.NotificationRequest
import com.example.testapp.domain.models.message.Message
import com.example.testapp.utils.storage.ChatMediaService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageService: MessageApiService,
    private val chatMediaService: ChatMediaService,
    private val notificationService: NotificationApiService,
    private val chatService: ChatApiService,
    private val userService: UserApiService
) {
    suspend fun execute(
        state: MessageInputState,
        context: Context?
    ): Result<Message> = withContext(Dispatchers.IO) {
        if (state.message.isNullOrBlank() && state.localAttachments.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No message or attachments"))
        }

        return@withContext try {
            when {
                state.isEditing -> {
                    val updatedMessage = state.editedMessageId?.let { editedMessage ->
                        messageService.updateMessage(
                            editedMessage,
                            MessageUpdateRequest(message = state.message)
                        )
                    }
                    Result.success(updatedMessage!!)
                }
                else -> {
                    val attachmentUrls = state.localAttachments.map { attachment ->
                        async {
                            chatMediaService.uploadChatFile(
                                chatId = state.chatId,
                                fileUri = attachment.uri,
                                context = context
                            ).onFailure { e ->
                                Log.e("ChatMediaService", "Failed to upload file: ${e.message}")
                            }
                        }
                    }.awaitAll().mapNotNull { it.getOrNull() }

                    val createdMessage = messageService.createMessage(
                        MessageRequest(
                            chatId = state.chatId,
                            senderId = state.senderId,
                            message = state.message,
                            replyTo = state.replyToMessage?.messageId
                        ), attachmentUrls = attachmentUrls
                    )

                    sendNotifications(state, createdMessage)

                    Result.success(createdMessage)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sendNotifications(
        state: MessageInputState,
        createdMessage: Message
    ) {
        val tokens = notificationService.getTokensByChatId(state.chatId)
            .filter { it.userId != state.senderId }
            .map { it.token }

        val notificationTitle = try {
            chatService.getChatMetadata(state.chatId).name
        } catch (e: Exception) {
            userService.getUserById(state.senderId).nickname
        }
        val avatarUrl = userService.getUserById(state.senderId).avatarUrl

        if (tokens.isNotEmpty()) {
            notificationService.sendMulticastNotification(
                NotificationRequest(
                    tokens = tokens,
                    title = notificationTitle,
                    body = state.message ?: "Attachment",
                    data = mapOf(
                        "chatId" to state.chatId,
                        "messageId" to (createdMessage.messageId!!),
                        "avatarUrl" to avatarUrl
                    )
                )
            )
        }
    }
}