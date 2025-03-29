package com.example.testapp.domain.usecase

import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.models.chat.ChatDisplayData
import com.example.testapp.domain.models.chat.ChatType
import javax.inject.Inject

class GetUserChatsUseCase @Inject constructor(
    private val chatService: ChatApiService,
    private val messageService: MessageApiService,
    private val userService: UserApiService
) {
    suspend fun execute(userId: String): List<ChatDisplayData> {
        val chats = chatService.getUserChats(userId)

        return chats.mapNotNull { chat ->
            chat.chatId?.let { chatId ->
                val lastMessage = messageService.getLastMessages(listOf(chatId)).firstOrNull()
                val unreadCount = messageService.getUnreadMessagesCount(chatId, userId)

                val displayData = when(chat.chatType) {
                    ChatType.GROUP -> getGroupChatData(chatId)
                    ChatType.PERSONAL -> getPersonalChatData(chatId, userId)
                    else -> null
                }

                displayData?.copy(
                    lastMessage = lastMessage,
                    unreadCount = unreadCount
                )
            }
        }
    }

    private suspend fun getGroupChatData(
        chatId: String
    ): ChatDisplayData {
        val metadata = chatService.getChatMetadata(chatId)
        return ChatDisplayData(
            chatId = chatId,
            name = metadata.name,
            avatarUrl = metadata.avatar,
            chatTypeId = 2
        )
    }

    private suspend fun getPersonalChatData(
        chatId: String,
        currentUserId: String
    ): ChatDisplayData {
        val participants = chatService.getChatParticipants(chatId)
        val otherUser = participants
            .first { it.userId != currentUserId }
            .let { userService.getUserById(it.userId) }

        return ChatDisplayData(
            chatId = chatId,
            name = otherUser.nickname,
            avatarUrl = otherUser.avatarUrl,
            chatTypeId = 1
        )
    }
}