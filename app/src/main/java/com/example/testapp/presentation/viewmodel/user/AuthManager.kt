package com.example.testapp.presentation.viewmodel.user

import android.util.Log
import com.example.testapp.di.api.UserApiService
import com.example.testapp.domain.dto.user.UserLogin
import com.example.testapp.domain.dto.user.UserRequest
import com.example.testapp.domain.dto.user.UserStatusRequest
import com.example.testapp.domain.models.user.UserStatus
import com.example.testapp.utils.storage.AvatarService
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.UserColorGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val userApiService: UserApiService,
    private val dataStoreUtil: DataStoreUtil,
    private val tokenManager: TokenManager,
    private val avatarService: AvatarService
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val currentUserIdFlow = dataStoreUtil.getUserId()

    suspend fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        try {
            val authResponse = userApiService.login(UserLogin(email = email, password = password))
            dataStoreUtil.saveTokens(
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken
            )
            dataStoreUtil.saveUserId(authResponse.userId)
            Log.d("AuthManager", "Login successful: $authResponse")
            _authState.value = AuthState.Authenticated
            updateUserStatus(UserStatusRequest(true))
        } catch (e: Exception) {
            handleAuthException(e, "Login")
        }
    }

    suspend fun signUp(userRequest: UserRequest, password: String) {
        _authState.value = AuthState.Loading
        try {
            val userColor = UserColorGenerator.getUserColor()
            val uniqueId = UUID.randomUUID().toString().take(8)
            val avatarFileName = "${userRequest.nickname}_$uniqueId"
                .replace(Regex("[^a-zA-Z0-9_]"), "_")

            val avatarUrl = avatarService.createUserAvatar(
                avatarFileName,
                userRequest.first_name,
                userRequest.last_name ?: "",
                userColor
            )

            val authResponse = userApiService.register(userRequest.copy(
                avatar_url = avatarUrl,
                password_hash = password,
                user_color = userColor
            ))

            dataStoreUtil.saveTokens(
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken
            )
            dataStoreUtil.saveUserId(authResponse.userId)

            _authState.value = AuthState.Authenticated
            updateUserStatus(UserStatusRequest(true))
        } catch (e: Exception) {
            handleAuthException(e, "Sign Up")
        }
    }

    suspend fun checkAuthStatus(): AuthState {
        _authState.value = AuthState.Loading
        return try {
            val accessToken = tokenManager.getValidAccessToken()
            val newState = if (accessToken != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
            _authState.value = newState
            newState
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Check auth error: ${e.message}")
            AuthState.Error("Check auth error")
        }
    }

    suspend fun updateUserStatus(userStatusRequest: UserStatusRequest): UserStatus? {
        when (val state = _authState.value) {
            is AuthState.Authenticated -> {
                val userId = currentUserIdFlow.firstOrNull()
                return if (userId != null) {
                    try {
                        val response = userApiService.updateUserStatus(userId, userStatusRequest)
                        Log.d(
                            "AuthManager", "User status updated: ${userStatusRequest.onlineStatus} for user $userId"
                        )
                        return response
                    } catch (e: Exception) {
                        Log.e("AuthManager", "Failed to update user status for user $userId", e)
                        null
                    }
                } else {
                    Log.e("AuthManager", "Current user ID is null")
                    null
                }
            }
            is AuthState.Unauthenticated -> {
                Log.d("AuthManager", "User is not authenticated, skipping status update")
            }
            is AuthState.Error -> {
                Log.e("AuthManager", "Authentication error: ${state.message}")
            }
            is AuthState.Loading -> {
                Log.d("AuthManager", "Authentication state is loading, skipping status update")
            }
        }
        return null
    }

    suspend fun logout(onLogoutComplete: () -> Unit) {
        /*currentUserId?.let {
            updateUserStatus(UserStatusRequest(false))
        }*/
        updateUserStatus(UserStatusRequest(false))
        _authState.value = AuthState.Unauthenticated

        dataStoreUtil.clearUserData()
        onLogoutComplete()
    }

    private fun handleAuthException(e: Exception, operation: String) {
        when (e) {
            is HttpException -> {
                Log.e("AuthManager", "$operation server error: ${e.code()}", e)
                _authState.value = AuthState.Error("Server error: ${e.message()}")
            }
            is IOException -> {
                Log.e("AuthManager", "$operation network error: ${e.message}", e)
                _authState.value = AuthState.Error("Network error: Check your internet connection")
            }
            else -> {
                Log.e("AuthManager", "$operation unknown error: ${e.message}", e)
                _authState.value = AuthState.Error("Unknown error occurred")
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}