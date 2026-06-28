package com.example.moodlegovapp.domain.repositoryinterface

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.MoodleAssignment
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection
import com.example.moodlegovapp.domain.models.CourseModule

import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.ParticipantDashboard
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

interface AuthRepositoryProtocol {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
    suspend fun requestPasswordReset(email: String): AppResult<com.example.moodlegovapp.domain.models.PasswordResetResult>
    suspend fun getSignupSettings(): AppResult<com.example.moodlegovapp.domain.models.SignupSettings>
    suspend fun getSiteInfo(): AppResult<com.example.moodlegovapp.domain.models.SiteInfo>
    suspend fun logout()
//    val isLoggedIn: Boolean
}
interface DashboardRepositoryProtocol {
    suspend fun getDashboard(filter: String = "all"): AppResult<ParticipantDashboard>
}
interface UserRepositoryProtocol {
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
    suspend fun getUserPreferences(userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.UserPreference>>
    suspend fun updatePreference(type: String, value: String): AppResult<com.example.moodlegovapp.domain.models.PreferenceUpdateResult>
    suspend fun updateUserPicture(draftItemId: Long, delete: Boolean = false): AppResult<com.example.moodlegovapp.domain.models.UserPictureUpdateResult>
    suspend fun getBadges(): AppResult<List<Badge>>
    suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData>
    suspend fun getTrainingStats(): AppResult<TrainingStats>
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

interface CoursesRepositoryProtocol {
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getAllCourses(): AppResult<List<Course>>
    suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseModule(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail>
    suspend fun getCourseModuleByInstance(module: String, instance: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail>
    suspend fun getCourseActivities(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleActivity>>
    suspend fun getCourseCompletionStatus(courseId: Int, userId: Int? = null): AppResult<com.example.moodlegovapp.domain.models.CourseCompletionStatus>
    suspend fun getGradeItems(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.GradeItem>>
    suspend fun getOverviewCourseGrades(userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.CourseGrade>>
    suspend fun getUserGradesTable(courseId: Int, userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.GradeTable>>
    suspend fun getQuizzes(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleQuiz>>
    suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>>
    suspend fun getAssignments(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleAssignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleAssignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>
    suspend fun searchCourses(query: String): AppResult<List<Course>>
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}

interface NotificationsRepositoryProtocol {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markAsRead(notificationId: Int): AppResult<Unit>
    suspend fun getUnreadNotificationCount(): AppResult<Int>
    suspend fun getActionEventsByTimesort(from: Long, to: Long, limit: Int = 20): AppResult<List<TrainingEvent>>
}

interface CertificatesRepositoryProtocol {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getDownloadUrl(certificateId: Int): AppResult<String>
    suspend fun viewCertificate(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.CertificateViewResponse>
}

interface AssignmentsRepositoryProtocol {

    // ── Listing ────────────────────────────────────────────────────────────

    /** All assignments across all enrolled courses for the current user. */
    suspend fun getAllAssignments(): AppResult<List<MoodleAssignment>>

    /** Fetch all assignments for a user (and optionally filtered by course). */
    suspend fun getAssignments(courseId: Int = -1): AppResult<List<MoodleAssignment>>

    /** Fetch full detail for a single assignment. */
    suspend fun getAssignmentDetail(courseId: Int, assignmentId: Int): AppResult<MoodleAssignment>

    // ── Submission status ──────────────────────────────────────────────────

    /** Current submission state (grade, attempts, files, etc.) */
    suspend fun getSubmissionStatus(assignmentId: Int): AppResult<AssignmentSubmissionStatus>

    // ── Submit ─────────────────────────────────────────────────────────────

    /** Save text / file references as a draft. */
    suspend fun saveSubmission(submission: AssignmentSubmission): AppResult<Unit>

    /** Finalize and officially submit the assignment. */
    suspend fun finalizeSubmission(finalize: AssignmentSubmissionFinalize): AppResult<Unit>

    // ── File upload ────────────────────────────────────────────────────────

    /**
     * Upload a file attachment for an assignment.
     * @param assignmentId  Target assignment
     * @param fileName      Original file name (used for MIME guessing in mock)
     * @param fileBytes     Raw bytes to upload
     * @return [FileUploadResult] containing the draftItemId to use in [saveSubmission]
     */
    suspend fun uploadFile(
        assignmentId: Int,
        fileName:     String,
        fileBytes:    ByteArray
    ): AppResult<FileUploadResult>

    // ── Resources ──────────────────────────────────────────────────────────

    /** Course-level downloadable resources (PDFs, ZIPs, etc.) */
    suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>>
}
