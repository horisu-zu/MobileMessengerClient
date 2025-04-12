package com.example.testapp.presentation.chat.dropdown

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.domain.models.chat.ChatType
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.templates.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBottomSheet(
    chatType: ChatType,
    currentUserRole: GroupRole,
    reactionUrls: List<String>,
    currentUserId: String,
    onDismissRequest: () -> Unit,
    messageData: Message,
    onReplyMessage: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onTranslateMessage: (String) -> Unit,
    onAddRestriction: (String) -> Unit,
    onToggleReaction: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = currentUserRole != GroupRole.MEMBER && chatType != ChatType.PERSONAL
    val isCurrentUser = messageData.senderId == currentUserId
    var reactionsExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReactionGrid(
                reactions = reactionUrls,
                onReactionSelected = { reactionUrl ->
                    messageData.messageId?.let { onToggleReaction(it, currentUserId, reactionUrl) }
                    onDismissRequest()
                },
                expanded = reactionsExpanded,
                onExpandToggle = { reactionsExpanded = !reactionsExpanded }
            )
            if (isCurrentUser) {
                MenuItem("Edit", R.drawable.ic_edit, {
                    onEditMessage(messageData)
                    onDismissRequest()
                })
            }
            MenuItem("Reply", R.drawable.ic_reply, {
                onReplyMessage(messageData)
                onDismissRequest()
            })
            if(!isCurrentUser) {
                MenuItem(
                    text = "Translate Message",
                    iconRes = R.drawable.ic_translate,
                    onClick = {
                        onTranslateMessage(messageData.messageId!!)
                        onDismissRequest()
                    }
                )
            }
            if(isAdmin) {
                MenuItem("Add Restriction", R.drawable.ic_restriction, {
                    onAddRestriction(messageData.senderId)
                    onDismissRequest()
                })
            }
            if (isCurrentUser || isAdmin) {
                MenuItem("Delete", R.drawable.ic_delete, {
                    messageData.messageId?.let { onDeleteMessage(it) }
                    onDismissRequest()
                }, MaterialTheme.colorScheme.error)
            }
        }
    }
}