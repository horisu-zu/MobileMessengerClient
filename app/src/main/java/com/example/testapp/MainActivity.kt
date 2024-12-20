package com.example.testapp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.testapp.di.api.ApiServices
import com.example.testapp.di.websocket.MessageWebSocketClient
import com.example.testapp.di.websocket.MetadataWebSocketClient
import com.example.testapp.di.websocket.ReactionWebSocketClient
import com.example.testapp.di.websocket.UserStatusWebSocketClient
import com.example.testapp.domain.dto.user.UserStatusRequest
import com.example.testapp.presentation.auth.AuthScreen
import com.example.testapp.presentation.main.navigator.MainAppNavigator
import com.example.testapp.presentation.profile.ProfileNavigator
import com.example.testapp.presentation.splash.SplashScreen
import com.example.testapp.presentation.viewmodel.gallery.MediaViewModel
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.gallery.GalleryViewModel
import com.example.testapp.presentation.viewmodel.main.MainViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.reaction.ReactionViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.AuthState
import com.example.testapp.presentation.viewmodel.user.AuthViewModel
import com.example.testapp.presentation.viewmodel.user.TokenManager
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.ui.theme.AppTheme
import com.example.testapp.utils.AvatarService
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var authManager: AuthManager
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var dataStoreUtil: DataStoreUtil
    private lateinit var tokenManager: TokenManager
    private val avatarService: AvatarService = AvatarService(FirebaseStorage.getInstance())
    private lateinit var userViewModel: UserViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var reactionViewModel: ReactionViewModel
    private lateinit var webSocketClient: UserStatusWebSocketClient
    private lateinit var metadataWebSocketClient: MetadataWebSocketClient
    private lateinit var messageWebSocketClient: MessageWebSocketClient
    private lateinit var reactionWebSocketClient: ReactionWebSocketClient

    override fun onResume() {
        super.onResume()
        checkAccessToken()
        updateUserStatus(true)
    }

    override fun onPause() {
        super.onPause()
        updateUserStatus(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreUtil = DataStoreUtil(applicationContext)
        webSocketClient = UserStatusWebSocketClient(dataStoreUtil, Defaults.baseUrl)
        metadataWebSocketClient = MetadataWebSocketClient(dataStoreUtil)
        messageWebSocketClient = MessageWebSocketClient(dataStoreUtil)
        reactionWebSocketClient = ReactionWebSocketClient(dataStoreUtil)
        ApiServices.initialize(dataStoreUtil)

        val systemTheme =
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> false
            }

        val userRepository = ApiServices.userApiService()
        val chatRepository = ApiServices.chatApiService()
        val messageRepository = ApiServices.messageApiService()
        val reactionRepository = ApiServices.reactionApiService()
        themeViewModel = ThemeViewModel(dataStoreUtil, systemTheme)
        tokenManager = TokenManager(
            apiService = userRepository,
            dataStoreUtil = dataStoreUtil
        )

        authManager = AuthManager(
            userApiService = userRepository,
            dataStoreUtil = dataStoreUtil,
            tokenManager = tokenManager,
            avatarService = avatarService
        )
        //Maybe I should combine them...
        val galleryViewModel = GalleryViewModel()
        val mediaViewModel = MediaViewModel()
        authViewModel = AuthViewModel(authManager)
        mainViewModel = MainViewModel(authManager)
        lifecycleScope.launch {
            authManager.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        userViewModel = UserViewModel(userRepository, dataStoreUtil, webSocketClient)
                        chatViewModel = ChatViewModel(chatRepository, dataStoreUtil, userViewModel, metadataWebSocketClient)
                        messageViewModel = MessageViewModel(messageRepository, dataStoreUtil, messageWebSocketClient)
                        reactionViewModel = ReactionViewModel(reactionRepository, reactionWebSocketClient)
                    }
                    else -> {}
                }
            }
        }

        setContent {
            val theme = dataStoreUtil.getTheme(systemTheme).collectAsState(initial = systemTheme)
            val navController = rememberNavController()
            val navigationEvent by mainViewModel.navigationEvent.collectAsState()

            /** After theme change there's recomposition and user data are reinitializing
             * Idk how I can handle this other than using LaunchedEffect with ID instead of init in ViewModel
             */
            //val currentUserState by userViewModel.currentUserState.collectAsState()
            /*LaunchedEffect(currentUserId) {
                currentUserId?.let { userViewModel.getCurrentUserById(it) }
            }*/

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
                            dataStoreUtil = dataStoreUtil,
                            themeViewModel = themeViewModel,
                            authViewModel = authViewModel
                        )
                    }
                    composable("main") {
                        MainAppNavigator(
                            authManager = authManager,
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel,
                            messageViewModel = messageViewModel,
                            themeViewModel = themeViewModel,
                            reactionViewModel = reactionViewModel,
                            mediaViewModel = mediaViewModel,
                            dataStoreUtil = dataStoreUtil,
                            parentNavController = navController
                        )
                    }
                    composable("profile") {
                        ProfileNavigator(
                            //currentUser = currentUserState.data,
                            userViewModel = userViewModel,
                            galleryViewModel = galleryViewModel,
                            avatarService = avatarService,
                            mainNavController = navController
                        )
                    }
                }
            }
        }
    }

    private fun updateUserStatus(isOnline: Boolean) {
        lifecycleScope.launch {
            dataStoreUtil.getUserId().collect { userId ->
                userId?.let {
                    authManager.updateUserStatus(UserStatusRequest(isOnline))
                }
            }
        }
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