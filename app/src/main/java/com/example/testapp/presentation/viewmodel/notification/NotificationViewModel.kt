package com.example.testapp.presentation.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.models.notification.InAppNotification
import com.example.testapp.domain.models.notification.NotificationBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor() : ViewModel() {

    private val _activeNotifications = MutableStateFlow<Map<Int, InAppNotification>>(emptyMap())
    val activeNotifications = _activeNotifications.asStateFlow()

    private val _timers = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val timers = _timers.asStateFlow()

    private val maxVisibleNotifications = 3

    init {
        viewModelScope.launch {
            NotificationBus.events.collect { event ->
                addNotification(event)
            }
        }
    }

    private fun addNotification(notification: InAppNotification) {
        val id = notification.notificationId
        _activeNotifications.value += (id to notification)

        viewModelScope.launch {
            val duration = 3000L
            val interval = 25L
            val steps = duration / interval

            for (i in steps downTo 0) {
                _timers.value += (id to i / steps.toFloat())
                delay(interval)
            }

            delay(300)
            dismissNotification(id)
        }

        if (_activeNotifications.value.size > maxVisibleNotifications) {
            val oldestId = _activeNotifications.value.values.minByOrNull { it.timestamp }?.notificationId
            oldestId?.let { dismissNotification(it) }
        }
    }

    fun dismissNotification(notificationId: Int) {
        _activeNotifications.value -= notificationId
        _timers.value -= notificationId
    }
}