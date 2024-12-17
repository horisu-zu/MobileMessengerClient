package com.example.testapp.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testapp.di.api.UserApiService
import com.example.testapp.di.websocket.UserStatusWebSocketClient
import com.example.testapp.utils.DataStoreUtil

class UserViewModelFactory(
    private val userRepository: UserApiService,
    private val dataStoreUtil: DataStoreUtil,
    private val webSocketClient: UserStatusWebSocketClient
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository, dataStoreUtil, webSocketClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
