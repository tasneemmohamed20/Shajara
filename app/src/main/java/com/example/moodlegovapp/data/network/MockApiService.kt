package com.example.moodlegovapp.data.network

import android.content.Context
import com.example.moodlegovapp.R
import com.example.moodlegovapp.data.network.datasource.ActivityDataSource
import com.example.moodlegovapp.data.network.datasource.AssignmentsDataSource
import com.example.moodlegovapp.data.network.datasource.AuthDataSource
import com.example.moodlegovapp.data.network.datasource.BadgesDataSource
import com.example.moodlegovapp.data.network.datasource.CertificatesDataSource
import com.example.moodlegovapp.data.network.datasource.CoursesDataSource
import com.example.moodlegovapp.data.network.datasource.EventsDataSource
import com.example.moodlegovapp.data.network.datasource.LeaderboardDataSource
import com.example.moodlegovapp.data.network.datasource.NotificationsDataSource
import com.example.moodlegovapp.data.network.datasource.SearchDataSource
import com.example.moodlegovapp.data.network.datasource.StatsDataSource
import com.example.moodlegovapp.data.network.datasource.UserDataSource
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.AssignmentItem
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentsResponse
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
import com.example.moodlegovapp.domain.models.UserResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

class MockApiService(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) : ApiServiceProtocol,
    AuthDataSource,
    UserDataSource,
    CoursesDataSource,
    AssignmentsDataSource,
    NotificationsDataSource,
    CertificatesDataSource,
    LeaderboardDataSource,
    BadgesDataSource,
    EventsDataSource,
    StatsDataSource,
    SearchDataSource,
    ActivityDataSource {

    private val gson = Gson()

    private suspend fun fakeDelay() = delay(400)

    private fun <T> readJson(rawResId: Int, typeToken: TypeToken<T>): T {
        val json = context.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }
        return gson.fromJson(json, typeToken.type)
    }

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        fakeDelay()
        return if (username.isNotBlank() && password.isNotBlank()) {
            AppResult.Success(
                AuthToken(
                    token = "mock_token_${System.currentTimeMillis()}",
                    privateToken = "mock_private_token"
                )
            )
        } else {
            AppResult.Failure(AppError.NetworkError("Invalid username or password"))
        }
    }

    override suspend fun getUserProfile(): AppResult<UserProfile> {
        fakeDelay()
        return try {
            val response = readJson(R.raw.mock_user_profile, object : TypeToken<UserResponse>() {})
            response.data?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_performance_overview, object : TypeToken<PerformanceOverview>() {}))
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_enrolled_courses, object : TypeToken<List<Course>>() {}))
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<CourseDetail> {
        fakeDelay()
        return try {
            val response = readJson(R.raw.mock_course_detail, object : TypeToken<CourseDetailsResponse>() {})
            response.data?.let { detail ->
                AppResult.Success(detail.copy(id = courseId))
            } ?: AppResult.Failure(AppError.DecodingError)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
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
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        fakeDelay()
        return try {
            val all = readJson(R.raw.mock_enrolled_courses, object : TypeToken<List<Course>>() {})
            val filtered = if (query.isBlank()) all
            else all.filter { it.title.contains(query, ignoreCase = true) }
            AppResult.Success(filtered)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getAllUserAssignments(courseId: Int): AppResult<List<AssignmentItem>> {
        fakeDelay()
        return try {
            val response = readJson(R.raw.mock_assignments, object : TypeToken<AssignmentsResponse>() {})
            response.data?.assignments?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        fakeDelay()
        return AppResult.Success(Unit)
    }

    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        fakeDelay()
        return AppResult.Success(Unit)
    }

    override suspend fun getNotifications(): AppResult<List<Notification>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_notifications, object : TypeToken<List<Notification>>() {}))
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun markNotificationRead(notificationId: Int): AppResult<Unit> {
        fakeDelay()
        return AppResult.Success(Unit)
    }

    override suspend fun getCertificates(): AppResult<List<Certificate>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_certificates, object : TypeToken<List<Certificate>>() {}))
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> {
        fakeDelay()
        return AppResult.Success("https://mock.gov.ae/certificates/$certificateId.pdf")
    }

    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> {
        fakeDelay()
        return try {
            val response = readJson(R.raw.mock_leaderboard, object : TypeToken<LeaderboardResponse>() {})
            response.data?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getBadges(): AppResult<List<Badge>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_badges, object : TypeToken<List<Badge>>() {}))
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        fakeDelay()
        return AppResult.Success(
            TrainingStats(
                activeCourses = 3,
                activitiesDue = 2,
                completedCourses = 1
            )
        )
    }
}
