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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val reactionRepository: ReactionApiService,
    private val reactionWebSocketClient: ReactionWebSocketClient
): ViewModel() {
    private val _chatReactionsState = MutableStateFlow(ReactionState())
    val chatReactionsState: StateFlow<ReactionState> = _chatReactionsState

    private var currentChatId: String? = null
    private var currentConnectedMessageIds = emptyList<String>()
    //private val _messageIdsFlow = MutableSharedFlow<List<String>>(replay = 1)

    init {
        /*viewModelScope.launch {
            _messageIdsFlow
                .collect { messageIds ->
                    if (messageIds.isNotEmpty()) {
                        loadReactions(messageIds)
                    }
                }
        }*/

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

        val updatedReactions = currentState.reactions.toMutableMap()

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

        _chatReactionsState.value = currentState.copy(reactions = updatedReactions)
    }

    fun loadReactionsForChat(chatId: String) {
        viewModelScope.launch {
            if (_chatReactionsState.value.isLoading || !_chatReactionsState.value.hasMorePages) return@launch

            if (currentChatId != chatId) {
                currentChatId = chatId
                _chatReactionsState.value = ReactionState(isLoading = true)
                if (currentConnectedMessageIds.isNotEmpty()) {
                    reactionWebSocketClient.disconnect()
                    currentConnectedMessageIds = emptyList()
                }
            } else {
                _chatReactionsState.value = _chatReactionsState.value.copy(isLoading = true)
            }

            try {
                val reactions = reactionRepository.getReactionsForChat(
                    chatId = chatId,
                    page = _chatReactionsState.value.currentPage,
                    size = 30
                )

                val mappedNewReactions = reactions.groupBy { it.messageId }

                val combinedReactions = if (_chatReactionsState.value.currentPage == 0) {
                    mappedNewReactions
                } else {
                    val merged = _chatReactionsState.value.reactions.toMutableMap()

                    mappedNewReactions.forEach { (messageId, newReactions) ->
                        val existingReactions = merged[messageId] ?: emptyList()
                        merged[messageId] = (existingReactions + newReactions).distinctBy { it.reactionId }
                    }

                    merged
                }

                val newMessageIds = mappedNewReactions.keys.toList()
                if (newMessageIds.isNotEmpty()) {
                    val allMessageIds = (currentConnectedMessageIds + newMessageIds).distinct()
                    reactionWebSocketClient.connect(allMessageIds)
                    currentConnectedMessageIds = allMessageIds
                }

                Log.d("ReactionViewModel", "Loaded reactions: $reactions")
                _chatReactionsState.value = _chatReactionsState.value.copy(
                    reactions = combinedReactions,
                    currentPage = _chatReactionsState.value.currentPage + 1,
                    hasMorePages = reactions.isNotEmpty(),
                    isLoading = false,
                    error = null
                )

            } catch (e: Exception) {
                Log.e("ReactionViewModel", "Error loading reactions: ${e.message}")
                _chatReactionsState.value = _chatReactionsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading reactions for chat: $chatId"
                )
            }
        }
    }

    /*private suspend fun loadReactions(messageIds: List<String>) {
        try {
            val reactions = messageIds.associateWith { messageId ->
                reactionRepository.getReactionsForMessage(messageId)
            }
            _chatReactionsState.value = Resource.Success(reactions)
            reactionWebSocketClient.connect(messageIds)
            currentConnectedMessageIds = messageIds
        } catch (e: Exception) {
            Log.e("ReactionViewModel", "Error loading reactions: ${e.message}")
            _chatReactionsState.value = Resource.Error(
                e.message ?: "Error loading reactions for messages: ${messageIds.joinToString()}"
            )
        }
    }

    fun loadReactionsForMessages(messageIds: List<String>) {
        if (messageIds == currentConnectedMessageIds) return
        viewModelScope.launch {
            _messageIdsFlow.emit(messageIds)
        }
    }*/

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