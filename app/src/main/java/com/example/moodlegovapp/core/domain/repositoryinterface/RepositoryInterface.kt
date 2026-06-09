package com.example.moodlegovapp.core.domain.repositoryinterface

import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.models.Assignment
import com.example.moodlegovapp.core.domain.models.AssignmentSubmission
import com.example.moodlegovapp.core.domain.models.AuthToken
import com.example.moodlegovapp.core.domain.models.Badge
import com.example.moodlegovapp.core.domain.models.Certificate
import com.example.moodlegovapp.core.domain.models.Course
import com.example.moodlegovapp.core.domain.models.CourseModule
import com.example.moodlegovapp.core.domain.models.CourseResource
import com.example.moodlegovapp.core.domain.models.LeaderboardEntry
import com.example.moodlegovapp.core.domain.models.Notification
import com.example.moodlegovapp.core.domain.models.PerformanceOverview
import com.example.moodlegovapp.core.domain.models.TrainingEvent
import com.example.moodlegovapp.core.domain.models.TrainingStats
import com.example.moodlegovapp.core.domain.models.User

// mirrors iOS AuthRepositoryProtocol
interface AuthRepositoryProtocol {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
    fun logout()
    val isLoggedIn: Boolean
}

// mirrors iOS UserRepositoryProtocol
interface UserRepositoryProtocol {
    suspend fun getUserProfile(): AppResult<User>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
    suspend fun getBadges(): AppResult<List<Badge>>
    suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>>
    suspend fun getTrainingStats(): AppResult<TrainingStats>
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

// mirrors iOS CoursesRepositoryProtocol
interface CoursesRepositoryProtocol {
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getCourseDetail(courseId: Int): AppResult<Course>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>>
    suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>
    suspend fun searchCourses(query: String): AppResult<List<Course>>
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}

// mirrors iOS NotificationsRepositoryProtocol
interface NotificationsRepositoryProtocol {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markAsRead(notificationId: Int): AppResult<Unit>
}

// mirrors iOS CertificatesRepositoryProtocol
interface CertificatesRepositoryProtocol {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getDownloadUrl(certificateId: Int): AppResult<String>
}