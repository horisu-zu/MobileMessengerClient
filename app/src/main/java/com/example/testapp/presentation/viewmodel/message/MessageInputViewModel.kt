package com.example.testapp.presentation.viewmodel.message

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.dto.message.LocalAttachment
import com.example.testapp.domain.dto.message.MessageInputState
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageInputViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
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

    fun clearEditing() {
        _messageInputState.update { currentState ->
            currentState?.copy(
                isEditing = false,
                editingMessage = null,
                editedMessageId = null,
                message = null
            )
        }
    }

    fun clearReplying() {
        _messageInputState.update { currentState ->
            currentState?.copy(
                isReplying = false,
                replyToMessage = null
            )
        }
    }

    fun addAttachment(attachment: LocalAttachment) {
        _messageInputState.update { currentState ->
            currentState?.copy(
                localAttachments = currentState.localAttachments + attachment
            )
        }
    }

    fun clearAttachment(attachment: LocalAttachment) {
        _messageInputState.update { currentState ->
            currentState?.copy(
                localAttachments = currentState.localAttachments.filter { it != attachment }
            )
        }
    }

    private fun clearState() {
        _messageInputState.update { currentState ->
            currentState?.copy(
                message = null,
                localAttachments = emptyList(),
                isEditing = false,
                editedMessageId = null,
                editingMessage = null,
                isReplying = false,
                replyToMessage = null
            )
        }
    }

    fun sendMessage(context: Context?) = viewModelScope.launch {
        _messageInputState.value?.let { state ->
            val result = sendMessageUseCase.execute(state, context)
            result.onSuccess {
                clearState()
            }.onFailure {
                //Idk... new state?
            }
        }
    }
}