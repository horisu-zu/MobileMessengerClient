package com.example.testapp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.domain.dto.user.UserStatusRequest
import com.example.testapp.presentation.auth.AuthScreen
import com.example.testapp.presentation.main.navigator.MainAppNavigator
import com.example.testapp.presentation.profile.ProfileNavigator
import com.example.testapp.presentation.splash.SplashScreen
import com.example.testapp.presentation.viewmodel.main.MainViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.AuthState
import com.example.testapp.presentation.viewmodel.user.TokenManager
import com.example.testapp.ui.theme.AppTheme
import com.example.testapp.utils.storage.AvatarService
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.storage.ChatMediaService
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var dataStoreUtil: DataStoreUtil
    @Inject lateinit var mediaService: ChatMediaService
    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var tokenManager: TokenManager

    override fun onResume() {
        super.onResume()
        checkAccessToken()
        Log.d("MainActivity", "onResume")
        lifecycleScope.launch {
            when (authManager.checkAuthStatus()) {
                is AuthState.Authenticated -> updateUserStatus(true)
                else -> { /**/ }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause")
        lifecycleScope.launch {
            when (authManager.checkAuthStatus()) {
                is AuthState.Authenticated -> updateUserStatus(false)
                else -> { /**/ }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreUtil = DataStoreUtil(applicationContext)
        val systemTheme =
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> false
            }

        setContent {
            val theme = dataStoreUtil.getTheme(systemTheme).collectAsState(initial = systemTheme)
            val navController = rememberNavController()
            val navigationEvent by mainViewModel.navigationEvent.collectAsState()

            AppTheme(darkTheme = theme.value) {
                NavHost(
                    navController = navController,
                    startDestination = navigationEvent,
                    enterTransition = { fadeIn(animationSpec = tween(500)) },
                    exitTransition = { fadeOut(animationSpec = tween(500)) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    composable("splash") {
                        SplashScreen()
                    }
                    composable("auth") {
                        AuthScreen(
                            parentNavController = navController,
                            dataStoreUtil = dataStoreUtil
                        )
                    }
                    composable("main") {
                        MainAppNavigator(
                            mediaService = mediaService,
                            authManager = authManager,
                            parentNavController = navController
                        )
                    }
                    composable("profile") {
                        ProfileNavigator(
                            //currentUser = currentUserState.data,
                            avatarService = AvatarService(FirebaseStorage.getInstance()),
                            mainNavController = navController
                        )
                    }
                }
            }
        }
    }

    private suspend fun updateUserStatus(isOnline: Boolean) {
        authManager.updateUserStatus(UserStatusRequest(isOnline))
    }

    private fun checkAccessToken() {
        lifecycleScope.launch {
            val accessToken = tokenManager.getValidAccessToken()
            if (accessToken != null && tokenManager.shouldRefreshToken(accessToken, 0.5)) {
                Log.d("MainActivity", "Access token is nearing expiration, refreshing...")
                tokenManager.tryRefreshingTokens()
            }
        }
    }
}