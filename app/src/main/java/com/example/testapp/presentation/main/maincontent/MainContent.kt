package com.example.testapp.presentation.main.maincontent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.viewmodel.chat.ChatDisplayViewModel
import com.example.testapp.utils.Resource

@Composable
fun MainContent(
    chatDisplayViewModel: ChatDisplayViewModel = hiltViewModel(),
    currentUserId: String,
    mainNavController: NavController
) {
    val chatDisplayDataState = chatDisplayViewModel.chatListItemsState.collectAsState()

    LaunchedEffect(currentUserId) {
        chatDisplayViewModel.startObservingChats(userId = currentUserId)
    }

    when(chatDisplayDataState.value) {
        is Resource.Error -> { /*TODO*/ }
        is Resource.Loading -> { /*TODO*/ }
        is Resource.Success -> {
            val chatDisplayData = chatDisplayDataState.value.data

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chatDisplayData?.entries?.toList() ?: emptyList()) { (_, chatItem) ->
                    ChatItem(
                        currentUserId = currentUserId,
                        chat = chatItem,
                        onChatClick = { chatId ->
                            mainNavController.navigate("chatScreen/${chatId}")
                        }
                    )
                }
            }
        }
    }
}