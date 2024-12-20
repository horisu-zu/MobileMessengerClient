package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.utils.Converter.groupMessagesInSequence
import com.example.testapp.utils.Converter.groupReactions

@Composable
fun MessageList(
    currentUserId: String,
    messages: Map<String, Message>,
    usersData: Map<String, UserResponse>,
    reactionsMap: Map<String, List<Reaction>>,
    reactionUrls: List<String>,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onEditClick: (Message) -> Unit,
    onDeleteClick: (String) -> Unit,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit
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

                val messageReactions = reactionsMap[message.messageId]?.let { reactions ->
                    groupReactions(reactions)
                } ?: emptyMap()

                usersData[message.senderId]?.let { user ->
                    MessageItem(
                        currentUserId = currentUserId,
                        isCurrentUser = currentUserId == message.senderId,
                        isFirstInGroup = isFirstInGroup,
                        isLastInGroup = isLastInGroup,
                        onAvatarClick = onAvatarClick,
                        onReplyClick = onReplyClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        message = message,
                        replyMessage = replyMessage,
                        replyUserData = replyUserData,
                        reactionsMap = messageReactions,
                        reactionUrls = reactionUrls,
                        userData = user,
                        usersData = usersData
                    )
                }
            }
        }
    }
}