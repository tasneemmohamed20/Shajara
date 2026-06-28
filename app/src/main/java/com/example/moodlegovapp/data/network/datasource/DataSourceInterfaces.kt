package com.example.moodlegovapp.data.network.datasource

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.MoodleAssignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection
import com.example.moodlegovapp.domain.models.CourseModule

import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile

/**
 * Remote data source interfaces following the Data Source pattern.
 * Abstract API calls away from repositories for better testability.
 */

interface AuthDataSource {
    suspend fun login(username: String, password: String): AppResult<AuthToken>
    suspend fun requestPasswordReset(email: String): AppResult<com.example.moodlegovapp.domain.models.PasswordResetResult>
    suspend fun getSignupSettings(): AppResult<com.example.moodlegovapp.domain.models.SignupSettings>
    suspend fun getSiteInfo(): AppResult<com.example.moodlegovapp.domain.models.SiteInfo>
}

interface UserDataSource {
    suspend fun getUserProfile(): AppResult<UserProfile>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>
    suspend fun getUserPreferences(userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.UserPreference>>
    suspend fun updatePreference(type: String, value: String): AppResult<com.example.moodlegovapp.domain.models.PreferenceUpdateResult>
    suspend fun updateUserPicture(draftItemId: Long, delete: Boolean = false): AppResult<com.example.moodlegovapp.domain.models.UserPictureUpdateResult>
}

interface CoursesDataSource {
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getAllCourses(): AppResult<List<Course>>
    suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseModule(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail>
    suspend fun getCourseModuleByInstance(module: String, instance: Int): AppResult<com.example.moodlegovapp.domain.models.MoodleCourseModuleDetail>
    suspend fun getCourseActivities(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleActivity>>
    suspend fun getCourseCompletionStatus(courseId: Int, userId: Int? = null): AppResult<com.example.moodlegovapp.domain.models.CourseCompletionStatus>
    suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>>
    suspend fun getGradeItems(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.GradeItem>>
    suspend fun getOverviewCourseGrades(userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.CourseGrade>>
    suspend fun getUserGradesTable(courseId: Int, userId: Int? = null): AppResult<List<com.example.moodlegovapp.domain.models.GradeTable>>
    suspend fun getQuizzes(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleQuiz>>
}

interface AssignmentsDataSource {
    suspend fun getAssignments(courseId: Int): AppResult<List<MoodleAssignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<MoodleAssignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>
}

interface NotificationsDataSource {
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markNotificationRead(notificationId: Int): AppResult<Unit>
    suspend fun getUnreadNotificationCount(): AppResult<Int>
    suspend fun getActionEventsByTimesort(from: Long, to: Long, limit: Int = 20): AppResult<List<TrainingEvent>>
}

interface CertificatesDataSource {
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String>
    suspend fun viewCertificate(cmid: Int): AppResult<com.example.moodlegovapp.domain.models.CertificateViewResponse>
}

interface LeaderboardDataSource {
    suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData>
}

interface BadgesDataSource {
    suspend fun getBadges(): AppResult<List<Badge>>
}

interface EventsDataSource {
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>
}

interface StatsDataSource {
    suspend fun getTrainingStats(): AppResult<TrainingStats>
}

interface SearchDataSource {
    suspend fun searchCourses(query: String): AppResult<List<Course>>
}

interface ActivityDataSource {
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}


