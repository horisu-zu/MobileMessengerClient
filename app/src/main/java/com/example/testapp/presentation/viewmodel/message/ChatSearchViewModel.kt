package com.example.testapp.presentation.viewmodel.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.chat.SearchFilter
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

    private val _chatUsersState = MutableStateFlow<List<UserResponse>>(emptyList())
    val chatUsersState = _chatUsersState.asStateFlow()

    private val _searchUsersState = MutableStateFlow<List<UserResponse>>(emptyList())
    val searchUsersState = _searchUsersState.asStateFlow()

    private var searchJob: Job? = null

    fun searchMessages(
        chatId: String,
        query: String? = null,
        filters: List<SearchFilter> = emptyList(),
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
                fromUser = filters.firstOrNull { it is SearchFilter.FromUser }?.value,
                hasAttachments = filters.firstOrNull { it is SearchFilter.HasAttachments }?.value?.toBoolean(),
                page = page,
                size = size,
                sortBy = "created_at",
                direction = filters.firstOrNull { it is SearchFilter.SortDirection }?.value ?: "DESC"
            )

            val attachments = searchMessages.mapNotNull { it.messageId }.flatMap { messageId ->
                messageRepository.getAttachmentsForMessage(messageId)
            }.groupBy { it.messageId }

            val replyMessageIds = searchMessages.mapNotNull { it.replyTo }
                .filter { it !in _searchMessagesState.value.replyMessages.keys }

            val replyMessages = if (replyMessageIds.isNotEmpty()) {
                messageRepository.getMessagesByIds(replyMessageIds).associateBy { it.messageId ?: "" }
            } else {
                emptyMap()
            }

            getUsers((searchMessages.map { it.senderId } + replyMessages.map { it.value.senderId })
                .distinctBy { it })

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

    fun getChatUsers(chatId: String) {
        if (searchJob?.isActive == true) {
            searchJob?.cancel()
        }
        searchJob = viewModelScope.launch {
            val users = userRepository.getUsersByChatId(chatId)
            _chatUsersState.value = users
        }
    }

    private fun getUsers(userIds: List<String>) {
        viewModelScope.launch {
            if(userIds.isEmpty()) return@launch
            val users = userRepository.getByIds(userIds)
            _searchUsersState.value = users
        }
    }
}