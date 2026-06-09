package com.example.moodlegovapp.data.network

import com.example.moodlegovapp.data.service.DataStoreManager
import retrofit2.Response
import  com.example.moodlegovapp.domain.models.Assignment
import  com.example.moodlegovapp.domain.models.AssignmentSubmission
import  com.example.moodlegovapp.domain.models.Notification
import  com.example.moodlegovapp.domain.models.Badge
import  com.example.moodlegovapp.domain.models.Certificate
import  com.example.moodlegovapp.domain.models.Course
import  com.example.moodlegovapp.domain.models.CourseModule
import  com.example.moodlegovapp.domain.models.CourseResource
import  com.example.moodlegovapp.domain.models.LeaderboardEntry
import  com.example.moodlegovapp.domain.models.PerformanceOverview
import  com.example.moodlegovapp.domain.models.TrainingEvent
import  com.example.moodlegovapp.domain.models.TrainingStats
import  com.example.moodlegovapp.domain.models.User
import  com.example.moodlegovapp.domain.models.AuthToken

class RealApiService(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager
) : ApiServiceProtocol {

    private fun userId() = dataStoreManager.userIdState.value?.toInt()?: 101

    // ── Safe call helper ──────────────────────
    // mirrors iOS request<T>() generic function
    private suspend fun <T> safeCall(call: suspend () -> Response<T>): AppResult<T> {
        return try {
            val response = call()
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) AppResult.Success(body)
                    else AppResult.Failure(AppError.DecodingError)
                }
                response.code() == 401 -> AppResult.Failure(AppError.Unauthorized)
                response.code() == 404 -> AppResult.Failure(AppError.NotFound)
                else -> AppResult.Failure(AppError.ServerError(response.code()))
            }
        } catch (e: Exception) {
            AppResult.Failure(AppError.NetworkError(e.localizedMessage ?: "Unknown error"))
        }
    }

    // ── AUTH ──────────────────────────────────
    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        // mirrors iOS: if useMock && useRemoteMock → call /auth/login
        return safeCall { retrofit.login(username, password) }
    }

    // ── USER ──────────────────────────────────
    override suspend fun getUserProfile(): AppResult<User> {
        // mirrors iOS RemoteUser mapping
        return safeCall { retrofit.getUserProfile(userId()) }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return safeCall { retrofit.getPerformanceOverview(userId()) }
    }

    // ── COURSES ───────────────────────────────
    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        return safeCall { retrofit.getEnrolledCourses(userId()) }
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<Course> {
        return safeCall { retrofit.getCourseDetail(courseId) }
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return safeCall { retrofit.getCourseModules(courseId) }
    }

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return safeCall { retrofit.getCourseResources(courseId) }
    }

    // ── ASSIGNMENTS ───────────────────────────
    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        return safeCall { retrofit.getAssignments(courseId) }
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        return safeCall { retrofit.getAssignmentDetail(assignId = assignmentId) }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        // mirrors iOS: return .success(())
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
    override suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>> {
        return safeCall { retrofit.getLeaderboard(courseId) }
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