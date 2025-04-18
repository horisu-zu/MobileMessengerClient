package com.example.testapp.presentation.viewmodel.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.AIService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.domain.dto.message.MessageEvent
import com.example.testapp.domain.dto.message.MessageStreamMode
import com.example.testapp.domain.dto.message.MessagesState
import com.example.testapp.domain.models.message.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageApiService,
    private val messageWebSocketClient: MessageWebSocketClient,
    private val aiService: AIService
) : ViewModel() {
    private val _chatMessagesState = MutableStateFlow<MessagesState>(MessagesState())
    val chatMessagesState: StateFlow<MessagesState> = _chatMessagesState

    private var currentChatId: String? = null
    private var currentMode: MessageStreamMode? = null

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
            currentChatId?.let {
                val updatedMessages = _chatMessagesState.value.messages.toMutableList().apply {
                    when (event) {
                        is MessageEvent.MessageCreated -> {
                            if (none { it.messageId == event.message.messageId }) {
                                add(event.message)
                                loadReplyMessage(event.message)
                                loadAttachments(event.message.messageId ?: "")
                            }
                        }
                        is MessageEvent.MessageUpdated -> {
                            val index = indexOfFirst { it.messageId == event.message.messageId }
                            this[index] = event.message
                        }
                        is MessageEvent.MessageDeleted -> {
                            removeAll { it.messageId == event.message.messageId }
                        }
                    }
                }
                _chatMessagesState.value = _chatMessagesState.value.copy(
                    messages = updatedMessages
                )
            }
        }
    }

    fun getMessagesForChat(chatId: String, userId: String) {
        viewModelScope.launch {
            if (_chatMessagesState.value.isLoading || !_chatMessagesState.value.hasMorePages) return@launch

            if (currentChatId != chatId) {
                currentChatId = chatId
                _chatMessagesState.value = MessagesState(isLoading = true)
            } else {
                _chatMessagesState.value = _chatMessagesState.value.copy(isLoading = true)
            }

            try {
                Log.d("MessageViewModel", "Loading messages for page: ${_chatMessagesState.value.currentPage}")
                val response = messageRepository.getMessagesForChat(
                    chatId = chatId,
                    page = _chatMessagesState.value.currentPage,
                    size = 30
                )

                val messageReadResponse = messageRepository.getReadMessageInChat(chatId, userId)

                val attachmentsResponse = messageRepository.getAttachmentsForChat(
                    chatId = chatId,
                    page = _chatMessagesState.value.currentPage,
                    size = 30
                ).groupBy { it.messageId }

                Log.d("MessageViewModel", "Loaded ${response.size} messages for page: ${_chatMessagesState.value.currentPage}")

                val messages = if (_chatMessagesState.value.currentPage == 0) {
                    response
                } else {
                    _chatMessagesState.value.messages + response
                }.distinctBy { it.messageId }

                val lastReadMessage = messageReadResponse.lastReadMessageId.let { id ->
                    messages.firstOrNull { it.messageId == id }
                }

                val updatedMessages = messages.map { message ->
                    val isRead = lastReadMessage?.let {
                        message.senderId == userId && message.createdAt <= it.createdAt
                    } ?: false

                    message.copy(isRead = isRead)
                }

                val replyMessageIds = messages
                    .mapNotNull { it.replyTo }
                    .filter { it !in _chatMessagesState.value.replyMessages.keys }

                val replyMessages = if (replyMessageIds.isNotEmpty()) {
                    messageRepository.getMessagesByIds(replyMessageIds)
                        .associateBy { it.messageId ?: "" }
                } else {
                    emptyMap()
                }

                _chatMessagesState.value = _chatMessagesState.value.copy(
                    messages = updatedMessages,
                    replyMessages = _chatMessagesState.value.replyMessages + replyMessages,
                    attachments = _chatMessagesState.value.attachments + attachmentsResponse,
                    currentPage = _chatMessagesState.value.currentPage + 1,
                    hasMorePages = response.isNotEmpty(),
                    isLoading = false
                )
                Log.d("MessageViewModel", "Loaded ${response.size} messages for page: ${_chatMessagesState.value.currentPage}")

                if (_chatMessagesState.value.currentPage == 1) {
                    connectWebSocket(listOf(chatId), MessageStreamMode.FULL_CHAT)
                }
            } catch (e: Exception) {
                _chatMessagesState.value = _chatMessagesState.value.copy(
                    error = e.message ?: "Error loading messages for chat",
                    isLoading = false
                )
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
        }
    }

    fun markMessageAsRead(chatId: String, messageId: String, userId: String) {
        viewModelScope.launch {
            messageRepository.markMessagesAsRead(chatId, messageId, userId)
        }
    }

    fun translateMessage(messageId: String, locale: Locale) {
        viewModelScope.launch {
            try {
                val messageIndex = _chatMessagesState.value.messages
                    .indexOfFirst { it.messageId == messageId }
                val message = _chatMessagesState.value.messages[messageIndex]

                val translatedMessage = aiService.translateMessage(message.message ?: "", locale).trim()
                Log.d("MessageViewModel", "Translated Message: $translatedMessage")
                val updatedMessages = _chatMessagesState.value.messages.toMutableList()
                updatedMessages[messageIndex] = message.copy(
                    message = translatedMessage,
                    isTranslated = true
                )
                _chatMessagesState.value = _chatMessagesState.value.copy(
                    messages = updatedMessages
                )
            } catch (e: Exception) {
                Log.d("MessageViewModel", "Error translating message: ${e.message}")
            }
        }
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

    private fun loadReplyMessage(message: Message) {
        message.replyTo?.let { replyId ->
            if (replyId !in _chatMessagesState.value.replyMessages) {
                viewModelScope.launch {
                    try {
                        val replyMessage = messageRepository.getMessagesByIds(listOf(replyId))
                            .firstOrNull()
                            ?.let { replyId to it }

                        replyMessage?.let {
                            _chatMessagesState.value = _chatMessagesState.value.copy(
                                replyMessages = _chatMessagesState.value.replyMessages + it
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("MessageViewModel", "Error loading reply message", e)
                    }
                }
            }
        }
    }

    private fun loadAttachments(messageId: String) {
        if (messageId !in _chatMessagesState.value.attachments) {
            viewModelScope.launch {
                try {
                    Log.d("MessageViewModel", "Loading attachments for message: $messageId")
                    val attachments = messageRepository.getAttachmentsForMessage(messageId)
                        .groupBy { it.messageId }

                    Log.d("MessageViewModel", "Loaded attachments for message: $attachments")
                    _chatMessagesState.value = _chatMessagesState.value.copy(
                        attachments = (_chatMessagesState.value.attachments + attachments).toMap()
                    )
                } catch (e: Exception) {
                    Log.e("MessageViewModel", "Error loading attachments", e)
                }
            }
        }
    }

    /*override fun onCleared() {
        super.onCleared()
        messageWebSocketClient.disconnect()
    }*/
}