package com.example.testapp.presentation.viewmodel.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.chat.FilterCriterion
import com.example.testapp.domain.dto.chat.SortBy
import com.example.testapp.domain.dto.message.MessagesState
import com.example.testapp.domain.dto.user.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatSearchViewModel @Inject constructor(
    private val userRepository: UserApiService,
    private val messageRepository: MessageApiService
): ViewModel() {

    private val _searchMessagesState = MutableStateFlow<MessagesState>(MessagesState())
    val searchMessagesState = _searchMessagesState.asStateFlow()

    private var searchJob: Job? = null

    fun searchMessages(
        chatId: String,
        query: String? = null,
        filters: List<FilterCriterion> = emptyList(),
        sortBy: SortBy? = null,
        page: Int = 0,
        size: Int = 20
    ) {
        if (searchJob?.isActive == true) {
            searchJob?.cancel()
        }
        searchJob = viewModelScope.launch {
            delay(300)

            val searchMessages = messageRepository.searchMessagesInChat(
                chatId = chatId,
                query = query,
                fromUser = filters.firstOrNull { it is FilterCriterion.FromUser }?.value,
                hasAttachments = filters.firstOrNull { it is FilterCriterion.HasAttachments }?.value?.toBoolean(),
                page = page,
                size = size,
                sortBy = sortBy?.field?.value ?: "created_at",
                direction = sortBy?.direction?.value ?: "DESC"
            )

            val attachments = searchMessages.mapNotNull { it.messageId }.flatMap { messageId ->
                messageRepository.getAttachmentsForMessage(messageId)
            }.groupBy { it.messageId }

            val replyMessageIds = searchMessages.mapNotNull { it.replyTo }.distinct()

            val replyMessages = if (replyMessageIds.isNotEmpty()) {
                Log.d("ChatSearchViewModel", "Reply Message IDs: $replyMessageIds")
                messageRepository.getMessagesByIds(replyMessageIds).associateBy { it.messageId ?: "" }
            } else { emptyMap() }

            _searchMessagesState.value = MessagesState(
                messages = searchMessages,
                replyMessages = replyMessages,
                attachments = attachments,
                hasMorePages = searchMessages.isNotEmpty(),
                currentPage = page + 1,
                isLoading = false
            )
        }
    }
}