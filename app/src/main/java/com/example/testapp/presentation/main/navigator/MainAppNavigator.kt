package com.example.testapp.presentation.main.navigator

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testapp.presentation.chat.ChatScreen
import com.example.testapp.presentation.main.MainScreen
import com.example.testapp.presentation.main.group.GroupAddNavigator
import com.example.testapp.presentation.viewmodel.notification.NotificationViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Defaults.fetchEmojiUrls
import com.google.firebase.storage.FirebaseStorage

@Composable
fun MainAppNavigator(
    authManager: AuthManager,
    userViewModel: UserViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    parentNavController: NavController
) {
    val mainNavController = rememberNavController()
    val currentUserState by userViewModel.currentUserState.collectAsStateWithLifecycle()

    var reactionUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        reactionUrls = fetchEmojiUrls(FirebaseStorage.getInstance())
    }

    NavHost(navController = mainNavController, startDestination = "mainScreen") {
        composable(
            route = "mainScreen",
            enterTransition = { slideInHorizontally { -it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() }
        ) {
            MainScreen(
                authManager = authManager,
                currentUser = currentUserState.data,
                parentNavController = parentNavController,
                notificationViewModel = notificationViewModel,
                mainNavController = mainNavController
            )
        }

        composable(
            route = "chatScreen/{chatId}",
            arguments = listOf(navArgument("chatId") {
                type = NavType.StringType
                nullable = true
            }),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) { backStackEntry ->
            ChatScreen(
                chatId = backStackEntry.arguments?.getString("chatId"),
                notificationViewModel = notificationViewModel,
                currentUser = currentUserState.data,
                reactionUrls = reactionUrls,
                mainNavController = mainNavController
            )
        }

        composable(
            route = "groupAddScreen",
            enterTransition = {
                slideInHorizontally { it } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally { it } + fadeOut()
            }
        ) {
            GroupAddNavigator(
                currentUser = currentUserState.data,
                mainNavController = mainNavController
            )
        }
    }
}