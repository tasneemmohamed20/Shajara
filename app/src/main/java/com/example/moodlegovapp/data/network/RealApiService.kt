package com.example.moodlegovapp.data.network

import com.example.moodlegovapp.data.service.DataStoreManager

import retrofit2.Response
import  com.example.moodlegovapp.domain.models.AssignmentItem
import  com.example.moodlegovapp.domain.models.AssignmentSubmission
import  com.example.moodlegovapp.domain.models.AssignmentsResponse
import  com.example.moodlegovapp.domain.models.Notification
import  com.example.moodlegovapp.domain.models.Badge
import  com.example.moodlegovapp.domain.models.Certificate
import  com.example.moodlegovapp.domain.models.Course
import  com.example.moodlegovapp.domain.models.CourseDetail
import  com.example.moodlegovapp.domain.models.CourseDetailsResponse
import  com.example.moodlegovapp.domain.models.CourseModule
import  com.example.moodlegovapp.domain.models.CourseResource
import  com.example.moodlegovapp.domain.models.LeaderboardData
import  com.example.moodlegovapp.domain.models.LeaderboardResponse
import  com.example.moodlegovapp.domain.models.PerformanceOverview
import  com.example.moodlegovapp.domain.models.TrainingEvent
import  com.example.moodlegovapp.domain.models.TrainingStats
import  com.example.moodlegovapp.domain.models.UserProfile
import  com.example.moodlegovapp.domain.models.UserResponse
import  com.example.moodlegovapp.domain.models.AuthToken

class RealApiService(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager
) : ApiServiceProtocol {

    private fun userId() = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    // ── Safe call helper ──────────────────────
    private suspend inline fun <T> safeCall(
        crossinline call: suspend () -> retrofit2.Response<T>
    ): AppResult<T> {
        return NetworkCallHandler.executeCall { call() }
    }

    // ── AUTH ──────────────────────────────────
    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        return safeCall { retrofit.login(username, password) }
    }

    // ── USER ──────────────────────────────────
    override suspend fun getUserProfile(): AppResult<UserProfile> {
        return when (val result = safeCall<UserResponse> { retrofit.getUserProfile(userId()) }) {
            is AppResult.Success -> result.data.data?.let { AppResult.Success(it) }
                                    ?: AppResult.Failure(AppError.DecodingError)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return safeCall { retrofit.getPerformanceOverview(userId()) }
    }

    // ── COURSES ───────────────────────────────
    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        return safeCall { retrofit.getEnrolledCourses(userId()) }
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<CourseDetail> {
        return when (val result = safeCall { retrofit.getCourseDetail(courseId) }) {
            is AppResult.Success -> result.data.data?.let { AppResult.Success(it) }
                                    ?: AppResult.Failure(AppError.DecodingError)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return when (val result = getCourseDetail(courseId)) {
            is AppResult.Success -> AppResult.Success(result.data.modules)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return safeCall { retrofit.getCourseResources(courseId) }
    }

    // ── ASSIGNMENTS ───────────────────────────
    override suspend fun getAllUserAssignments(courseId: Int): AppResult<List<AssignmentItem>> {
        return when (val result = safeCall { retrofit.getAllUserAssignments(courseId) }) {
            is AppResult.Success -> {
                val items = result.data.data?.assignments
                if (items != null) AppResult.Success(items)
                else AppResult.Failure(AppError.DecodingError)
            }
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    // ── NOTIFICATIONS ─────────────────────────
    override suspend fun getNotifications(): AppResult<List<Notification>> {
        return safeCall { retrofit.getNotifications(userId()) }
    }

    override suspend fun markNotificationRead(notificationId: Int): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    // ── CERTIFICATES ──────────────────────────
    override suspend fun getCertificates(): AppResult<List<Certificate>> {
        return safeCall { retrofit.getCertificates(userId()) }
    }

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> {
        return AppResult.Success("https://mock.gov.ae/certificates/$certificateId.pdf")
    }

    // ── LEADERBOARD ───────────────────────────
    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> {
        return when (val result = safeCall { retrofit.getLeaderboard(courseId) }) {
            is AppResult.Success -> result.data.data?.let { AppResult.Success(it) }
                                    ?: AppResult.Failure(AppError.DecodingError)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── BADGES ────────────────────────────────
    override suspend fun getBadges(): AppResult<List<Badge>> {
        return safeCall { retrofit.getBadges(userId()) }
    }

    // ── EVENTS ────────────────────────────────
    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        return safeCall { retrofit.getUpcomingEvents(userId()) }
    }

    // ── STATS ─────────────────────────────────
    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        return safeCall { retrofit.getTrainingStats(userId()) }
    }

    // ── SEARCH ────────────────────────────────
    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        val result = getEnrolledCourses()
        return when (result) {
            is AppResult.Success -> AppResult.Success(
                if (query.isBlank()) result.data
                else result.data.filter { it.title.contains(query, ignoreCase = true) }
            )
            else -> result
        }
    }

    // ── ACTIVITY COMPLETION ───────────────────
    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}