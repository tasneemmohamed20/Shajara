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
import kotlinx.coroutines.flow.StateFlow
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val userDeferred = async { userRepository.getUserProfile() }
                val coursesDeferred = async { coursesRepository.getEnrolledCourses() }
                val notificationsDeferred = async { notificationsRepository.getNotifications() }

                val userResult = userDeferred.await()
                val coursesResult = coursesDeferred.await()
                val notificationsResult = notificationsDeferred.await()

                if (userResult is AppResult.Success) {
                    _user.value = userResult.data
                }

                if (coursesResult is AppResult.Success) {
                    _enrolledCourses.value = coursesResult.data
                }

                if (notificationsResult is AppResult.Success) {
                    _notifications.value = notificationsResult.data
                }

            } finally {
                _isLoading.value = false
            }
        }
    }
}