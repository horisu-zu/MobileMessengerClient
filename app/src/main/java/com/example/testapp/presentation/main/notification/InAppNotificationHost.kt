package com.example.testapp.presentation.main.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.viewmodel.notification.NotificationViewModel

@Composable
fun InAppNotificationHost(
    onNotificationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val activeNotifications by notificationViewModel.activeNotifications.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        activeNotifications.forEach { notification ->
            key(notification.notificationId) {
                InAppNotificationItem(
                    notification = notification,
                    onClick = onNotificationClick,
                    onExpire = { notificationId ->
                        notificationViewModel.dismissNotification(notificationId)
                    }
                )
            }
        }
    }
}