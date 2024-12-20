package com.example.testapp.presentation.viewmodel.reaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.ReactionApiService
import com.example.testapp.di.websocket.ReactionWebSocketClient
import com.example.testapp.domain.dto.reaction.ReactionEvent
import com.example.testapp.domain.dto.reaction.ReactionRequest
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.utils.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ReactionViewModel(
    private val reactionRepository: ReactionApiService,
    private val reactionWebSocketClient: ReactionWebSocketClient
): ViewModel() {
    private val _chatReactionsState = MutableStateFlow<Resource<Map<String, List<Reaction>>>>(Resource.Loading())
    val chatReactionsState: StateFlow<Resource<Map<String, List<Reaction>>>> = _chatReactionsState

    private var currentConnectedMessageIds = emptyList<String>()
    private val _messageIdsFlow = MutableSharedFlow<List<String>>(replay = 1)

    init {
        viewModelScope.launch {
            _messageIdsFlow
                .collect { messageIds ->
                    if (messageIds.isNotEmpty()) {
                        loadReactions(messageIds)
                    }
                }
        }

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
        if (currentState !is Resource.Success) return

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

    private suspend fun loadReactions(messageIds: List<String>) {
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
    }

    suspend fun toggleReaction(messageId: String, userId: String, emojiUrl: String) {
        reactionRepository.toggleReaction(ReactionRequest(
            messageId = messageId,
            userId = userId,
            emoji = emojiUrl
        ))
    }

    override fun onCleared() {
        super.onCleared()
        reactionWebSocketClient.disconnect()
    }
}