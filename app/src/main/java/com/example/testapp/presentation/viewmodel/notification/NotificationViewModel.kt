package com.example.testapp.presentation.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.models.notification.InAppNotification
import com.example.testapp.domain.models.notification.NotificationBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(): ViewModel() {

    private val _activeNotifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val activeNotifications = _activeNotifications.asStateFlow()

    private val maxVisibleNotifications = 3

    init {
        viewModelScope.launch {
            NotificationBus.events.collect { event ->
                addNotification(notification = event)
            }
        }
    }

    private fun addNotification(notification: InAppNotification) {
        val currentNotifications = _activeNotifications.value.toMutableList()

        if(currentNotifications.size >= maxVisibleNotifications) {
            val oldestNotification = currentNotifications.minBy { it.timestamp }
            oldestNotification.let { currentNotifications.remove(it) }
        }

        currentNotifications.add(notification)
        _activeNotifications.value = currentNotifications
    }

    fun dismissNotification(notificationId: Int) {
        val currentNotifications = _activeNotifications.value.toMutableList()
        currentNotifications.removeIf { it.notificationId == notificationId }
        _activeNotifications.value = currentNotifications
    }
}