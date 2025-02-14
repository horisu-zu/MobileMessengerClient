package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.models.chat.ChatDisplayData
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
    private val chatRepository: ChatApiService,
    private val messageRepository: MessageApiService,
    private val userRepository: UserApiService,
    private val messageWebSocketClient: MessageWebSocketClient
): ViewModel() {

    private val _chatListItemsState = MutableStateFlow<Resource<Map<String, ChatDisplayData>>>(Resource.Loading())
    val chatListItemsState: StateFlow<Resource<Map<String, ChatDisplayData>>> = _chatListItemsState

    private var currentItems = mutableMapOf<String, ChatDisplayData>()

    init {
        viewModelScope.launch {
            messageWebSocketClient.getMessageUpdates()
                .catch { e ->
                    Log.e("ChatListItem", "Error in message updates", e)
                }
                .collect { updates ->
                    processMessageUpdates(updates)
                }
        }
    }

    fun startObservingChats(userId: String) {
        viewModelScope.launch {
            try {
                val chats = getUserChats(userId)
                val chatDisplayMap = mutableMapOf<String, ChatDisplayData>()

                chats.forEach { chat ->
                    chat.chatId?.let { chatId ->
                        try {
                            val displayData = when (chat.chatTypeId) {
                                1 -> loadPrivateChatData(chatId, userId)
                                2 -> loadGroupChatData(chatId)
                                else -> throw IllegalStateException("Unknown chat type: ${chat.chatTypeId}")
                            }
                            chatDisplayMap[chatId] = displayData
                        } catch (e: Exception) {
                            Log.e("ChatListItem", "Error loading chat $chatId", e)
                        }
                    }
                }

                _chatListItemsState.value = Resource.Success(chatDisplayMap)

                loadLastMessages(chats.mapNotNull { it.chatId })
            } catch (e: Exception) {
                _chatListItemsState.value = Resource.Error(e.message ?: "Error fetching chats")
            }
        }
    }

    private suspend fun getUserChats(userId: String): List<Chat> {
        return chatRepository.getUserChats(userId)
    }

    private suspend fun loadPrivateChatData(chatId: String, currentUserId: String): ChatDisplayData {
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

    private suspend fun loadLastMessages(chatIds: List<String>) {
        try {
            val lastMessages = messageRepository.getLastMessages(chatIds)
            val messagesMap = lastMessages.associateBy { it.chatId }

            _chatListItemsState.update { currentResource ->
                when (currentResource) {
                    is Resource.Success -> {
                        val updatedMap = currentResource.data?.mapValues { (chatId, displayData) ->
                            displayData.copy(lastMessage = messagesMap[chatId])
                        }
                        Resource.Success(updatedMap)
                    }
                    else -> currentResource
                }
            }
        } catch (e: Exception) {
            Log.e("ChatListItem", "Error loading last messages", e)
        }
    }

    private fun processMessageUpdates(updates: Map<String, MessageEvent>) {
        updates.forEach { (chatId, event) ->
            when (event) {
                is MessageEvent.MessageDeleted -> {
                    currentItems = currentItems.toMutableMap().apply {
                        this[chatId] = this[chatId]?.copy(lastMessage = null) ?: return@apply
                    }
                }
                is MessageEvent.MessageCreated, is MessageEvent.MessageUpdated -> {
                    val message = when (event) {
                        is MessageEvent.MessageCreated -> event.message
                        is MessageEvent.MessageUpdated -> event.message
                        else -> return
                    }
                    currentItems = currentItems.toMutableMap().apply {
                        this[chatId] = this[chatId]?.copy(lastMessage = message) ?: return@apply
                    }
                }
            }
            _chatListItemsState.value = Resource.Success(currentItems)
        }
    }
}