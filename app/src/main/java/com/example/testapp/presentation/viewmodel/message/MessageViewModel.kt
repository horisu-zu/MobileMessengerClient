package com.example.testapp.presentation.viewmodel.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageStreamMode
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.models.message.Message
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MessageViewModel(
    private val messageRepository: MessageApiService,
    private val dataStoreUtil: DataStoreUtil,
    private val messageWebSocketClient: MessageWebSocketClient
): ViewModel() {
    private val _lastMessagesState = MutableStateFlow<Resource<Map<String, Message>>>(Resource.Loading())
    val lastMessagesState: StateFlow<Resource<Map<String, Message>>> = _lastMessagesState

    private val _chatMessagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Loading())
    val chatMessagesState: StateFlow<Resource<List<Message>>> = _chatMessagesState

    private var currentChatId: String? = null
    private var currentMode: MessageStreamMode? = null

    private var currentLastMessages: Map<String, Message> = emptyMap()
    private var currentChatMessages: List<Message> = emptyList()

    init {
        viewModelScope.launch {
            try {
                messageWebSocketClient.getMessageUpdates()
                    .catch { e ->
                        Log.e("WebSocket", "Error receiving status updates", e)
                    }
                    .collect { updates ->
                        processWebSocketUpdates(updates)
                    }
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Uncaught error in message subscription", e)
            }
        }
    }

    private fun processWebSocketUpdates(updates: Map<String, MessageEvent>) {
        Log.d("MessageViewModel", "Event: $updates")
        updates.forEach { (chatId, event) ->
            when (currentMode) {
                MessageStreamMode.FULL_CHAT -> {
                    currentChatId?.let {
                        currentChatMessages = currentChatMessages.toMutableList().apply {
                            when (event) {
                                is MessageEvent.MessageCreated -> add(event.message)
                                is MessageEvent.MessageUpdated -> add(event.message)
                                is MessageEvent.MessageDeleted -> {
                                    removeAll { it.messageId == event.message.messageId }
                                }
                            }
                        }
                        _chatMessagesState.value = Resource.Success(currentChatMessages)
                    }
                }
                MessageStreamMode.LATEST_ONLY -> {
                    when (event) {
                        is MessageEvent.MessageDeleted -> currentLastMessages = currentLastMessages.toMutableMap().apply {
                            remove(chatId)
                        }
                        is MessageEvent.MessageCreated -> currentLastMessages = currentLastMessages.toMutableMap().apply {
                            this[chatId] = event.message
                        }
                        is MessageEvent.MessageUpdated -> currentLastMessages = currentLastMessages.toMutableMap().apply {
                            this[chatId] = event.message
                        }
                    }
                    _lastMessagesState.value = Resource.Success(currentLastMessages)
                }
                null -> {}
            }
        }
    }

    fun getLastMessages(chatIds: List<String>) {
        viewModelScope.launch {
            _lastMessagesState.value = Resource.Loading()
            try {
                val response = messageRepository.getLastMessages(chatIds)
                Log.d("MessageViewModel", "Last messages response: $response")

                val mappedResponse = response.associateBy { chat -> chat.chatId }
                currentLastMessages = mappedResponse
                _lastMessagesState.value = Resource.Success(mappedResponse)

                Log.d("MessageViewModel", "Calling connectWebSocket for LATEST_ONLY")
                connectWebSocket(chatIds, MessageStreamMode.LATEST_ONLY)
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Error in getLastMessages", e)
                _lastMessagesState.value = Resource.Error(e.message ?: "Error loading last messages")
            }
        }
    }

    fun getMessagesForChat(chatId: String) {
        viewModelScope.launch {
            _chatMessagesState.value = Resource.Loading()
            try {
                val response = messageRepository.getMessagesForChat(chatId)
                currentChatMessages = response
                _chatMessagesState.value = Resource.Success(response)

                connectWebSocket(listOf(chatId), MessageStreamMode.FULL_CHAT)
            } catch (e: Exception) {
                _chatMessagesState.value = Resource.Error(e.message ?: "Error loading messages for chat")
            }
        }
    }

    suspend fun sendMessage(messageRequest: MessageRequest) {
        messageRepository.createMessage(messageRequest)
    }

    suspend fun updateMessage(messageId: String, messageUpdateRequest: MessageUpdateRequest) {
        messageRepository.updateMessage(messageId, messageUpdateRequest)
    }

    suspend fun deleteMessage(messageId: String) {
        messageRepository.deleteMessage(messageId)
    }

    private fun connectWebSocket(chatIds: List<String>, mode: MessageStreamMode) {
        Log.d("MessageViewModel", "ConnectWebSocket called with mode: $mode, chatIds: $chatIds")

        if (currentMode != mode) {
            Log.d("MessageViewModel", "Disconnecting existing WebSocket")
            messageWebSocketClient.disconnect()
        }

        currentMode = mode
        if (mode == MessageStreamMode.FULL_CHAT) {
            currentChatId = chatIds.firstOrNull()
        }

        Log.d("MessageViewModel", "Connecting WebSocket with chatIds: $chatIds and mode: $mode")
        messageWebSocketClient.connect(chatIds, mode)
    }

    override fun onCleared() {
        super.onCleared()
        messageWebSocketClient.disconnect()
    }
}