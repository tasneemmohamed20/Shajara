package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepositoryProtocol
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val unreadCount: Int get() = _notifications.value.count { !it.isRead }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = notificationsRepository.getNotifications()) {
                    is AppResult.Success -> _notifications.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    is AppResult.Loading -> Unit
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }
}
