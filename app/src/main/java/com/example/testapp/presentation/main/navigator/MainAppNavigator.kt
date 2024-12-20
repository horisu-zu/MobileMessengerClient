package com.example.testapp.presentation.main.navigator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testapp.presentation.chat.ChatScreen
import com.example.testapp.presentation.main.MainScreen
import com.example.testapp.presentation.main.group.GroupAddNavigator
import com.example.testapp.presentation.viewmodel.gallery.MediaViewModel
import com.example.testapp.presentation.viewmodel.ThemeViewModel
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.reaction.ReactionViewModel
import com.example.testapp.presentation.viewmodel.user.AuthManager
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.DataStoreUtil
import com.example.testapp.utils.Defaults.fetchEmojiUrls
import com.google.firebase.storage.FirebaseStorage

@Composable
fun MainAppNavigator(
    authManager: AuthManager,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    messageViewModel: MessageViewModel,
    themeViewModel: ThemeViewModel,
    reactionViewModel: ReactionViewModel,
    mediaViewModel: MediaViewModel,
    dataStoreUtil: DataStoreUtil,
    parentNavController: NavController
) {
    val mainNavController = rememberNavController()

    val currentBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentUserState by userViewModel.currentUserState.collectAsStateWithLifecycle()
    val isInChat = currentBackStackEntry?.destination?.route?.startsWith("chatScreen") == true

    var reactionUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        reactionUrls = fetchEmojiUrls(FirebaseStorage.getInstance())
    }

    AnimatedContent(
        targetState = isInChat,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }.using(SizeTransform(clip = false))
        },
        label = "MainAppNavigator"
    ) { inChat ->
        NavHost(navController = mainNavController, startDestination = "mainScreen") {
            composable(route = "mainScreen") {
                if (!inChat) {
                    MainScreen(
                        authManager = authManager,
                        themeViewModel = themeViewModel,
                        userViewModel = userViewModel,
                        currentUser = currentUserState.data,
                        chatViewModel = chatViewModel,
                        messageViewModel = messageViewModel,
                        dataStoreUtil = dataStoreUtil,
                        parentNavController = parentNavController,
                        mainNavController = mainNavController
                    )
                }
            }
            composable(
                route = "chatScreen/{chatId}",
                arguments = listOf(navArgument("chatId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                ChatScreen(
                    chatId = backStackEntry.arguments?.getString("chatId"),
                    userViewModel = userViewModel,
                    messageViewModel = messageViewModel,
                    currentUser = currentUserState.data,
                    chatViewModel = chatViewModel,
                    reactionViewModel = reactionViewModel,
                    mediaViewModel = mediaViewModel,
                    reactionUrls = reactionUrls,
                    mainNavController = mainNavController
                )
            }
            composable("groupAddScreen") {
                GroupAddNavigator(
                    currentUser = currentUserState.data,
                    chatViewModel = chatViewModel,
                    userViewModel = userViewModel,
                    dataStoreUtil = dataStoreUtil,
                    mainNavController = mainNavController
                )
            }
        }
    }
}