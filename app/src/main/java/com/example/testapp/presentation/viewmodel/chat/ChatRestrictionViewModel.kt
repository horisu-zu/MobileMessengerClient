package com.example.testapp.presentation.viewmodel.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.ChatApiService
import com.example.testapp.domain.dto.chat.ChatRestrictionUpdateRequest
import com.example.testapp.domain.dto.chat.RestrictionExpireType
import com.example.testapp.domain.models.chat.ChatRestriction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatRestrictionViewModel @Inject constructor(
    private val chatRepository: ChatApiService
): ViewModel() {

    private val _restrictionsState = MutableStateFlow<Map<RestrictionExpireType, List<ChatRestriction>>>(
        RestrictionExpireType.entries.associateWith { emptyList() }
    )
    val restrictionState = _restrictionsState.asStateFlow()

    fun getChatRestrictions(
        chatId: String,
        expire: RestrictionExpireType,
        isReloading: Boolean = false
    ) {
        viewModelScope.launch {
            val currentState = _restrictionsState.value[expire]
            if(currentState?.isNotEmpty() == true && !isReloading) {
                Log.d("ChatRestrictionViewModel", "Skipping request for $expire, data already exists")
                return@launch
            }

            try {
                val response = chatRepository.getChatRestrictions(chatId, expire)
                _restrictionsState.update { currentMap ->
                    val newMap = currentMap.toMutableMap()
                    newMap[expire] = response
                    newMap
                }
            } catch (e: Exception) {
                Log.e("ChatRestrictionViewModel", e.message ?: "Unknown Error")
            }
        }
    }

    fun updateRestriction(
        restrictionId: String,
        newDuration: Duration
    ) {
        viewModelScope.launch {
            try {
                val response = chatRepository.updateRestriction(
                    restrictionId,
                    ChatRestrictionUpdateRequest(newDuration.toString())
                )

                updateLocalRestriction(response)
            } catch (e: Exception) {
                Log.e("ChatRestrictionViewModel", e.message ?: "Unknown Error")
            }
        }
    }

    fun updateLocalRestriction(updatedRestriction: ChatRestriction) {
        _restrictionsState.update { currentMap ->
            val mutableMap = currentMap.toMutableMap()

            RestrictionExpireType.entries.forEach { type ->
                val currentList = mutableMap[type] ?: emptyList()
                mutableMap[type] = currentList.filter { it.restrictionId != updatedRestriction.restrictionId }
            }

            val expireType = if (updatedRestriction.expiresAt!!.isBefore(Instant.now())) {
                RestrictionExpireType.EXPIRED
            } else {
                RestrictionExpireType.ACTIVE
            }

            val updatedList = mutableMap[expireType] ?: emptyList()
            mutableMap[expireType] = listOf(updatedRestriction) + updatedList

            mutableMap
        }
    }
}