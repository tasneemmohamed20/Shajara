package com.example.moodlegovapp.core.data.network

import android.content.Context
import com.example.moodlegovapp.R
import com.example.moodlegovapp.core.data.service.SecureStorage
import com.example.moodlegovapp.core.domain.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

class MockApiService(
    private val context: Context,
    private val secureStorage: SecureStorage
) : ApiServiceProtocol {

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

    override suspend fun getUserProfile(): AppResult<User> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_user_profile, object : TypeToken<User>() {}))
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_performance_overview, object : TypeToken<PerformanceOverview>() {}))
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_enrolled_courses, object : TypeToken<List<Course>>() {}))
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<Course> {
        fakeDelay()
        return try {
            val course = readJson(R.raw.mock_course_detail, object : TypeToken<Course>() {})
            AppResult.Success(course.copy(id = courseId))
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        fakeDelay()
        return try {
            val course = readJson(R.raw.mock_course_detail, object : TypeToken<Course>() {})
            AppResult.Success(course.modules)
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
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
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        fakeDelay()
        return AppResult.Failure(AppError.NotFound)
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String> {
        fakeDelay()
        return AppResult.Success("https://mock.gov.ae/certificates/$certificateId.pdf")
    }

    override suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    override suspend fun getBadges(): AppResult<List<Badge>> {
        fakeDelay()
        return try {
            AppResult.Success(readJson(R.raw.mock_badges, object : TypeToken<List<Badge>>() {}))
        } catch (e: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        fakeDelay()
        return AppResult.Success(TrainingStats(activeCourses = 3, activitiesDue = 2, completedCourses = 1))
    }
}
