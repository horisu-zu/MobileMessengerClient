package com.example.testapp.presentation.chat.attachment

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.domain.models.message.Attachment

@Composable
fun AttachmentFragment(
    modifier: Modifier = Modifier,
    attachments: List<Attachment> = emptyList()
) {
    Column(
        modifier = modifier
    ) {
        attachments.forEach { attachment ->
            val type = attachment.fileType

            when  {
                type.startsWith("image/") -> {
                    ImageAttachment(attachment = attachment)
                }
                type.startsWith("audio/") -> {
                    AudioAttachment(attachment = attachment)
                }
                type.startsWith("video/") -> {
                    VideoAttachment( attachment = attachment)
                }
            }
        }
    }
}