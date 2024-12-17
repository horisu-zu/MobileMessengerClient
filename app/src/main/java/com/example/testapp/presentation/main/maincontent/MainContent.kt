package com.example.testapp.presentation.main.maincontent

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.testapp.presentation.viewmodel.chat.ChatViewModel
import com.example.testapp.presentation.viewmodel.message.MessageViewModel
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource

@Composable
fun MainContent(
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    messageViewModel: MessageViewModel,
    currentUserId: String,
    mainNavController: NavController
) {
    val chatsListState by chatViewModel.userChatsState.collectAsStateWithLifecycle()
    val chatDisplayDataState by chatViewModel.chatDisplayDataState.collectAsStateWithLifecycle()
    val lastMessagesState by messageViewModel.lastMessagesState.collectAsStateWithLifecycle()

    val effectKey = remember(currentUserId) { currentUserId }
    LaunchedEffect(effectKey) {
        Log.d("MainScreen", "Start observing chats triggered")
        chatViewModel.startObservingChats(currentUserId)
    }

    when (chatsListState) {
        is Resource.Loading -> {
            //LoadingIndicator()
        }

        is Resource.Error -> {
            //ErrorContent(...
        }

        is Resource.Success -> {
            val chats = chatsListState.data ?: emptyList()
            val chatIds = chats.mapNotNull { it.chatId }

            LaunchedEffect(chatIds) {
                if (chatIds.isNotEmpty()) {
                    messageViewModel.getLastMessages(chatIds)
                }
            }

            LaunchedEffect(chats.map { it.chatId }) {
                Log.d("MainScreen", "Load chat display data triggered")
                chats.filter { chat ->
                    chat.chatId?.let { chatId ->
                        chatDisplayDataState[chatId] !is Resource.Success
                    } ?: false
                }.forEach { chat ->
                    chatViewModel.loadChatDisplayData(chat, currentUserId)
                }
            }

            ChatsList(
                currentUserId = currentUserId,
                chats = chats,
                displayDataState = chatDisplayDataState,
                lastMessages = lastMessagesState.data,
                userViewModel = userViewModel,
                onChatClick = { chatId ->
                    mainNavController.navigate("chatScreen/$chatId")
                }
            )
        }
    }
}