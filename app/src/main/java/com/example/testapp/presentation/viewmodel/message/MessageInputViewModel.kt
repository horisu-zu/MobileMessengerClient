package com.example.testapp.presentation.viewmodel.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.domain.dto.message.MessageInputState
import com.example.testapp.domain.dto.message.MessageRequest
import com.example.testapp.domain.dto.message.MessageUpdateRequest
import com.example.testapp.domain.models.message.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageInputViewModel @Inject constructor(
    private val messageRepository: MessageApiService
) : ViewModel() {

    private val _messageInputState = MutableStateFlow<MessageInputState?>(null)
    val messageInputState = _messageInputState.asStateFlow()

    fun initialize(chatId: String, senderId: String) {
        if (_messageInputState.value == null) {
            _messageInputState.value = MessageInputState(
                chatId = chatId,
                senderId = senderId
            )
        }
    }

    fun setMessage(message: String?) {
        _messageInputState.update { currentState ->
            currentState?.copy(message = message)
        }
    }

    fun startEditing(message: Message) {
        _messageInputState.update { currentState ->
            currentState?.copy(
                isEditing = true,
                editingMessage = message,
                editedMessageId = message.messageId,
                message = message.message
            )
        }
    }

    fun startReplying(message: Message) {
        _messageInputState.update { currentState ->
            currentState?.copy(
                isReplying = true,
                replyToMessage = message
            )
        }
    }

    fun clearState() {
        _messageInputState.update { currentState ->
            currentState?.copy(
                message = null,
                isEditing = false,
                editedMessageId = null,
                editingMessage = null,
                isReplying = false,
                replyToMessage = null
            )
        }
    }

    fun sendMessage() = viewModelScope.launch {
        _messageInputState.value?.let { state ->
            when {
                state.isEditing -> {
                    state.editedMessageId?.let { messageId ->
                        messageRepository.updateMessage(
                            messageId,
                            MessageUpdateRequest(message = state.message)
                        )
                    }
                }
                else -> {
                    messageRepository.createMessage(
                        MessageRequest(
                            chatId = state.chatId,
                            senderId = state.senderId,
                            message = state.message,
                            replyTo = state.replyToMessage?.messageId
                        )
                    )
                }
            }
            clearState()
        }
    }
}