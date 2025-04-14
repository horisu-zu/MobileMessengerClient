package com.example.testapp.presentation.chat.main

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.domain.dto.message.LocalAttachment
import com.example.testapp.domain.dto.message.MessageInputState
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.chat.ChatRestriction
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.presentation.templates.media.MediaBottomSheet
import com.example.testapp.presentation.viewmodel.gallery.MediaViewModel
import java.time.Instant

@Composable
fun MessageInput(
    userData: Map<String, UserResponse>,
    mediaViewModel: MediaViewModel,
    messageInputState: MessageInputState,
    currentUserRestriction: ChatRestriction?,
    onSendClick: () -> Unit,
    onMessageInputChange: (String) -> Unit,
    onClearEditing: () -> Unit,
    onAddAttachment: (LocalAttachment) -> Unit,
    onClearAttachment: (LocalAttachment) -> Unit,
    onClearReplying: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val messageAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var showMediaBottomSheet by remember { mutableStateOf(false) }
    val mediaState = mediaViewModel.mediaState.collectAsState()
    val isUserRestricted = remember(currentUserRestriction) {
        derivedStateOf {
            when (currentUserRestriction) {
                null -> false
                else -> currentUserRestriction.expiresAt?.let { it > Instant.now() } ?: true
            }
        }
    }

    Column(modifier = modifier) {
        if (messageInputState.localAttachments.isNotEmpty()) {
            LazyRow(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(messageInputState.localAttachments) { attachment ->
                    AttachmentPreview(
                        attachment = attachment,
                        onRemove = { onClearAttachment(attachment) },
                        context = context
                    )
                }
            }
        }

        messageInputState.editingMessage?.let { message ->
            EditMessage(
                editingMessage = message,
                attachments = messageAttachments,
                onCancelEdit = onClearEditing
            )
        }

        messageInputState.replyToMessage?.let { message ->
            userData[message.senderId]?.let { replyUserData ->
                ReplyToMessage(
                    replyMessage = message,
                    userData = replyUserData,
                    attachments = messageAttachments,
                    onCancelReply = onClearReplying
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                enabled = !isUserRestricted.value,
                value = messageInputState.message ?: "",
                onValueChange = { newValue ->
                    onMessageInputChange(newValue)
                },
                modifier = Modifier
                    .weight(1f)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant),
                placeholder = {
                    Text(
                        when {
                            isUserRestricted.value -> "You're muted"
                            messageInputState.isEditing -> "Edit message"
                            messageInputState.isReplying -> "Reply to message"
                            else -> "Type a message"
                        }
                    )
                },
                maxLines = 4,
                colors = messageInputColors()
            )

            IconButton(
                enabled = !isUserRestricted.value,
                onClick = { showMediaBottomSheet = true }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clip),
                    contentDescription = "Attach media",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = { onSendClick() },
                enabled = !messageInputState.message.isNullOrBlank()
                        || messageInputState.localAttachments.isNotEmpty()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (messageInputState.isEditing) "Update" else "Send"
                )
            }
        }
    }

    if (showMediaBottomSheet) {
        MediaBottomSheet(
            mediaState = mediaState.value,
            onMediaSelected = { uri, type ->
                val newAttachment = LocalAttachment(
                    uri = uri,
                    mediaType = type
                )

                onAddAttachment(newAttachment)
                showMediaBottomSheet = false
            },
            onDismiss = { showMediaBottomSheet = false },
            onRequestPermission = { mediaType ->
                mediaViewModel.loadMedia(context, mediaType)
            },
            context = context
        )
    }
}

@Composable
fun messageInputColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)