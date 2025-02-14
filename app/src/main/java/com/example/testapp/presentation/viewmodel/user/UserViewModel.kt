package com.example.testapp.presentation.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.di.api.UserApiService
import com.example.testapp.di.websocket.UserStatusWebSocketClient
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserApiService,
    private val dataStoreUtil: DataStoreUtil,
    private val webSocketClient: UserStatusWebSocketClient
) : ViewModel() {

    private val _participantsState = MutableStateFlow<Resource<List<UserResponse>>>(Resource.Loading())
    val participantsState: MutableStateFlow<Resource<List<UserResponse>>> = _participantsState

    private val _currentUserState = MutableStateFlow<Resource<UserResponse>>(Resource.Loading())
    val currentUserState: StateFlow<Resource<UserResponse>> = _currentUserState

    private val _usersState = MutableStateFlow<Resource<List<UserResponse>>>(Resource.Loading())
    val usersState: StateFlow<Resource<List<UserResponse>>> = _usersState

    private val _userStatusState = MutableStateFlow<Map<String, UserStatus>>(emptyMap())
    val userStatusState: StateFlow<Map<String, UserStatus>> = _userStatusState

    private val _lastMessageUserStates = MutableStateFlow<Map<String, Resource<UserResponse>>>(emptyMap())
    val lastMessageUserState: StateFlow<Map<String, Resource<UserResponse>>> get() = _lastMessageUserStates


    private val currentUserIdFlow = dataStoreUtil.getUserId()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            launch {
                webSocketClient.getStatusUpdates()
                    .catch { e ->
                        Log.e("WebSocket", "Error receiving status updates", e)
                    }
                    .collect { newStatuses ->
                        Log.d("UserViewModel", "Received status update: $newStatuses")
                        _userStatusState.update { currentMap ->
                            currentMap.toMutableMap().apply {
                                newStatuses.forEach { (userId, status) ->
                                    this[userId] = status
                                }
                            }
                        }
                    }
            }
            launch {
                currentUserIdFlow
                    .distinctUntilChanged()
                    .collect { userId ->
                        if (userId != null) {
                            Log.d("UserViewModel", "Loading User with ID: $userId")
                            getCurrentUserById(userId)
                        } else {
                            _currentUserState.value = Resource.Error("User ID is null")
                        }
                    }
            }
        }
    }

    private fun getCurrentUserById(userId: String) {
        viewModelScope.launch {
            _currentUserState.value = Resource.Loading()
            try {
                val user = userRepository.getUserById(userId)
                Log.d("Current User", "Loading User for ID: $userId")
                _currentUserState.value = Resource.Success(user)
                Log.d("Current User", "Current User: ${user.nickname}")
            } catch (e: Exception) {
                Log.d("Current Error", "Error loading user: ${e.message}")
                _currentUserState.value = Resource.Error("Error loading user: ${e.message}")
            }
        }
    }

    suspend fun getUserInPersonalChat(userId: String) {
        _usersState.value = Resource.Loading()
        try {
            val user = userRepository.getUserById(userId)
            _usersState.value = Resource.Success(listOf(user))
            connectWebSocket(listOf(user).map { it.userId })
        } catch (e: Exception) {
            _usersState.value = Resource.Error("Failed to search users: ${e.message}")
        }
    }

    suspend fun getUserById(userId: String): UserResponse {
        return userRepository.getUserById(userId)
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            if (_lastMessageUserStates.value[userId] !is Resource.Success) {
                _lastMessageUserStates.update { current ->
                    current + (userId to Resource.Loading())
                }
                try {
                    val user = userRepository.getUserById(userId)
                    _lastMessageUserStates.update { current ->
                        current + (userId to Resource.Success(user))
                    }
                } catch (e: Exception) {
                    _lastMessageUserStates.update { current ->
                        current + (userId to Resource.Error(e.message ?: "Error"))
                    }
                }
            }
        }
    }

    suspend fun getUsersByIds(userIds: List<String>) {
        _participantsState.value = Resource.Loading()
        try {
            val usersList = userRepository.getByIds(userIds)
            _participantsState.value = Resource.Success(usersList)
            connectWebSocket(usersList.map { it.userId })
        } catch (e: Exception) {
            _participantsState.value = Resource.Error("Failed to find users: ${e.message}")
        }
    }

    fun searchUsers(nickname: String? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _usersState.value = Resource.Loading()

            try {
                val users = userRepository.getUsersByNickname(nickname)
                val currentUserId = currentUserIdFlow.first()
                val filteredUsers = users.filter { it.userId != currentUserId }

                _usersState.value = Resource.Success(filteredUsers)
                connectWebSocket(users.map { it.userId })
            } catch (e: Exception) {
                _usersState.value = Resource.Error("Failed to search users: ${e.message}")
            }
        }
    }

    private fun connectWebSocket(userIds: List<String>) {
        webSocketClient.connect(userIds)
    }

    public override fun onCleared() {
        Log.d("UserViewMode", "onCleared")
        super.onCleared()
        webSocketClient.disconnect()
    }

    suspend fun updateUser(userResponse: UserResponse) {
        Log.d("UserViewModel", "Updating user with ID: ${userResponse.userId}")

        try {
            val updatedUser = userRepository.updateUser(userResponse.userId, userResponse)
            _currentUserState.value = Resource.Success(updatedUser)
            Log.d("UserViewModel", "User successfully updated: ${updatedUser.nickname}")
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating user: ${e.message}")
            _currentUserState.value = Resource.Error("Failed to update user: ${e.message}")
        }
    }

    fun updateNickname(userResponse: UserResponse): Flow<Result<UserResponse>> = flow {
        try {
            Log.d("UserViewModel", "Updating user with ID: ${userResponse.userId}")
            val updatedUser = userRepository.updateNickname(userResponse.userId, userResponse)
            _currentUserState.value = Resource.Success(updatedUser)
            emit(Result.success(updatedUser))
            Log.d("UserViewModel", "User successfully updated: ${updatedUser.nickname}")
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating user: ${e.message}")
            emit(Result.failure(e))
        }
    }
}
