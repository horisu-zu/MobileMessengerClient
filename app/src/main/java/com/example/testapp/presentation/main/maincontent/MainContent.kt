package com.example.testapp.presentation.main.maincontent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.viewmodel.chat.ChatDisplayViewModel
import com.example.testapp.presentation.viewmodel.message.MessageSummarizeViewModel
import com.example.testapp.utils.Resource

@Composable
fun MainContent(
    chatDisplayViewModel: ChatDisplayViewModel = hiltViewModel(),
    messageSummarizeViewModel: MessageSummarizeViewModel = hiltViewModel(),
    currentUserId: String,
    mainNavController: NavController
) {
    val chatDisplayDataState by chatDisplayViewModel.chatListItemsState.collectAsState()
    val summarizeState = messageSummarizeViewModel.summarizationState.collectAsState()
    var showSummaryDialog by remember { mutableStateOf(false) }

    val sortedChatsList by remember(chatDisplayDataState) {
        derivedStateOf {
            when (chatDisplayDataState) {
                is Resource.Success -> {
                    chatDisplayDataState.data?.entries
                        ?.sortedByDescending { it.value.lastMessage?.createdAt }
                        ?: emptyList()
                }
                else -> emptyList()
            }
        }
    }

    if (showSummaryDialog) {
        SummarizeDialog(
            summarizeState = summarizeState.value,
            onDismiss = { showSummaryDialog = false }
        )
    }

    when (chatDisplayDataState) {
        is Resource.Idle -> { /*TODO*/ }
        is Resource.Error -> { /*TODO*/ }
        is Resource.Loading -> { /*TODO*/ }
        is Resource.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(sortedChatsList) { index, (_, chatItem) ->
                    ChatItem(
                        currentUserId = currentUserId,
                        chat = chatItem,
                        onChatClick = { chatId ->
                            mainNavController.navigate("chatScreen/${chatId}")
                        },
                        onLongClick = { chatId ->
                            messageSummarizeViewModel.summarizeMessages(chatId, currentUserId)
                            showSummaryDialog = true
                        }
                    )
                }
            }
        }
    }
}