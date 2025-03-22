package com.example.testapp.presentation.chat.attachment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.models.message.Attachment
import com.example.testapp.presentation.chat.message.attachmentShape

@Composable
fun AttachmentFragment(
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    hasMessageText: Boolean,
    hasReply: Boolean,
    attachments: List<Attachment> = emptyList()
) {
    Column(
        modifier = modifier
    ) {
        attachments.forEachIndexed { index, attachment ->
            val type = attachment.fileType

            val shape = attachmentShape(
                isCurrentUser = isCurrentUser,
                isFirstInGroup = isFirstInGroup,
                isLastInGroup = isLastInGroup,
                hasMessageText = hasMessageText,
                hasReply = hasReply
            )

            when  {
                type.startsWith("image/") -> {
                    ImageAttachment(
                        attachment = attachment,
                        shape = shape,
                        modifier = Modifier
                    )
                }
                type.startsWith("audio/") -> {
                    AudioAttachment(
                        attachment = attachment,
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                type.startsWith("video/") -> {
                    VideoAttachment(
                        attachment = attachment,
                        modifier = Modifier.padding(2.dp)
                            .clip(shape)
                    )
                }
            }
        }
    }
}