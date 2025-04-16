package com.example.testapp.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.dto.user.UserPortrait
import com.example.testapp.domain.usecase.CreateUserPortraitUseCase
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPortraitViewModel @Inject constructor(
    private val createUserPortraitUseCase: CreateUserPortraitUseCase
): ViewModel() {

    private val _portraitState = MutableStateFlow<Resource<Map<String, UserPortrait>>>(Resource.Idle())
    val portraitState = _portraitState.asStateFlow()

    fun createUserPortrait(chatId: String, userId: String) {
        viewModelScope.launch {
            val currentMap = _portraitState.value.data?.toMutableMap() ?: mutableMapOf()

            try {
                _portraitState.value = Resource.Loading()
                val response = createUserPortraitUseCase.execute(chatId, userId)

                currentMap[userId] = response
                _portraitState.value = Resource.Success(currentMap)
            } catch (e: Exception) {
                _portraitState.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}