package com.example.testapp.presentation.chat.dropdown

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.domain.models.message.Message
import com.example.testapp.presentation.templates.MenuItem

@Composable
fun MessageDropdown(
    isCurrentUser: Boolean,
    expanded: Boolean,
    horizontalOffset: Dp,
    onDismissRequest: () -> Unit,
    messageData: Message,
    onReplyMessage: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    //var reactionsExpanded by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        offset = DpOffset(horizontalOffset, 0.dp),
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
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
        if (isCurrentUser) {
            MenuItem("Delete", R.drawable.ic_delete, {
                messageData.messageId?.let { onDeleteMessage(it) }
                onDismissRequest()
            }, MaterialTheme.colorScheme.error)
        }
    }
}