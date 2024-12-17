package com.example.testapp.presentation.main.maincontent

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testapp.R
import com.example.testapp.domain.models.chat.Chat
import com.example.testapp.domain.dto.chat.ChatDisplayData
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.Resource
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatsList(
    currentUserId: String,
    chats: List<Chat>,
    displayDataState: Map<String, Resource<ChatDisplayData>>,
    lastMessages: Map<String, Message>?,
    userViewModel: UserViewModel,
    onChatClick: (String) -> Unit
) {
    val sortedChats = chats.sortedByDescending { it.createdAt }
    val lastMessageUserState by userViewModel.lastMessageUserState.collectAsStateWithLifecycle()

    LazyColumn {
        items(
            items = sortedChats,
            key = { it.chatId ?: "" }
        ) { chat ->
            chat.chatId?.let { chatId ->
                when(val displayData = displayDataState[chatId]) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                    is Resource.Success -> {
                        val lastMessage = lastMessages?.get(chatId)
                        val userState = lastMessage?.let { lastMessageUserState[it.senderId] }

                        if (lastMessage != null && userState == null) {
                            userViewModel.loadUser(lastMessage.senderId)
                        }

                        ChatItem(
                            currentUserId = currentUserId,
                            chat = chat,
                            chatName = displayData.data?.name ?: "Unknown Chat",
                            chatAvatar = displayData.data?.avatarUrl,
                            senderName = when (userState) {
                                is Resource.Success -> userState.data?.nickname ?: "Unknown Sender"
                                else -> "Unknown Sender"
                            },
                            lastMessage = lastMessage,
                            onChatClick = { onChatClick(chatId) }
                        )
                    }
                    null -> {}
                }
            }
        }
    }
}

@Composable
fun ChatAvatar(
    avatarUrl: String, chatType: Int, modifier: Modifier = Modifier
) {
    when (chatType) {
        1 -> Avatar(avatarUrl = avatarUrl, isGroupChat = false)
        2 -> Avatar(avatarUrl = avatarUrl, isGroupChat = true)
    }
}

@SuppressLint("NewApi")
fun formatTimestamp(instant: Instant): String {
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val today = LocalDate.now()

    return when {
        dateTime.toLocalDate() == today -> {
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        dateTime.toLocalDate() == today.minusDays(1) -> {
            "Yesterday"
        }
        dateTime.toLocalDate().year == today.year -> {
            dateTime.format(DateTimeFormatter.ofPattern("d MMM"))
        }
        else -> {
            dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        }
    }
}
