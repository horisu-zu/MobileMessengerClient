package com.example.testapp.presentation.viewmodel.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.AIService
import com.example.testapp.di.api.MessageApiService
import com.example.testapp.di.api.UserApiService
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MessageSummarizeViewModel @Inject constructor(
    private val aiService: AIService,
    private val userRepository: UserApiService,
    private val messageRepository: MessageApiService
): ViewModel() {
    private val _summarizationState = MutableStateFlow<Resource<String>>(Resource.Loading())
    val summarizationState = _summarizationState.asStateFlow()

    fun summarizeMessages(chatId: String, userId: String, locale: Locale = Locale.getDefault()) {
        viewModelScope.launch {
            try {
                _summarizationState.value = Resource.Loading()
                val messages = messageRepository.getUnreadMessages(chatId, userId)
                val userIds = messages.map { it.senderId }.distinct()
                val users = userRepository.getByIds(userIds)
                val userNicknamesMap = users.associateBy({ it.userId }, { it.nickname })

                val messagesMap = messages.map { message ->
                    (userNicknamesMap[message.senderId] ?: "Unknown") to message
                }

                val summary = aiService.summarizeMessages(messagesMap, locale)
                _summarizationState.value = Resource.Success(summary)
            } catch (e: Exception) {
                _summarizationState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}