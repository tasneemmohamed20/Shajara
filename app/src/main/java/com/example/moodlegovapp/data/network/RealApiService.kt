package com.example.moodlegovapp.data.network

import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentsResponse
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseDetail
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.CourseResourcesResponse
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.SubmissionSaveResponse
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.models.UserResponse
import com.example.moodlegovapp.domain.models.toUserProfile

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
        return when (val result = safeCall { retrofit.getUserByField(value = username) }) {
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

    // ── USER ──────────────────────────────────
    override suspend fun getUserProfile(): AppResult<UserProfile> {
        val username = dataStoreManager.get<String>(DataStoreManager.Companion.KEY_USERNAME) ?: "test.student1"
        return when (val result = safeCall { retrofit.getUserByField(value = username) }) {
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
        return safeCall { retrofit.getPerformanceOverview(userId()) }
    }

    // ── COURSES ───────────────────────────────
    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        val cachedId = dataStoreManager.userIdState.value?.toIntOrNull()
        val finalUserId = if (cachedId != null && cachedId != 101) {
            cachedId
        } else {
            val profileResult = getUserProfile()
            if (profileResult is AppResult.Success) profileResult.data.id else 101
        }
        return safeCall { retrofit.getEnrolledCourses(finalUserId) }
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


    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        return when (val r = safeCall<AssignmentsResponse> {
            if (courseId <= 0) retrofit.getAssignments(userId())
            else               retrofit.getAssignmentsByCourse(userId(), courseId)
        }) {
            is AppResult.Success -> AppResult.Success(r.data.data?.assignments ?: emptyList())
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        // No dedicated detail endpoint on the mock server — pull list and filter.
        return when (val r = getAssignments(-1)) {
            is AppResult.Success -> r.data.find { it.id == assignmentId }
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        return when (val result = safeCall<SubmissionSaveResponse> {
            retrofit.saveSubmission(submission.assignmentId, userId(), submission)
        }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result  // propagate
            is AppResult.Loading -> AppResult.Loading
        }
    }

// ── COURSE RESOURCES ──────────────────────────────────────────────────────────

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return when (val r = safeCall<CourseResourcesResponse> {
            retrofit.getCourseResources(courseId, userId())
        }) {
            is AppResult.Success -> AppResult.Success(r.data.data?.resources ?: emptyList())
            is AppResult.Failure -> r
            is AppResult.Loading -> AppResult.Loading
        }
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
                else result.data.filter { it.fullName?.contains(query, ignoreCase = true) == true }
            )
            else -> result
        }
    }

    // ── ACTIVITY COMPLETION ───────────────────
    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}