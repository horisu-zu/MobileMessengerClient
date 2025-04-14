package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.dto.chat.ChatRestrictionRequest
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.domain.models.chat.RestrictionType
import com.example.testapp.domain.usecase.CreateChatRestrictionUseCase
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class ChatRestrictionInputViewModel @Inject constructor(
    private val createChatRestrictionUseCase: CreateChatRestrictionUseCase
): ViewModel() {

    private val _inputState = MutableStateFlow<ChatRestrictionRequest>(
        ChatRestrictionRequest(
            userId = "",
            type = RestrictionType.MUTE,
            duration = Duration.ZERO.toString(),
            reason = "",
            createdBy = ""
        )
    )
    val inputState = _inputState.asStateFlow()

    private val _restrictionState = MutableStateFlow<Resource<ChatRestriction>>(Resource.Idle())
    val restrictionState = _restrictionState.asStateFlow()

    fun createUserRestriction(
        chatId: String,
        chatRestrictionRequest: ChatRestrictionRequest
    ) = viewModelScope.launch {
        _restrictionState.value = Resource.Loading()

        val response = createChatRestrictionUseCase.execute(chatId, chatRestrictionRequest)
        response.fold(
            onSuccess = { restriction ->
                _restrictionState.value = Resource.Success(restriction)
                clearInputState()
            },
            onFailure = { error ->
                Log.e("ChatRestrictionInputViewModel", "Error: ${error.message}")
                _restrictionState.value = Resource.Error(error.message ?: "Unknown Error")
            }
        )
    }

    fun updateInputState(chatRestrictionRequest: ChatRestrictionRequest) {
        _inputState.value = chatRestrictionRequest
    }

    fun clearInputState() {
        _inputState.value = ChatRestrictionRequest(
            userId = _inputState.value.userId,
            type = RestrictionType.MUTE,
            duration = Duration.ZERO.toString(),
            reason = "",
            createdBy = _inputState.value.createdBy
        )
    }
}