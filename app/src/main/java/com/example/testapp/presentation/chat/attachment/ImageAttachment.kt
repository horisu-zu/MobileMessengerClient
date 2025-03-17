package com.example.testapp.presentation.chat.attachment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.testapp.domain.models.message.Attachment

@Composable
fun ImageAttachment(
    attachment: Attachment,
    modifier: Modifier = Modifier
) {
    var isFullScreen by remember { mutableStateOf(false) }

    AsyncImage(
        model = attachment.url,
        contentDescription = attachment.name,
        modifier = modifier.clickable { isFullScreen = true },
        contentScale = ContentScale.Crop
    )

    if(isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->
                    scale = (scale * zoomChange).coerceIn(1f, 3f)

                    val maxX = (scale - 1) * 500
                    val maxY = (scale - 1) * 500

                    offset = Offset(
                        x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                        y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                    )
                }

                AsyncImage(
                    model = attachment.url,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(transformableState)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )

                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .padding(top = 8.dp, start = 12.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Full Screen View",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}