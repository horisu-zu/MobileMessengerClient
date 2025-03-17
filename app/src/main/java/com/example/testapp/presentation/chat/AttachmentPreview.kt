package com.example.testapp.presentation.chat

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.testapp.R
import com.example.testapp.domain.dto.message.LocalAttachment
import com.example.testapp.presentation.templates.media.MediaType
import com.example.testapp.presentation.templates.media.VideoThumbnail
import com.example.testapp.utils.MediaLoader

@Composable
fun AttachmentPreview(
    attachment: LocalAttachment,
    onRemove: () -> Unit,
    context: Context
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 24.dp)
        ) {
            when (attachment.mediaType) {
                MediaType.IMAGES -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(attachment.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image preview",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                MediaType.VIDEO -> {
                    VideoThumbnail(
                        uri = attachment.uri,
                        modifier = Modifier.size(96.dp)
                    )
                }
                MediaType.AUDIO -> {
                    Column(
                        modifier = Modifier.size(96.dp).padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_audio),
                            contentDescription = null,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = MediaLoader.getFileName(context, attachment.uri),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}