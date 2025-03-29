package com.example.testapp.presentation.chat.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.domain.models.message.Message
import com.example.testapp.utils.Converter.getAttachmentDescription
import com.example.testapp.utils.UserColorGenerator.toColor

@Composable
fun ReplyFragment(
    replyMessage: Message,
    modifier: Modifier = Modifier,
    attachments: List<Attachment>? = emptyList(),
    userData: UserResponse,
    onReplyClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                userData.userColor
                    .toColor()
                    .copy(alpha = 0.2f)
            )
            .clickable(onClick = onReplyClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        VerticalDivider(
            thickness = 4.dp,
            color = userData.userColor.toColor(),
            modifier = Modifier
                .height(42.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = userData.nickname,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = userData.userColor.toColor(),
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if(replyMessage.message != null) {
                Text(
                    text = replyMessage.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                attachments?.let {
                    Text(
                        text = getAttachmentDescription(attachments),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}