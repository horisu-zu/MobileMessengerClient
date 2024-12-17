package com.example.testapp.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.dto.user.UserRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager.authState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authManager.signIn(email, password)
        }
    }

    fun signUp(userRequest: UserRequest, password: String) {
        viewModelScope.launch {
            authManager.signUp(userRequest, password)
        }
    }

    suspend fun logout(onLogoutComplete: () -> Unit) {
        authManager.logout {
            viewModelScope.launch {
                onLogoutComplete()
            }
        }
    }
}
