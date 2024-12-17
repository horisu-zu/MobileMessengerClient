package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Message

@Composable
fun MessageList(
    currentUserId: String,
    messages: Map<String, Message>,
    usersData: Map<String, UserResponse>,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onEditClick: (Message) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    val messageGroups by remember(messages) {
        mutableStateOf(groupMessagesInSequence(messages))
    }

    LazyColumn(
        reverseLayout = true,
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        messageGroups.forEach { group ->
            items(
                items = group,
                key = { it.messageId!! }
            ) { message ->
                val isFirstInGroup = group.last() == message
                val isLastInGroup = group.first() == message

                val replyMessage = message.replyTo?.let { replyId ->
                    messages[replyId]
                }

                val replyUserData = replyMessage?.senderId?.let { senderId ->
                    usersData[senderId]
                }

                usersData[message.senderId]?.let { user ->
                    MessageItem(
                        isCurrentUser = currentUserId == message.senderId,
                        isFirstInGroup = isFirstInGroup,
                        isLastInGroup = isLastInGroup,
                        onAvatarClick = onAvatarClick,
                        onReplyClick = onReplyClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        message = message,
                        replyMessage = replyMessage,
                        replyUserData = replyUserData,
                        userData = user
                    )
                }
            }
        }
    }
}

fun groupMessagesInSequence(messages: Map<String, Message>): List<List<Message>> {
    val sortedMessages = messages.values.sortedByDescending { it.createdAt }
    return sortedMessages.fold(mutableListOf<MutableList<Message>>()) { groups, message ->
        if (groups.isEmpty() || groups.last().first().senderId != message.senderId) {
            groups.add(mutableListOf(message))
        } else {
            groups.last().add(message)
        }
        groups
    }
}