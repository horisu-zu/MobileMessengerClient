package com.example.testapp.presentation.chat.message

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.utils.Converter.groupMessagesInSequence
import com.example.testapp.utils.Converter.groupReactions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    currentUserId: String,
    messages: Map<String, Message>,
    replyMessages: Map<String, Message>,
    usersData: Map<String, UserResponse>,
    reactionsMap: Map<String, List<Reaction>>,
    reactionUrls: List<String>,
    hasMorePages: Boolean,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onEditClick: (Message) -> Unit,
    onDeleteClick: (String) -> Unit,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Log.d("MessageList", "HasMorePages value — $hasMorePages")
    val listState = rememberLazyListState()
    val messageGroups by remember(messages) { derivedStateOf { groupMessagesInSequence(messages) } }

    LaunchedEffect(messages) {
        if(listState.firstVisibleItemIndex <= 1 && messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(listState, hasMorePages) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (hasMorePages && lastVisibleIndex >= totalItems - 10) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        reverseLayout = true,
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        val totalMessages = messageGroups.sumOf { it.size }

        items(
            count = totalMessages,
            key = { index ->
                var currentIndex = 0
                var targetMessage: Message? = null

                for (group in messageGroups) {
                    if (index < currentIndex + group.size) {
                        targetMessage = group[index - currentIndex]
                        break
                    }
                    currentIndex += group.size
                }
                targetMessage?.messageId ?: index.toString()
            }
        ) { index ->
            var currentIndex = 0
            var targetMessage: Message? = null
            var targetGroup: List<Message>? = null

            for (group in messageGroups) {
                if (index < currentIndex + group.size) {
                    targetMessage = group[index - currentIndex]
                    targetGroup = group
                    break
                }
                currentIndex += group.size
            }

            targetMessage?.let { message ->
                targetGroup?.let { group ->
                    val isFirstInGroup = group.last() == message
                    val isLastInGroup = group.first() == message

                    val replyMessage = replyMessages[message.replyTo]
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

        if(hasMorePages) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}