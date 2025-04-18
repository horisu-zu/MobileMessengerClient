package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.dto.chat.ChatRestrictionRequest
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.domain.models.chat.RestrictionType
import com.example.testapp.domain.usecase.CreateChatRestrictionUseCase
import com.example.testapp.domain.usecase.UpdateChatRestrictionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class ChatRestrictionInputViewModel @Inject constructor(
    private val createChatRestrictionUseCase: CreateChatRestrictionUseCase,
    private val updateChatRestrictionUseCase: UpdateChatRestrictionUseCase
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

    private val _completionEvent = MutableSharedFlow<ChatRestriction>(0)
    val completionEvent = _completionEvent.asSharedFlow()

    fun createUserRestriction(
        chatId: String,
        chatRestrictionRequest: ChatRestrictionRequest
    ) = viewModelScope.launch {

        val response = createChatRestrictionUseCase.execute(chatId, chatRestrictionRequest)
        response.fold(
            onSuccess = { restriction ->
                _completionEvent.emit(restriction)
            },
            onFailure = { error ->
                Log.e("ChatRestrictionInputViewModel", "Error: ${error.message}")
            }
        )
    }

    fun updateRestriction(
        restrictionId: String,
        newDuration: Duration
    ) = viewModelScope.launch {

        try {
            val response = updateChatRestrictionUseCase.execute(restrictionId, newDuration)
            response.fold(
                onSuccess = { restriction ->
                    _completionEvent.emit(restriction)
                },
                onFailure = { error ->
                    Log.e("ChatRestrictionInputViewModel", "Error: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e("ChatRestrictionInputViewModel", "Error: ${e.message}")
        }
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