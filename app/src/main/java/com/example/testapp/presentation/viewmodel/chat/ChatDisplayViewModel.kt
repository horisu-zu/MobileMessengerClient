package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageStreamMode
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatDisplayData
import com.example.testapp.domain.models.chat.ChatType
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDisplayViewModel @Inject constructor(
    private val dataStoreUtil: DataStoreUtil,
    private val chatRepository: ChatApiService,
    private val messageRepository: MessageApiService,
    private val userRepository: UserApiService,
    private val messageWebSocketClient: MessageWebSocketClient
) : ViewModel() {

    private val _chatListItemsState =
        MutableStateFlow<Resource<Map<String, ChatDisplayData>>>(Resource.Loading())
    val chatListItemsState: StateFlow<Resource<Map<String, ChatDisplayData>>> = _chatListItemsState

    init {
        viewModelScope.launch {
            dataStoreUtil.getUserId().collect { userId ->
                userId?.let { startObservingChats(it) }
            }

            messageWebSocketClient.getMessageUpdates()
                .catch { e ->
                    Log.e("WebSocket", "Error in updates", e)
                }
                .collect { updates ->
                    processLastMessagesUpdates(updates)
                }
        }
    }

    private suspend fun processLastMessagesUpdates(updates: Map<String, MessageEvent>) {
        _chatListItemsState.update { currentState ->
            when (currentState) {
                is Resource.Success -> {
                    val updatedMap = currentState.data?.toMutableMap() ?: mutableMapOf()
                    updates.forEach { (chatId, event) ->
                        updatedMap[chatId] = when (event) {
                            is MessageEvent.MessageCreated -> {
                                val sender = userRepository.getUserById(event.message.senderId)
                                val currentData = updatedMap[chatId] ?: throw IllegalStateException(
                                    "Chat ${event.message.chatId} not initialized"
                                )

                                val isNewMessage =
                                    currentData.lastMessage?.createdAt?.let { lastTimestamp ->
                                        event.message.createdAt > lastTimestamp
                                    } ?: true

                                currentData.copy(
                                    lastMessage = event.message,
                                    senderName = sender.nickname,
                                    unreadCount = if (isNewMessage) currentData.unreadCount + 1 else currentData.unreadCount
                                )
                            }

                            is MessageEvent.MessageUpdated ->
                                updatedMap[chatId]?.copy(lastMessage = event.message)

                            is MessageEvent.MessageDeleted ->
                                updatedMap[chatId]?.copy(lastMessage = null)
                        } ?: return@forEach
                    }
                    Resource.Success(updatedMap)
                }

                else -> currentState
            }
        }
    }

    private fun startObservingChats(userId: String) {
        viewModelScope.launch {
            try {
                val chats = getUserChats(userId)
                val chatDisplayMap = mutableMapOf<String, ChatDisplayData>()

                chats.forEach { chat ->
                    chat.chatId?.let { chatId ->
                        try {
                            val displayData = when (chat.chatType) {
                                ChatType.PERSONAL -> loadPrivateChatData(chatId, userId)
                                ChatType.GROUP -> loadGroupChatData(chatId)
                                else -> throw IllegalStateException("Unknown chat type: ${chat.chatType}")
                            }
                            chatDisplayMap[chatId] = displayData
                        } catch (e: Exception) {
                            Log.e("ChatListItem", "Error loading chat $chatId", e)
                        }
                    }
                }
                _chatListItemsState.value = Resource.Success(chatDisplayMap)

                loadData(chats, userId)

                connectWebSocket(chats.mapNotNull { it.chatId })
            } catch (e: Exception) {
                _chatListItemsState.value = Resource.Error(e.message ?: "Error fetching chats")
            }
        }
    }

    private suspend fun getUserChats(userId: String): List<Chat> {
        return chatRepository.getUserChats(userId)
    }

    private suspend fun loadPrivateChatData(
        chatId: String,
        currentUserId: String
    ): ChatDisplayData {
        val participants = chatRepository.getChatParticipants(chatId)
        val otherUser = participants
            .first { it.userId != currentUserId }
            .let { userRepository.getUserById(it.userId) }

        return ChatDisplayData(
            chatId = chatId,
            name = otherUser.nickname,
            avatarUrl = otherUser.avatarUrl,
            chatTypeId = 1,
            isLoading = false
        )
    }

    private suspend fun loadGroupChatData(chatId: String): ChatDisplayData {
        val metadata = chatRepository.getChatMetadata(chatId)
        return ChatDisplayData(
            chatId = chatId,
            name = metadata.name,
            avatarUrl = metadata.avatar,
            chatTypeId = 2,
            isLoading = false
        )
    }

    private suspend fun loadData(chats: List<Chat>, userId: String) {
        val chatIds = chats.mapNotNull { it.chatId }

        try {
            val lastMessages = messageRepository.getLastMessages(chatIds)

            val messagesMap = lastMessages.associateBy { it.chatId }
            val userIds = lastMessages.map { it.senderId }.distinct()
            val users = userRepository.getByIds(userIds)
            val usersMap = users.associateBy { it.userId }

            Log.d("ChatDisplayViewModel", "Fetching unread counts for chats: $chatIds")
            val unreadCounts = chatIds.associateWith { chatId ->
                val count = messageRepository.getUnreadMessagesCount(chatId, userId)
                Log.d("ChatDisplayViewModel", "Unread count for chat $chatId: $count")
                count
            }

            _chatListItemsState.update { currentResource ->
                when (currentResource) {
                    is Resource.Success -> {
                        val updatedMap = currentResource.data?.mapValues { (chatId, displayData) ->
                            val lastMessage = messagesMap[chatId]
                            val sender = lastMessage?.senderId?.let { usersMap[it] }

                            if (lastMessage == null) {
                                displayData
                            } else {
                                displayData.copy(
                                    lastMessage = lastMessage,
                                    senderName = sender?.nickname ?: "Placeholder",
                                    unreadCount = unreadCounts[chatId] ?: 0
                                )
                            }
                        } ?: currentResource.data

                        Resource.Success(updatedMap)
                    }

                    else -> {
                        currentResource
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("ChatDisplayViewModel", "Error in loadData", e)
        }
    }

    private fun connectWebSocket(chatIds: List<String>) {
        Log.d("ChatDisplayViewModel", "ConnectWebSocket called with chatIds: $chatIds")

        messageWebSocketClient.disconnect()

        Log.d(
            "ChatDisplayViewModel",
            "Connecting WebSocket with chatIds: $chatIds and mode: ${MessageStreamMode.LATEST_ONLY}"
        )
        messageWebSocketClient.connect(chatIds, MessageStreamMode.LATEST_ONLY)
    }
}