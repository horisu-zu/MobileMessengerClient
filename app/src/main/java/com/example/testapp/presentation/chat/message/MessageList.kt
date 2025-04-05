package com.example.testapp.presentation.chat.message

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatType
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.chat.dropdown.MessageDropdown
import com.example.testapp.utils.Converter.groupMessages
import com.example.testapp.utils.Converter.groupReactions
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun MessageList(
    chatType: ChatType,
    currentUserRole: GroupRole,
    currentUserId: String,
    messages: List<Message>,
    replyMessages: Map<String, Message>,
    usersData: Map<String, UserResponse>,
    reactionsMap: Map<String, List<Reaction>>,
    attachments: Map<String, List<Attachment>>,
    reactionUrls: List<String>,
    hasMorePages: Boolean,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onEditClick: (Message) -> Unit,
    onDeleteClick: (String) -> Unit,
    onTranslateClick: (String) -> Unit,
    onAddRestriction: (String) -> Unit,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit,
    onMarkMessage: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Log.d("MessageList", "HasMorePages value — $hasMorePages")
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val messageGroups by remember(messages) { derivedStateOf { groupMessages(messages) } }

    val showDropdown = remember { mutableStateOf(false) }
    val dropdownPosition = remember { mutableStateOf(Offset.Zero) }
    val selectedMessageId = remember { mutableStateOf<String?>(null) }

    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

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

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .debounce(500L)
            .filter { !it }
            .distinctUntilChanged()
            .collect {
                val newestVisibleItem = listState.layoutInfo.visibleItemsInfo.minByOrNull { it.index }
                val newestVisibleMessageId = newestVisibleItem?.key as? String

                if (newestVisibleMessageId != null) {
                    Log.d("MessageList", "Newest visible message ID: $newestVisibleMessageId")
                    onMarkMessage(newestVisibleMessageId)
                }
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            reverseLayout = true,
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            messageGroups.forEach { dateGroup ->
                dateGroup.messages.forEach { senderGroup ->
                    itemsIndexed(
                        items = senderGroup,
                        key = { _, message -> message.messageId!! }
                    ) { index, message ->
                        val isFirstInGroup = index == senderGroup.lastIndex
                        val isLastInGroup = index == 0

                        val replyMessage = replyMessages[message.replyTo]
                        val replyUserData = replyMessage?.senderId?.let { senderId ->
                            usersData[senderId]
                        }

                        val messageReactions = reactionsMap[message.messageId]?.let { reactions ->
                            groupReactions(reactions)
                        } ?: emptyMap()

                        val messageAttachments = attachments[message.messageId] ?: emptyList()

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
                                attachments = messageAttachments,
                                userData = user,
                                usersData = usersData
                            )
                        }
                    }
                }
                item {
                    DateHeader(dateString = dateGroup.date)
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
                chatType = chatType,
                currentUserRole = currentUserRole,
                reactionUrls = reactionUrls,
                currentUserId = currentUserId,
                expanded = showDropdown.value,
                offset = dropdownPosition.value,
                onDismissRequest = {
                    showDropdown.value = false
                    selectedMessageId.value = null
                },
                messageData = messages[messages.indexOfFirst { message -> message.messageId == it }],
                onReplyMessage = onReplyClick,
                onEditMessage = onEditClick,
                onDeleteMessage = onDeleteClick,
                onTranslateMessage = onTranslateClick,
                onAddRestriction = onAddRestriction,
                onToggleReaction = onReactionClick
            )
        }

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Scroll to bottom",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}