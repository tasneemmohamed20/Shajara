package com.example.moodlegovapp.data.network

import android.util.Log
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

private const val TAG = "FallbackApiService"

fun AppError.shouldFallbackToLocalMock(): Boolean = when (this) {
    is AppError.NetworkError  -> true
    is AppError.NotFound      -> true
    is AppError.DecodingError -> true
    is AppError.ServerError   -> code in 500..599
    is AppError.Unauthorized  -> NetworkConfig.USE_REMOTE_MOCK
    else                      -> false
}

/**
 * Tries [network] first; on recoverable failures delegates to [localMock].
 */
class FallbackApiService(
    private val network: RealApiService,
    private val localMock: MockApiService
) : ApiServiceProtocol {

    private suspend fun <T> withFallback(
        operation: String,
        networkCall: suspend () -> AppResult<T>,
        mockCall: suspend () -> AppResult<T>
    ): AppResult<T> {
        return when (val result = networkCall()) {
            is AppResult.Success -> result
            is AppResult.Failure -> {
                if (result.error.shouldFallbackToLocalMock()) {
                    Log.w(TAG, "$operation network failed (${result.error}) — using local mock")
                    mockCall()
                } else {
                    result
                }
            }
            is AppResult.Loading -> result
        }
    }

    override suspend fun login(username: String, password: String): AppResult<AuthToken> =
        withFallback("login", { network.login(username, password) }, { localMock.login(username, password) })

    override suspend fun getUserProfile(): AppResult<UserProfile> =
        withFallback("getUserProfile", { network.getUserProfile() }, { localMock.getUserProfile() })

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> =
        withFallback("getPerformanceOverview", { network.getPerformanceOverview() }, { localMock.getPerformanceOverview() })

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> =
        withFallback("getEnrolledCourses", { network.getEnrolledCourses() }, { localMock.getEnrolledCourses() })

    override suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>> =
        withFallback("getCourseContents", { network.getCourseContents(courseId) }, { localMock.getCourseContents(courseId) })

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> =
        withFallback("getCourseModules", { network.getCourseModules(courseId) }, { localMock.getCourseModules(courseId) })

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> =
        withFallback("getCourseResources", { network.getCourseResources(courseId) }, { localMock.getCourseResources(courseId) })

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> =
        withFallback("getAssignments", { network.getAssignments(courseId) }, { localMock.getAssignments(courseId) })

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> =
        withFallback("getAssignmentDetail", { network.getAssignmentDetail(assignmentId) }, { localMock.getAssignmentDetail(assignmentId) })

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> =
        withFallback("submitAssignment", { network.submitAssignment(submission) }, { localMock.submitAssignment(submission) })

    override suspend fun getNotifications(): AppResult<List<Notification>> =
        withFallback("getNotifications", { network.getNotifications() }, { localMock.getNotifications() })

    override suspend fun markNotificationRead(notificationId: Int): AppResult<Unit> =
        withFallback("markNotificationRead", { network.markNotificationRead(notificationId) }, { localMock.markNotificationRead(notificationId) })

    override suspend fun getCertificates(): AppResult<List<Certificate>> =
        withFallback("getCertificates", { network.getCertificates() }, { localMock.getCertificates() })

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> =
        withFallback("getCertificateDownloadUrl", { network.getCertificateDownloadUrl(certificateId) }, { localMock.getCertificateDownloadUrl(certificateId) })

    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> =
        withFallback("getLeaderboard", { network.getLeaderboard(courseId) }, { localMock.getLeaderboard(courseId) })

    override suspend fun getBadges(): AppResult<List<Badge>> =
        withFallback("getBadges", { network.getBadges() }, { localMock.getBadges() })

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> =
        withFallback("getUpcomingEvents", { network.getUpcomingEvents() }, { localMock.getUpcomingEvents() })

    override suspend fun getTrainingStats(): AppResult<TrainingStats> =
        withFallback("getTrainingStats", { network.getTrainingStats() }, { localMock.getTrainingStats() })

    override suspend fun searchCourses(query: String): AppResult<List<Course>> =
        withFallback("searchCourses", { network.searchCourses(query) }, { localMock.searchCourses(query) })

    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> =
        withFallback(
            "updateActivityCompletion",
            { network.updateActivityCompletion(activityId, completed) },
            { localMock.updateActivityCompletion(activityId, completed) }
        )
}
