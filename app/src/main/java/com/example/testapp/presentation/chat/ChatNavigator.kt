package com.example.testapp.presentation.chat

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.chat.main.ChatScreen
import com.example.testapp.presentation.chat.search.ChatScreenSearch
import com.example.testapp.presentation.viewmodel.notification.NotificationViewModel

@Composable
fun ChatNavigator(
    chatId: String?,
    notificationViewModel: NotificationViewModel,
    currentUserData: UserResponse?,
    reactionUrls: List<String>,
    mainNavController: NavController
) {
    val chatNavController = rememberNavController()

    NavHost(
        navController = chatNavController,
        startDestination = "chatScreenMain"
    ) {
        composable(
            route = "chatScreenMain",
            enterTransition = { slideInHorizontally { -it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            ChatScreen(
                chatId = chatId,
                notificationViewModel = notificationViewModel,
                currentUser = currentUserData,
                reactionUrls = reactionUrls,
                mainNavController = mainNavController,
                chatNavController = chatNavController
            )
        }
        composable(
            route = "chatScreenSearch",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            ChatScreenSearch(
                chatId = chatId,
                chatNavController = chatNavController
            )
        }
        // Pinned Screen...
    }
}