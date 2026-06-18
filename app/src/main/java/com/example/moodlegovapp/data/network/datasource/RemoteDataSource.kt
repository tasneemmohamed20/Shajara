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
import com.example.moodlegovapp.domain.models.AssignmentsResponse
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection

import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.CourseResourcesResponse
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.LeaderboardResponse
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.SubmissionSaveResponse
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.models.toUserProfile

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
        return when (val result = NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getUserByField(value = username)
        }) {
            is AppResult.Success -> {
                if (result.data.isNotEmpty()) {
                    AppResult.Success(AuthToken("b4cd92a9bbb816fc54ae1a43a01d1dcc", null))
                } else {
                    AppResult.Failure(AppError.NetworkError("User not found"))
                }
            }
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    // ── USER ──────────────────────────────────────────────────────

    override suspend fun getUserProfile(): AppResult<UserProfile> {
        Log.d(TAG, "getUserProfile: Initiating fetch for User ID: ${getCurrentUserId()}")

        val username = dataStoreManager.get<String>(DataStoreManager.Companion.KEY_USERNAME) ?: "test.student1"
        return when (val result = NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getUserByField(value = username)
        }) {
            is AppResult.Success -> {
                val studentUser = result.data.firstOrNull()
                if (studentUser != null) {
                    // Save the user ID from the API call
                    studentUser.id?.let {
                        dataStoreManager.save(DataStoreManager.Companion.KEY_USER_ID, it.toString())
                    }
                    AppResult.Success(studentUser.toUserProfile())
                } else {
                    AppResult.Failure(AppError.DecodingError)
                }
            }
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getPerformanceOverview(getCurrentUserId())
        }
    }

    // ── COURSES ───────────────────────────────────────────────────

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        val cachedId = dataStoreManager.userIdState.value?.toIntOrNull()
        val finalUserId = if (cachedId != null && cachedId != 101) {
            cachedId
        } else {
            val profileResult = getUserProfile()
            if (profileResult is AppResult.Success) profileResult.data.id else 101
        }
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getEnrolledCourses(finalUserId)
        }
    }



    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getCourseModules(courseId)
        }
    }

    override suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>> {
        return NetworkCallHandler.safeCall(retryPolicy) {
            retrofit.getCourseContents(courseId)
        }
    }
// ── COURSES ───────────────────────────────────────────────────

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return when (val result = NetworkCallHandler.safeCall<CourseResourcesResponse>(retryPolicy) {
            retrofit.getCourseResources(courseId, getCurrentUserId())  // fix: missing userId param
        }) {
            is AppResult.Success -> result.data.data?.resources?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)           // fix: unwrap .data.resources
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

// ── ASSIGNMENTS ───────────────────────────────────────────────

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        return when (val result = NetworkCallHandler.safeCall<AssignmentsResponse>(retryPolicy) {
            retrofit.getAssignmentsByCourse(getCurrentUserId(), courseId) // fix: use correct endpoint
        }) {
            is AppResult.Success -> result.data.data?.assignments?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)             // fix: unwrap .data.assignments
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        // fix: no single-assignment endpoint exists in RetrofitApiService;
        // fetch all and find by id as a workaround
        return when (val result = NetworkCallHandler.safeCall<AssignmentsResponse>(retryPolicy) {
            retrofit.getAssignments(getCurrentUserId())
        }) {
            is AppResult.Success -> result.data.data?.assignments
                ?.find { it.id == assignmentId }
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        // fix: no submitAssignment endpoint exists; use saveSubmission instead
        return when (NetworkCallHandler.safeCall<SubmissionSaveResponse>(retryPolicy) {
            retrofit.saveSubmission(submission.assignmentId, getCurrentUserId(), submission)
        }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> AppResult.Failure(AppError.Unknown)
            is AppResult.Loading -> AppResult.Loading
        }
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
                else result.data.filter { it.fullName?.contains(query, ignoreCase = true) == true })

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


