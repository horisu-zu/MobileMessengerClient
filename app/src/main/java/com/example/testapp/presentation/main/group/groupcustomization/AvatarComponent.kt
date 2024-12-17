package com.example.testapp.presentation.main.group.groupcustomization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.testapp.R

@Composable
fun AvatarComponent(
    avatar: String,
    onAvatarClick: () -> Unit,
    isClickable: Boolean
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .pointerInput(isClickable) {
                if (isClickable) {
                    detectTapGestures(onTap = { onAvatarClick() })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (avatar.isNotEmpty()) {
            AsyncImage(
                model = avatar,
                contentDescription = "Chat Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_photo),
                contentDescription = "Select Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface), CircleShape)
                    .padding(8.dp)
            )
        }
    }
}