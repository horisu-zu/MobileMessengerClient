package com.example.testapp.presentation.viewmodel.reaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.ReactionApiService
import com.example.testapp.di.websocket.ReactionWebSocketClient
import com.example.testapp.domain.dto.reaction.ReactionEvent
import com.example.testapp.domain.dto.reaction.ReactionRequest
import com.example.testapp.domain.dto.reaction.ReactionState
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val reactionRepository: ReactionApiService,
    private val reactionWebSocketClient: ReactionWebSocketClient
): ViewModel() {
    private val _chatReactionsState = MutableStateFlow<Resource<Map<String, List<Reaction>>>>(Resource.Loading())
    val chatReactionsState: StateFlow<Resource<Map<String, List<Reaction>>>> = _chatReactionsState

    private val _messageIdsInChat = MutableStateFlow<List<String>>(emptyList())

    private var currentChatId: String? = null
    private var currentPage: Int = 0
    private var isWebSocketConnected = false

    init {
        viewModelScope.launch {
            try {
                reactionWebSocketClient.getReactionUpdates()
                    .catch { e ->
                        Log.e("WebSocket", "Error receiving reaction updates", e)
                    }
                    .collect { updates ->
                        processWebSocketUpdates(updates)
                    }
            } catch (e: Exception) {
                Log.e("ReactionViewModel", "Uncaught error in reaction subscription", e)
            }
        }
    }

    private fun processWebSocketUpdates(updates: Map<String, ReactionEvent>) {
        val currentState = _chatReactionsState.value
        val updatedReactions = currentState.data?.toMutableMap() ?: mutableMapOf()

        updates.forEach { (messageId, event) ->
            when (event) {
                is ReactionEvent.ReactionAdded -> {
                    val currentReactions = updatedReactions[messageId]?.toMutableList() ?: mutableListOf()
                    if (!currentReactions.any { it.reactionId == event.reaction.reactionId }) {
                        currentReactions.add(event.reaction)
                    }
                    updatedReactions[messageId] = currentReactions.distinctBy { it.reactionId }
                }
                is ReactionEvent.ReactionRemoved -> {
                    val currentReactions = updatedReactions[messageId]?.toMutableList() ?: mutableListOf()
                    currentReactions.removeAll { it.reactionId == event.reaction.reactionId }
                    if (currentReactions.isEmpty()) {
                        updatedReactions.remove(messageId)
                    } else {
                        updatedReactions[messageId] = currentReactions
                    }
                }
            }
        }

        _chatReactionsState.value = Resource.Success(updatedReactions)
    }

    fun setMessageIdsInChat(messageIds: List<String>) {
        viewModelScope.launch {
            val currentIds = _messageIdsInChat.value
            if (currentIds != messageIds) {
                Log.d("ReactionViewModel", "Updating message IDs: from ${currentIds.size} to ${messageIds.size}")
                _messageIdsInChat.value = messageIds

                if (currentChatId != null && messageIds.isNotEmpty()) {
                    updateWebSocket()
                }
            }
        }
    }

    private fun updateWebSocket() {
        val messageIds = _messageIdsInChat.value

        viewModelScope.launch {
            if (isWebSocketConnected) {
                reactionWebSocketClient.disconnect()
                isWebSocketConnected = false
            }

            try {
                reactionWebSocketClient.connect(messageIds)
                isWebSocketConnected = true
                Log.d("ReactionViewModel", "Updated websocket connection for ${messageIds.size} messages")
            } catch (e: Exception) {
                Log.e("ReactionViewModel", "Failed to update websocket connection", e)
                isWebSocketConnected = false
            }
        }
    }

    fun loadReactionsForChat(chatId: String) {
        viewModelScope.launch {
            if (currentChatId != chatId) {
                currentChatId = chatId
                _messageIdsInChat.value = emptyList()
                _chatReactionsState.value = Resource.Loading()
            } else if (currentPage == 0) {
                return@launch
            }

            try {
                val reactions = reactionRepository.getReactionsForChat(
                    chatId = chatId,
                    page = currentPage,
                    size = 30
                )

                val mappedNewReactions = reactions.groupBy { it.messageId }

                val combinedReactions = if (currentPage == 0) {
                    mappedNewReactions
                } else {
                    val merged = _chatReactionsState.value.data?.toMutableMap() ?: mutableMapOf()
                    mappedNewReactions.forEach { (messageId, newReactions) ->
                        val existingReactions = merged[messageId] ?: emptyList()
                        merged[messageId] = (existingReactions + newReactions).distinctBy { it.reactionId }
                    }
                    merged
                }

                Log.d("ReactionViewModel", "Loaded reactions for page — $currentPage")
                _chatReactionsState.value = Resource.Success(combinedReactions)
                currentPage++

            } catch (e: Exception) {
                Log.e("ReactionViewModel", "Error loading reactions: ${e.message}")
                _chatReactionsState.value = Resource.Error(e.message ?: "Error loading reactions")
            }
        }
    }

    fun toggleReaction(messageId: String, userId: String, emojiUrl: String) {
        viewModelScope.launch {
            reactionRepository.toggleReaction(ReactionRequest(
                messageId = messageId,
                userId = userId,
                emoji = emojiUrl
            ))
        }
    }

    override fun onCleared() {
        super.onCleared()
        reactionWebSocketClient.disconnect()
    }
}