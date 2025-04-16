package com.example.testapp.presentation.chat

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.chat.admin.ChatAdminScreen
import com.example.testapp.presentation.chat.main.ChatScreen
import com.example.testapp.presentation.chat.search.ChatScreenSearch
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.notification.NotificationViewModel
import com.example.testapp.presentation.viewmodel.reaction.ReactionViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource

@Composable
fun ChatNavigator(
    chatId: String?,
    notificationViewModel: NotificationViewModel,
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel(),
    reactionViewModel: ReactionViewModel = hiltViewModel(),
    currentUserData: UserResponse?,
    reactionUrls: List<String>,
    mainNavController: NavController
) {
    val chatNavController = rememberNavController()
    val participantsState by chatViewModel.chatParticipantsState.collectAsState()
    val messagesState by messageViewModel.chatMessagesState.collectAsState()

    LaunchedEffect(chatId) {
        chatId?.let {
            currentUserData?.userId?.let { userId ->
                messageViewModel.getMessagesForChat(chatId, userId)
                chatViewModel.getUserRestrictionsInChat(chatId, userId)
            }
            reactionViewModel.loadReactionsForChat(chatId)
            chatViewModel.getChatParticipants(chatId)
            chatViewModel.getChatById(chatId)
            chatViewModel.getChatMetadata(chatId)
        }
    }

    LaunchedEffect(messagesState) {
        if (messagesState.messages.isNotEmpty()) {
            reactionViewModel.setMessageIdsInChat(
                messagesState.messages.mapNotNull { it.messageId }
            )
        }
    }

    LaunchedEffect(participantsState) {
        if(participantsState is Resource.Success) {
            val userIds = participantsState.data?.map { it.userId }
            userIds?.let {
                userViewModel.getUsersByIds(userIds, chatId)
            }
        }
    }

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
                userViewModel = userViewModel,
                chatViewModel = chatViewModel,
                messageViewModel = messageViewModel,
                reactionViewModel = reactionViewModel,
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
        composable(
            route = "chatScreenAdmin",
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            ChatAdminScreen(
                chatId = chatId,
                userViewModel = userViewModel,
                chatNavController = chatNavController
            )
        }
        // Pinned Screen...
    }
}