package com.example.testapp.presentation.main.maincontent

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.domain.models.chat.ChatDisplayData
import com.example.testapp.utils.Converter.ChatAvatar
import com.example.testapp.utils.Converter.formatTimestamp
import com.example.testapp.utils.MarkdownString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: ChatDisplayData,
    currentUserId: String,
    onChatClick: (String) -> Unit,
    onLongClick: (String) -> Unit
) {
    Log.d("ChatItem", "ChatItem: $chat")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onChatClick(chat.chatId) },
                onLongClick = { onLongClick(chat.chatId) }
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChatAvatar(
            avatarUrl = chat.avatarUrl ?: "Shouldn't happen",
            chatType = chat.chatTypeId,
            modifier = Modifier.size(56.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = chat.lastMessage?.let { formatTimestamp(it.createdAt) } ?: "",
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayMessage = when {
                    chat.chatTypeId == 1 -> chat.lastMessage?.message
                    chat.lastMessage != null -> "${chat.senderName}: ${chat.lastMessage.message}"
                    else -> ""
                }

                if (displayMessage != null) {
                    Text(
                        text = MarkdownString.parseMarkdown(displayMessage, color = MaterialTheme.colorScheme.primary),
                        style = TextStyle(fontWeight = FontWeight.Light),
                        maxLines = 1,
                        fontSize = 12.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if(chat.unreadCount != 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                if (currentUserId == chat.lastMessage?.senderId) {
                    Icon(
                        painter = painterResource(
                            id = if (chat.lastMessage.isRead) R.drawable.ic_check else R.drawable.ic_send
                        ),
                        contentDescription = if (chat.lastMessage.isRead) {
                            "Message read"
                        } else {
                            "Message sent"
                        },
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}