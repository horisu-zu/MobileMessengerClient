package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMost
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.domain.models.reaction.Reaction
import com.example.testapp.presentation.templates.Avatar
import com.example.testapp.presentation.templates.MessageSwipeBackground
import kotlin.math.absoluteValue

@Composable
fun MessageItem(
    currentUserId: String,
    userData: UserResponse,
    usersData: Map<String, UserResponse>,
    message: Message,
    replyMessage: Message?,
    replyUserData: UserResponse?,
    reactionsMap: Map<String, List<Reaction>>,
    attachments: List<Attachment> = emptyList(),
    isCurrentUser: Boolean,
    isLastInGroup: Boolean,
    isFirstInGroup: Boolean,
    onMessageClick: (Offset) -> Unit,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onReactionClick: (String, String, String) -> Unit,
    onReactionLongClick: (String) -> Unit
) {
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer

    val topPadding = when {
        isFirstInGroup -> 8.dp
        else -> 0.dp
    }

    val bottomPadding = when {
        isLastInGroup -> 8.dp
        else -> 0.dp
    }

    val thresholdValue = 240f
    var lastKnownOffset by remember { mutableFloatStateOf(0f) }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { thresholdValue },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    if (lastKnownOffset >= thresholdValue) {
                        onReplyClick(message)
                    }
                    false
                }
                else -> false
            }
        }
    )

    LaunchedEffect(dismissState) {
        snapshotFlow {
            try {
                dismissState.requireOffset()
            } catch (e: IllegalStateException) {
                0f
            }
        }.collect { offset ->
            lastKnownOffset = offset.absoluteValue
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding)
    ) {
        val maxWidth = maxWidth

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.fillMaxWidth(),
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                MessageSwipeBackground(
                    dismissState = dismissState,
                    threshold = thresholdValue,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (avatar, messageSurface, reactionFragment, dateFragment) = createRefs()

                if (!isCurrentUser && isLastInGroup) {
                    Avatar(
                        avatarUrl = userData.avatarUrl,
                        modifier = Modifier.constrainAs(avatar) {
                            start.linkTo(parent.start)
                            bottom.linkTo(messageSurface.bottom)
                        },
                        onClick = { onAvatarClick(userData) }
                    )
                }

                Surface(
                    shape = messageShape(isCurrentUser, isFirstInGroup, isLastInGroup),
                    color = backgroundColor,
                    modifier = Modifier
                        .constrainAs(messageSurface) {
                            top.linkTo(parent.top)
                            if (isCurrentUser) {
                                end.linkTo(parent.end)
                            } else {
                                if (isLastInGroup) {
                                    start.linkTo(avatar.end, margin = 8.dp)
                                } else {
                                    start.linkTo(parent.start, margin = 56.dp)
                                }
                            }
                            width = Dimension.preferredWrapContent.atMost(0.75 * maxWidth)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        onMessageClick(offset)
                                    }
                                )
                            }
                    ) {
                        val attachmentsExists = attachments.isNotEmpty()

                        if (replyMessage != null && replyUserData != null) {
                            ReplyFragment(
                                replyMessage = replyMessage,
                                userData = replyUserData,
                                onReplyClick = { /**/ },
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                                    .fillMaxWidth()
                            )
                        }

                        message.message?.let {
                            MessageTextFragment(
                                message = message.message,
                                replyMessage = replyMessage != null,
                            )
                        }
                    }
                }

                MessageDateFragment(
                    isCurrentUser = isCurrentUser,
                    messageData = message,
                    modifier = Modifier.constrainAs(dateFragment) {
                        if (isCurrentUser) {
                            end.linkTo(messageSurface.start, margin = 8.dp)
                        } else {
                            start.linkTo(messageSurface.end, margin = 8.dp)
                        }
                        bottom.linkTo(messageSurface.bottom, margin = 4.dp)
                    }
                )

                message.messageId?.let { messageId ->
                    ReactionFragment(
                        groupedReactions = reactionsMap,
                        messageId = messageId,
                        currentUserId = currentUserId,
                        usersData = usersData,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        modifier = Modifier.constrainAs(reactionFragment) {
                            top.linkTo(messageSurface.bottom)
                            if (isCurrentUser) {
                                end.linkTo(parent.end)
                            } else {
                                start.linkTo(messageSurface.start)
                            }
                        }
                    )
                }
            }
        }
    }
}