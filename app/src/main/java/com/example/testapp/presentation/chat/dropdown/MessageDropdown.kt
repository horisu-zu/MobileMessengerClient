package com.example.testapp.presentation.chat.dropdown

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.domain.models.chat.ChatType
import com.example.testapp.domain.models.chat.GroupRole
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.templates.MenuItem

@Composable
fun MessageDropdown(
    chatType: ChatType,
    currentUserRole: GroupRole,
    reactionUrls: List<String>,
    currentUserId: String,
    expanded: Boolean,
    offset: Offset,
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

    DropdownMenu(
        expanded = expanded,
        offset = DpOffset(offset.x.dp, offset.y.dp),
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        ReactionGrid(
            reactions = reactionUrls,
            onReactionSelected = { reactionUrl ->
                Log.d("MessageDropdown", "Clicked on reaction: $reactionUrl in message: " +
                        messageData.messageId)
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