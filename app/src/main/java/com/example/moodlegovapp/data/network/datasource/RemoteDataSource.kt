package com.example.moodlegovapp.data.network.datasource

import android.util.Log
import com.example.moodlegovapp.data.network.AppError
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseDetail
import com.example.moodlegovapp.domain.models.CourseDetailsResponse
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.LeaderboardResponse
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

/**
 * Remote data source implementation that wraps Retrofit calls.
 * Implements all data source interfaces using a single Retrofit service.
 */
class RemoteDataSource(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager,
    private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT
) : AuthDataSource, UserDataSource, CoursesDataSource, AssignmentsDataSource,
    NotificationsDataSource, CertificatesDataSource, LeaderboardDataSource, BadgesDataSource,
    EventsDataSource, StatsDataSource, SearchDataSource, ActivityDataSource {

    private fun getCurrentUserId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101
    private val TAG = "RemoteDataSource"
    // ── AUTH ──────────────────────────────────────────────────────

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.login(username, password)
        }
    }

    // ── USER ──────────────────────────────────────────────────────

    override suspend fun getUserProfile(): AppResult<UserProfile> {
        Log.d(TAG, "getUserProfile: Initiating fetch for User ID: ${getCurrentUserId()}")

        return when (val result = NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getUserProfile(getCurrentUserId())
        }) {
            is AppResult.Success -> {
                val userProfile = result.data.data
                if (userProfile != null) {
                    Log.d(
                        TAG,
                        "getUserProfile: Success! Received profile for ${userProfile.fullName} (ID: ${userProfile.id})"
                    )
                    AppResult.Success(userProfile)
                } else {
                    Log.e(
                        TAG,
                        "getUserProfile: Failure - API payload field 'data' was null (DecodingError)"
                    )
                    AppResult.Failure(AppError.DecodingError)
                }
            }

            is AppResult.Failure -> {
                Log.e(
                    TAG, "getUserProfile: Network or Server Failure. Error Details: ${result.error}"
                )
                result
            }

            is AppResult.Loading -> {
                Log.d(TAG, "getUserProfile: Request is loading/retrying...")
                AppResult.Loading
            }
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getPerformanceOverview(getCurrentUserId())
        }
    }

    // ── COURSES ───────────────────────────────────────────────────

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getEnrolledCourses(getCurrentUserId())
        }
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<CourseDetail> {
        return when (val result = NetworkCallHandler.safeCall<CourseDetailsResponse>(retryPolicy) {
            retrofit.getCourseDetail(courseId)
        }) {
            is AppResult.Success -> result.data.data?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)

            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getCourseModules(courseId)
        }
    }

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getCourseResources(courseId)
        }
    }

    // ── ASSIGNMENTS ───────────────────────────────────────────────

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getAssignments(courseId)
        }
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getAssignmentDetail(assignId = assignmentId)
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    // ── NOTIFICATIONS ─────────────────────────────────────────────

    override suspend fun getNotifications(): AppResult<List<Notification>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getNotifications(getCurrentUserId())
        }
    }

    override suspend fun markNotificationRead(notificationId: Int): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    // ── CERTIFICATES ───────────────────────────────────────────────

    override suspend fun getCertificates(): AppResult<List<Certificate>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getCertificates(getCurrentUserId())
        }
    }

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> {
        return AppResult.Success("https://mock.gov.ae/certificates/$certificateId.pdf")
    }

    // ── LEADERBOARD ────────────────────────────────────────────────

    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> {
        return when (val result = NetworkCallHandler.safeCall<LeaderboardResponse>(retryPolicy) {
            retrofit.getLeaderboard(courseId)
        }) {
            is AppResult.Success -> result.data.data?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)

            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── BADGES ─────────────────────────────────────────────────────

    override suspend fun getBadges(): AppResult<List<Badge>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getBadges(getCurrentUserId())
        }
    }

    // ── EVENTS ─────────────────────────────────────────────────────

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getUpcomingEvents(getCurrentUserId())
        }
    }

    // ── STATS ──────────────────────────────────────────────────────

    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getTrainingStats(getCurrentUserId())
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────

    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        val result = getEnrolledCourses()
        return when (result) {
            is AppResult.Success -> AppResult.Success(
                if (query.isBlank()) result.data
                else result.data.filter { it.title.contains(query, ignoreCase = true) })

            else -> result
        }
    }

    // ── ACTIVITY COMPLETION ───────────────────────────────────────

    override suspend fun updateActivityCompletion(
        activityId: Int, completed: Boolean
    ): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}


