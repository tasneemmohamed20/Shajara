package com.example.moodlegovapp.domain.repositoryinterface

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

interface AuthRepositoryProtocol {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
    suspend fun logout()
//    val isLoggedIn: Boolean
}

interface UserRepositoryProtocol {
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
    suspend fun getBadges(): AppResult<List<Badge>>
    suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData>
    suspend fun getTrainingStats(): AppResult<TrainingStats>
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

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

interface NotificationsRepositoryProtocol {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markAsRead(notificationId: Int): AppResult<Unit>
}

interface CertificatesRepositoryProtocol {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getDownloadUrl(certificateId: Int): AppResult<String>
}