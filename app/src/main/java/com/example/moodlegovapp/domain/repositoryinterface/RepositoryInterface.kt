package com.example.moodlegovapp.domain.repositoryinterface

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseDetail
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

interface AuthRepositoryProtocol {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
    suspend fun logout()
//    val isLoggedIn: Boolean
}

interface UserRepositoryProtocol {
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
    suspend fun getBadges(): AppResult<List<Badge>>
    suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData>
    suspend fun getTrainingStats(): AppResult<TrainingStats>
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

interface CoursesRepositoryProtocol {
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getCourseDetail(courseId: Int): AppResult<CourseDetail>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>>
    suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>
    suspend fun searchCourses(query: String): AppResult<List<Course>>
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}

interface NotificationsRepositoryProtocol {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markAsRead(notificationId: Int): AppResult<Unit>
}

interface CertificatesRepositoryProtocol {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getDownloadUrl(certificateId: Int): AppResult<String>
}

interface AssignmentsRepositoryProtocol {

    // ── Listing ────────────────────────────────────────────────────────────

    /** All assignments across all enrolled courses for the current user. */
    suspend fun getAllAssignments(): AppResult<List<Assignment>>

    /** Assignments scoped to a specific course. */
    suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>

    /** Fetch full detail for a single assignment. */
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment>

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
    suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>>
}