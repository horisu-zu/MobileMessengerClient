package com.example.testapp.presentation.main.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.models.notification.InAppNotification
import com.example.testapp.presentation.templates.Avatar
import kotlinx.coroutines.delay

@Composable
fun InAppNotificationItem(
    notification: InAppNotification,
    onClick: (String) -> Unit,
    onExpire: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(1f) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(notification.notificationId) {
        visible = true
        val duration = 3000L
        val interval = 25L
        val steps = duration / interval

        for (i in steps downTo 0) {
            progress = i / steps.toFloat()
            delay(interval)
        }

        visible = false
        delay(300)
        onExpire(notification.notificationId)
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = modifier.fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    onClick(notification.chatId)
                    onExpire(notification.notificationId)
                },
            //.shadow(4.dp, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Avatar(
                    avatarUrl = notification.avatarUrl,
                    modifier = Modifier.size(36.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )
        }
    }
}