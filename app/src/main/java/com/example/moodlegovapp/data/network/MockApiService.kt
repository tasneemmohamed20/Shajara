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
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatusResponse
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection

import com.example.moodlegovapp.domain.models.CourseModule

import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.LeaderboardResponse
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.SubmissionFinalizeData
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UploadedFileInfo
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

    override suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>> {
        fakeDelay()
        return try {
            // Since we don't have a mock JSON for core_course_get_contents yet, we'll return an empty list or fake data
            AppResult.Success(emptyList())
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return when (val result = getCourseContents(courseId)) {
            is AppResult.Success -> AppResult.Success(result.data.flatMap { it.modules })
            is AppResult.Failure -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

//    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
//        fakeDelay()
//        return AppResult.Success(emptyList())
//    }

    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        fakeDelay()
        return try {
            val all = readJson(R.raw.mock_enrolled_courses, object : TypeToken<List<Course>>() {})
            val filtered = if (query.isBlank()) all
            else all.filter { it.fullName?.contains(query, ignoreCase = true) == true }
            AppResult.Success(filtered)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    override suspend fun getAssignments(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleAssignment>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    // 2. getAssignmentDetail — finds a single assignment by id from the same file.
    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleAssignment> {
        fakeDelay()
        return AppResult.Failure(AppError.NotFound)
    }

    // 3. getSubmissionStatus — reads mock_assignment_submission_status.json and
//    patches the assignmentId field so the caller always gets the right id back.
    suspend fun getSubmissionStatus(assignmentId: Int): AppResult<AssignmentSubmissionStatus> {
        fakeDelay()
        return try {
            val response = readJson(
                R.raw.submission_status,
                object : TypeToken<AssignmentSubmissionStatusResponse>(){}
            )
            response.data
                ?.copy(assignmentId = assignmentId)
                ?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.DecodingError)
        } catch (_: Exception) {
            AppResult.Failure(AppError.DecodingError)
        }
    }

    // 4. submitAssignment (draft save) — returns a fixed draft response.
    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        fakeDelay()
        return AppResult.Success(Unit)   // UI layer can treat Unit as "draft saved"
    }

    // 5. finalizeSubmission — always succeeds in mock.
    suspend fun finalizeSubmission(
        finalize: AssignmentSubmissionFinalize
    ): AppResult<SubmissionFinalizeData> {
        fakeDelay()
        return if (finalize.acceptIntegrityStatement) {
            AppResult.Success(
                SubmissionFinalizeData(
                    submissionId  = 9001,
                    status        = "submitted",
                    submittedAt   = "2024-10-22T14:30:00Z",
                    gradingStatus = "not_graded"
                )
            )
        } else {
            AppResult.Failure(AppError.ValidationError(mapOf("integrity" to "Must accept integrity statement")))
        }
    }

    // 6. uploadFile — returns a fixed FileUploadResult without doing a real upload.
    suspend fun uploadFile(
        assignmentId: Int,
        fileName: String,
        fileSizeBytes: Long
    ): AppResult<FileUploadResult> {
        fakeDelay()
        val maxBytes = 25L * 1024 * 1024
        return if (fileSizeBytes > maxBytes) {
            AppResult.Failure(AppError.BadRequest("File exceeds the maximum allowed size of 25MB."))
        } else {
            AppResult.Success(
                FileUploadResult(
                    draftItemId = 987654,
                    file = UploadedFileInfo(
                        fileName = fileName,
                        fileSizeLabel = "${fileSizeBytes / (1024 * 1024)}.${(fileSizeBytes % (1024 * 1024)) / 100000} MB",
                        mimeType = guessMimeType(fileName),
                        previewUrl = "https://lms.sharjahpolice.ae/drafts/101/987654/$fileName"
                    )
                )
            )
        }
    }

    // 7. getCourseResources — now returns empty mock data for MoodleResources
    override suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>> {
        fakeDelay()
        return AppResult.Success(emptyList())
    }

    // ── Private helper (add to MockApiService body) ───────────────────────────────
    private fun guessMimeType(fileName: String): String = when {
        fileName.endsWith(".pdf",  ignoreCase = true) -> "application/pdf"
        fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        fileName.endsWith(".jpg",  ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        fileName.endsWith(".png",  ignoreCase = true) -> "image/png"
        fileName.endsWith(".zip",  ignoreCase = true) -> "application/zip"
        else                                          -> "application/octet-stream"
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
