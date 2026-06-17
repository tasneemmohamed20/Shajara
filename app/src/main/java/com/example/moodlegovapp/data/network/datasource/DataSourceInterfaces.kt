package com.example.moodlegovapp.data.network.datasource

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseDetail
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

/**
 * Remote data source interfaces following the Data Source pattern.
 * Abstract API calls away from repositories for better testability.
 */

interface AuthDataSource {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
}

interface UserDataSource {
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
}

interface CoursesDataSource {
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getCourseDetail(courseId: Int): AppResult<CourseDetail>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>>
}

interface AssignmentsDataSource {
    suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>
}

interface NotificationsDataSource {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markNotificationRead(notificationId: Int): AppResult<Unit>
}

interface CertificatesDataSource {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String>
}

interface LeaderboardDataSource {
    suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData>
}

interface BadgesDataSource {
    suspend fun getBadges(): AppResult<List<Badge>>
}

interface EventsDataSource {
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

interface StatsDataSource {
    suspend fun getTrainingStats(): AppResult<TrainingStats>
}

interface SearchDataSource {
    suspend fun searchCourses(query: String): AppResult<List<Course>>
}

interface ActivityDataSource {
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}


