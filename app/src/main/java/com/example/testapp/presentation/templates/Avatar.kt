package com.example.testapp.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.testapp.utils.shimmerEffect

@Composable
fun Avatar(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    isGroupChat: Boolean = false,
    onClick: (() -> Unit)? = null,
    userStatus: Boolean? = null
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(if(isGroupChat) RoundedCornerShape(12.dp) else CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            loading = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
        )

        if (userStatus != null) {
            StatusIndicator(
                userStatus = userStatus,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(4.dp, 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (userStatus) Color.Green else Color.Gray,
                        CircleShape
                    )
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        }
    }
}


@Composable
fun StatusIndicator(
    userStatus: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = when (userStatus) {
        true -> Color.Green
        false -> Color.Gray
    }

    Box(
        modifier = modifier
            .background(statusColor)
    )
}