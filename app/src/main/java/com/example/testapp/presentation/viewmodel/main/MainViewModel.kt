package com.example.testapp.presentation.viewmodel.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val authManager: AuthManager): ViewModel() {
    private val _navigationEvent = MutableStateFlow("splash")
    val navigationEvent = _navigationEvent.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            authManager.checkAuthStatus()
            authManager.authState.collect { state ->
                Log.d("MainViewModel", "Auth state: $state")
                val destination = when (state) {
                    is AuthState.Authenticated -> "main"
                    is AuthState.Unauthenticated -> "auth"
                    is AuthState.Error -> "auth"
                    is AuthState.Loading -> return@collect
                }
                Log.d("MainViewModel", "Navigation destination: $destination")
                _navigationEvent.value = destination
            }
        }
    }
}