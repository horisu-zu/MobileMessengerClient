package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.models.chat.ChatDisplayData
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageStreamMode
import com.example.testapp.domain.usecase.GetUserChatsUseCase
import com.example.testapp.domain.usecase.ProcessMessageUpdatesUseCase
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDisplayViewModel @Inject constructor(
    private val dataStoreUtil: DataStoreUtil,
    private val messageWebSocketClient: MessageWebSocketClient,
    private val getUserChatsUseCase: GetUserChatsUseCase,
    private val processMessageUpdatesUseCase: ProcessMessageUpdatesUseCase
) : ViewModel() {

    private val _chatListItemsState =
        MutableStateFlow<Resource<Map<String, ChatDisplayData>>>(Resource.Loading())
    val chatListItemsState: StateFlow<Resource<Map<String, ChatDisplayData>>> = _chatListItemsState

    init {
        viewModelScope.launch {
            try {
                val userId = dataStoreUtil.getUserId().first() ?: return@launch
                loadChats(userId)

                launch {
                    messageWebSocketClient.getMessageUpdates()
                        .catch { e ->
                            Log.e("WebSocket", "Error in updates", e)
                        }
                        .collect { updates ->
                            processLastMessagesUpdates(updates)
                        }
                }
            } catch (e: Exception) {
                Log.e("ChatDisplayViewModel", "Initialization error", e)
            }
        }
    }

    private fun loadChats(userId: String) {
        viewModelScope.launch {
            try {
                val chats = getUserChatsUseCase.execute(userId)
                _chatListItemsState.value = Resource.Success(chats.associateBy { it.chatId })

                connectWebSocket(chats.map { it.chatId })
            } catch (e: Exception) {
                _chatListItemsState.value = Resource.Error(e.message ?: "Error loading chats")
            }
        }
    }

    private fun processLastMessagesUpdates(updates: Map<String, MessageEvent>) {
        viewModelScope.launch {
            try {
                val currentChats = _chatListItemsState.value.data ?: return@launch
                val userId = dataStoreUtil.getUserId().first() ?: return@launch
                val updatedChats = processMessageUpdatesUseCase.execute(userId, currentChats, updates)
                _chatListItemsState.update { Resource.Success(updatedChats) }
            } catch (e: Exception) {
                Log.e("ChatDisplayViewModel", "Error processing updates", e)
            }
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