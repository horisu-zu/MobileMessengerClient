package com.example.testapp.presentation.chat.attachment

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    context: Context,
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
                    )
                }
                type.startsWith("video/") -> {
                    VideoAttachment(
                        attachment = attachment,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}