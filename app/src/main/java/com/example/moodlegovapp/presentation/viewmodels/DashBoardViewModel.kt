package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.UserCertificate
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val userRepository: UserRepositoryProtocol,
    private val coursesRepository: CoursesRepositoryProtocol,
    private val notificationsRepository: NotificationsRepositoryProtocol,
    private val certificatesRepository: CertificatesRepositoryProtocol
) : ViewModel() {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user

    // Full enrolled courses. Use this for All Training only.
    private val _allEnrolledCourses = MutableStateFlow<List<Course>>(emptyList())
    val allEnrolledCourses: StateFlow<List<Course>> = _allEnrolledCourses

    // Backward-compatible alias used by older UI code.
    val enrolledCourses: StateFlow<List<Course>> = allEnrolledCourses

    // Only courses with completion conditions are shown in Continue Training.
    val continueTrainingCourses: StateFlow<List<Course>> = _allEnrolledCourses
        .map { courses ->
            courses.filter { course ->
                course.showCompletionConditions == true && course.hidden != true
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _trainingEvents = MutableStateFlow<List<TrainingEvent>>(emptyList())
    val trainingEvents: StateFlow<List<TrainingEvent>> = _trainingEvents

    private val _certificates = MutableStateFlow<List<UserCertificate>>(emptyList())
    val certificates: StateFlow<List<UserCertificate>> = _certificates

    private val _leaderboard = MutableStateFlow<LeaderboardData?>(null)
    val leaderboard: StateFlow<LeaderboardData?> = _leaderboard

    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.read } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val activeCoursesCount: StateFlow<Int> = _allEnrolledCourses
        .map { courses ->
            courses.count { course ->
                course.showCompletionConditions == true &&
                        course.hidden != true &&
                        course.completed != true &&
                        ((course.progress ?: 0) < 100)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val completedCoursesCount: StateFlow<Int> = _allEnrolledCourses
        .map { courses -> courses.count { it.completed == true || ((it.progress ?: 0) >= 100) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val dueActivitiesCount: StateFlow<Int> = _trainingEvents
        .map { events ->
            events.count { event ->
                event.type.equals("due", ignoreCase = true) ||
                        event.type.equals("assignment", ignoreCase = true) ||
                        event.moduleName.equals("assign", ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val averageProgress: StateFlow<Int> = _allEnrolledCourses
        .map { courses ->
            if (courses.isEmpty()) 0 else courses.map { it.progress ?: 0 }.average().toInt()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val now = System.currentTimeMillis() / 1000L
                val oneMonthFromNow = now + 31L * 24L * 60L * 60L

                val userDeferred = async { userRepository.getUserProfile() }
                val coursesDeferred = async { coursesRepository.getEnrolledCourses() }
                val eventsDeferred = async {
                    notificationsRepository.getActionEventsByTimesort(
                        from = now,
                        to = oneMonthFromNow,
                        limit = 50
                    )
                }
                val notificationsDeferred = async { notificationsRepository.getNotifications() }
                val certificatesDeferred = async { certificatesRepository.getCertificates() }

                when (val result = userDeferred.await()) {
                    is AppResult.Success -> _user.value = result.data
                    is AppResult.Failure -> setFirstError(result.error.errorDescription)
                    AppResult.Loading -> Unit
                }

                val courses = when (val result = coursesDeferred.await()) {
                    is AppResult.Success -> {
                        _allEnrolledCourses.value = result.data
                        result.data
                    }
                    is AppResult.Failure -> {
                        setFirstError(result.error.errorDescription)
                        emptyList()
                    }
                    AppResult.Loading -> emptyList()
                }

                when (val result = eventsDeferred.await()) {
                    is AppResult.Success -> _trainingEvents.value = result.data.sortedBy { it.rawTimeStart }
                    is AppResult.Failure -> setFirstError(result.error.errorDescription)
                    AppResult.Loading -> Unit
                }

                when (val result = notificationsDeferred.await()) {
                    is AppResult.Success -> _notifications.value = result.data
                    is AppResult.Failure -> setFirstError(result.error.errorDescription)
                    AppResult.Loading -> Unit
                }

                when (val result = certificatesDeferred.await()) {
                    is AppResult.Success -> _certificates.value = result.data.map { it.toUserCertificate() }
                    is AppResult.Failure -> setFirstError(result.error.errorDescription)
                    AppResult.Loading -> Unit
                }

                loadLeaderboard(courses)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = loadAll()

    fun markNotificationRead(notificationId: Int) {
        viewModelScope.launch {
            when (val result = notificationsRepository.markAsRead(notificationId)) {
                is AppResult.Success -> {
                    _notifications.value = _notifications.value.map { notification ->
                        if (notification.id == notificationId) notification.copy(read = true) else notification
                    }
                }
                is AppResult.Failure -> setFirstError(result.error.errorDescription)
                AppResult.Loading -> Unit
            }
        }
    }

    private suspend fun loadLeaderboard(courses: List<Course>) {
        val leaderboardCourseId = courses.firstOrNull { it.id == 82 }?.id
            ?: courses.firstOrNull()?.id
            ?: 82

        when (val result = userRepository.getLeaderboard(leaderboardCourseId)) {
            is AppResult.Success -> _leaderboard.value = result.data
            is AppResult.Failure -> setFirstError(result.error.errorDescription)
            AppResult.Loading -> Unit
        }
    }

    private fun setFirstError(message: String) {
        if (_errorMessage.value == null) _errorMessage.value = message
    }

    private fun Certificate.toUserCertificate(): UserCertificate {
        val dateText = completionDate.toLongOrNull()?.let { timestamp ->
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp * 1000L))
        } ?: completionDate.ifBlank { "N/A" }

        return UserCertificate(
            id = id.toString(),
            courseName = courseTitle.ifBlank { "Certificate" },
            instructorName = instructorName.ifBlank { "N/A" },
            status = "completed",
            approvalStatus = "Completed",
            completedAtFormatted = dateText,
            viewUrl = certificateUrl.takeIf { it.isNotBlank() },
            downloadUrl = certificateUrl.takeIf { it.isNotBlank() },
            isAvailable = certificateUrl.isNotBlank(),
            pendingMessage = null
        )
    }
}
