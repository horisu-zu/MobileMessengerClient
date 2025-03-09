package com.example.testapp.presentation.chat.message

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.chat.dropdown.MessageDropdown
import com.example.testapp.utils.Converter.groupMessagesInSequence
import com.example.testapp.utils.Converter.groupReactions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    currentUserRole: GroupRole,
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
    onAddRestriction: (String) -> Unit,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Log.d("MessageList", "HasMorePages value — $hasMorePages")
    val listState = rememberLazyListState()
    val messageGroups by remember(messages) { derivedStateOf { groupMessagesInSequence(messages) } }

    val showDropdown = remember { mutableStateOf(false) }
    val dropdownPosition = remember { mutableStateOf(Offset.Zero) }
    val selectedMessageId = remember { mutableStateOf<String?>(null) }

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
                            onMessageClick = { offset ->
                                selectedMessageId.value = message.messageId
                                showDropdown.value = true
                                dropdownPosition.value = offset
                            },
                            onAvatarClick = onAvatarClick,
                            onReplyClick = onReplyClick,
                            onReactionClick = onReactionClick,
                            onReactionLongClick = onReactionLongClick,
                            message = message,
                            replyMessage = replyMessage,
                            replyUserData = replyUserData,
                            reactionsMap = messageReactions,
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

    selectedMessageId.value?.let {
        MessageDropdown(
            currentUserRole = currentUserRole,
            reactionUrls = reactionUrls,
            currentUserId = currentUserId,
            expanded = showDropdown.value,
            offset = dropdownPosition.value,
            onDismissRequest = {
                showDropdown.value = false
                selectedMessageId.value = null
            },
            messageData = messages[selectedMessageId.value]!!,
            onReplyMessage = onReplyClick,
            onEditMessage = onEditClick,
            onDeleteMessage = onDeleteClick,
            onAddRestriction = onAddRestriction,
            onToggleReaction = onReactionClick
        )
    }
}