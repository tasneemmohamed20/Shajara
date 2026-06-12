package com.example.moodlegovapp.data.network

import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.LeaderboardEntry
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServiceProtocol {
    // AUTH
    suspend fun login(username: String, password: String): AppResult<AuthToken>

    // USER
    suspend fun getUserProfile(): AppResult<User>
    suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>

    // COURSES
    suspend fun getEnrolledCourses(): AppResult<List<Course>>
    suspend fun getCourseDetail(courseId: Int): AppResult<Course>
    suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>>
    suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>>

    // ASSIGNMENTS
    suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>
    suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment>
    suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit>

    // NOTIFICATIONS
    suspend fun getNotifications(): AppResult<List<Notification>>
    suspend fun markNotificationRead(notificationId: Int): AppResult<Unit>

    // CERTIFICATES
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun getCertificateDownloadUrl(certificateId: Int): AppResult<String>

    // LEADERBOARD
    suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>>

    // BADGES
    suspend fun getBadges(): AppResult<List<Badge>>

    // SCHEDULE
    suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>

    // STATS
    suspend fun getTrainingStats(): AppResult<TrainingStats>

    // SEARCH
    suspend fun searchCourses(query: String): AppResult<List<Course>>

    // ACTIVITY
    suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit>
}


interface RetrofitApiService {

    // AUTH
    @GET("auth/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<AuthToken>

    // USER
    @GET("user/profile")
    suspend fun getUserProfile(
        @Query("id") userId: Int
    ): Response<User>

    @GET("progress/overview")
    suspend fun getPerformanceOverview(
        @Query("id") userId: Int
    ): Response<PerformanceOverview>

    // COURSES
    @GET("user/enrolled-courses")
    suspend fun getEnrolledCourses(
        @Query("id") userId: Int
    ): Response<List<Course>>

    @GET("courses")
    suspend fun getCourseDetail(
        @Query("courseId") courseId: Int
    ): Response<Course>

    @GET("courses")
    suspend fun getCourseModules(
        @Query("courseId") courseId: Int,
        @Query("contents") contents: String = "true"
    ): Response<List<CourseModule>>

    @GET("courses")
    suspend fun getCourseResources(
        @Query("courseId") courseId: Int,
        @Query("contents") contents: String = "resources"
    ): Response<List<CourseResource>>

    @GET("courses")
    suspend fun getAssignments(
        @Query("courseId")  courseId: Int,
        @Query("activities") activities: String = "assignments"
    ): Response<List<Assignment>>

    @GET("courses")
    suspend fun getAssignmentDetail(
        @Query("activity") activity: String = "assignment",
        @Query("assignid") assignId: Int
    ): Response<Assignment>

    // NOTIFICATIONS
    @GET("notifications")
    suspend fun getNotifications(
        @Query("user_id") userId: Int
    ): Response<List<Notification>>

    // CERTIFICATES
    @GET("certificates")
    suspend fun getCertificates(
        @Query("user_id") userId: Int,
        @Query("id") id: Int = 1
    ): Response<List<Certificate>>

    // LEADERBOARD
    @GET("courses")
    suspend fun getLeaderboard(
        @Query("courseId")    courseId: Int,
        @Query("leaderboard") leaderboard: String = "true"
    ): Response<List<LeaderboardEntry>>

    // BADGES
    @GET("user/preferences")
    suspend fun getBadges(
        @Query("id") userId: Int
    ): Response<List<Badge>>

    // EVENTS
    @GET("notifications")
    suspend fun getUpcomingEvents(
        @Query("user_id") userId: Int
    ): Response<List<TrainingEvent>>

    // STATS (comes from progress/overview)
    @GET("progress/overview")
    suspend fun getTrainingStats(
        @Query("id") userId: Int
    ): Response<TrainingStats>
}