package com.example.testapp.domain.usecase

import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.models.chat.ChatDisplayData
import javax.inject.Inject

class ProcessMessageUpdatesUseCase @Inject constructor(
    private val userService: UserApiService,
    private val messageService: MessageApiService
) {
    suspend fun execute(
        currentUserId: String,
        currentChats: Map<String, ChatDisplayData>,
        updates: Map<String, MessageEvent>
    ): Map<String, ChatDisplayData> {
        val updatedMap = currentChats.toMutableMap()

        updates.forEach { (chatId, event) ->
            updatedMap[chatId] = processEventForChat(currentUserId, updatedMap[chatId], event)
        }

        return updatedMap
    }

    private suspend fun processEventForChat(
        currentUserId: String,
        currentData: ChatDisplayData?,
        event: MessageEvent
    ): ChatDisplayData = when (event) {
        is MessageEvent.MessageCreated -> {
            val sender = userService.getUserById(event.message.senderId)
            val isNewMessage = currentData?.lastMessage?.createdAt?.let {
                event.message.createdAt > it
            } ?: true
            val isCurrentUser = currentUserId == event.message.senderId

            currentData?.copy(
                lastMessage = event.message,
                senderName = sender.nickname,
                unreadCount = if (isNewMessage && !isCurrentUser)
                    (currentData.unreadCount + 1)
                else currentData.unreadCount
            ) ?: throw IllegalStateException("Chat not found")
        }
        is MessageEvent.MessageUpdated -> {
            val updatedMessage = currentData?.lastMessage?.let {
                if (it.messageId == event.message.messageId) event.message else it
            } ?: throw IllegalStateException("Chat not found")

            currentData.copy(lastMessage = updatedMessage)
        }
        is MessageEvent.MessageDeleted -> {
            val lastMessage = messageService.getLastMessages(listOf(event.message.chatId)).first()
            val senderNickname = userService.getUserById(lastMessage.senderId).nickname
            currentData?.copy(
                lastMessage = lastMessage,
                senderName = senderNickname,
                unreadCount = if(currentData.unreadCount > 0) currentData.unreadCount - 1 else 0
            ) ?: throw IllegalStateException("Chat not found")
        }
    }
}