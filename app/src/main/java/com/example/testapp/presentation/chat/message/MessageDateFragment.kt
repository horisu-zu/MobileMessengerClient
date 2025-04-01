package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.domain.models.message.Message
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MessageDateFragment(
    isCurrentUser: Boolean,
    messageData: Message,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentWidth()
    ) {
        if(messageData.isTranslated) {
            Box(
                modifier = Modifier.size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_translate),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        Row(
            modifier = modifier.wrapContentWidth(),
            horizontalArrangement = if (isCurrentUser) {
                Arrangement.spacedBy(4.dp, Alignment.End)
            } else {
                Arrangement.spacedBy(4.dp, Alignment.Start)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = messageData.createdAt.formatDate(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier = Modifier.alignByBaseline()
            )

            if (isCurrentUser) {
                Icon(
                    painter = painterResource(
                        id = if (messageData.isRead) R.drawable.ic_check else R.drawable.ic_send
                    ),
                    contentDescription = if (messageData.isRead) "Read" else "Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .alignByBaseline()
                )
            }
        }
    }
}

fun Instant.formatDate(): String {
    return DateTimeFormatter
        .ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(this)
}