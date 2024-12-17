package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atLeast
import androidx.constraintlayout.compose.atMost
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.chat.dropdown.MessageDropdown
import com.example.testapp.presentation.templates.Avatar

@Composable
fun MessageItem(
    userData: UserResponse,
    message: Message,
    replyMessage: Message?,
    replyUserData: UserResponse?,
    attachments: List<Attachment> = emptyList(),
    isCurrentUser: Boolean,
    isLastInGroup: Boolean,
    isFirstInGroup: Boolean,
    onAvatarClick: (UserResponse) -> Unit,
    onReplyClick: (Message) -> Unit,
    onEditClick: (Message) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val showDropdown = remember { mutableStateOf(false) }
    val dropdownPosition = remember { mutableStateOf(Offset.Zero) }
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val topPadding = when {
        isFirstInGroup -> 8.dp
        else -> 2.dp
    }

    val bottomPadding = when {
        isLastInGroup -> 8.dp
        else -> 2.dp
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding)
    ) {
        val maxWidth = maxWidth

        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (avatar, messageSurface, dateFragment) = createRefs()

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
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    dropdownPosition.value = offset
                                    showDropdown.value = true
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
                        )
                    }

                    message.message?.let {
                        MessageTextFragment(
                            message = message.message,
                            replyMessage = replyMessage != null,
                        )
                    }
                }
                /*ConstraintLayout(
                    //modifier = Modifier.clickable { showDropdown.value = true }
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                dropdownPosition.value = offset
                                showDropdown.value = true
                            }
                        )
                    }
                ) {
                    val (replyBox, messageText) = createRefs()
                    val attachmentsExists = attachments.isNotEmpty()

                    if (replyMessage != null && replyUserData != null) {
                        ReplyFragment(
                            replyMessage = replyMessage,
                            userData = replyUserData,
                            onReplyClick = { *//**//* },
                            modifier = Modifier
                                .constrainAs(replyBox) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                    width = Dimension.fillToConstraints.atLeast(200.dp)
                                }
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                        )
                    }

                    message.message?.let {
                        MessageTextFragment(
                            message = message.message,
                            replyMessage = replyMessage != null,
                            modifier = Modifier.constrainAs(messageText) {
                                top.linkTo(replyBox.bottom)
                                if (isCurrentUser) {
                                    end.linkTo(parent.end)
                                } else {
                                    start.linkTo(parent.start)
                                }
                            }
                        )
                    }
                }*/
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
            MessageDropdown(
                isCurrentUser = isCurrentUser,
                expanded = showDropdown.value,
                horizontalOffset = dropdownPosition.value.x.dp,
                onDismissRequest = { showDropdown.value = false },
                messageData = message,
                onReplyMessage = onReplyClick,
                onEditMessage = onEditClick,
                onDeleteMessage = onDeleteClick
            )
        }
    }
}