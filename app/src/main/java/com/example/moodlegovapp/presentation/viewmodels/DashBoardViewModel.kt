package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.User
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepositoryProtocol,
    private val coursesRepository: CoursesRepositoryProtocol,
    private val notificationsRepository: NotificationsRepositoryProtocol
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _enrolledCourses = MutableStateFlow<List<Course>>(emptyList())
    val enrolledCourses: StateFlow<List<Course>> = _enrolledCourses

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val userDeferred          = async { userRepository.getUserProfile() }
                val coursesDeferred       = async { coursesRepository.getEnrolledCourses() }
                val notificationsDeferred = async { notificationsRepository.getNotifications() }

                when (val result = userDeferred.await()) {
                    is AppResult.Success -> _user.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    else -> Unit
                }

                when (val result = coursesDeferred.await()) {
                    is AppResult.Success -> _enrolledCourses.value = result.data
                    is AppResult.Failure -> if (_errorMessage.value == null) _errorMessage.value = result.error.errorDescription
                    else -> Unit
                }

                when (val result = notificationsDeferred.await()) {
                    is AppResult.Success -> _notifications.value = result.data
                    is AppResult.Failure -> if (_errorMessage.value == null) _errorMessage.value = result.error.errorDescription
                    else -> Unit
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = loadAll()

    fun markNotificationRead(notificationId: Int) {
        viewModelScope.launch {
            notificationsRepository.markAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }
}