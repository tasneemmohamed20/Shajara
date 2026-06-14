package com.example.moodlegovapp.data.network

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
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServiceProtocol :
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
    ActivityDataSource


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
    ): Response<UserResponse>

    @GET("progress/overview")
    suspend fun getPerformanceOverview(
        @Query("id") userId: Int
    ): Response<PerformanceOverview>

    // COURSES
    @GET("user/enrolled-courses")
    suspend fun getEnrolledCourses(
        @Query("id") userId: Int
    ): Response<List<Course>>

    @GET("courses/{courseId}/contents")
     suspend fun getCourseDetail(
        @Path("courseId") courseId: Int
     ): Response<CourseDetailsResponse>

//    @GET("courses")
//    suspend fun getCourseDetail(
//        @Query("courseId") courseId: Int
//    ): Response<CourseDetailsResponse>

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

    @GET("courses/{courseId}/contents")
    suspend fun getAllUserAssignments(
        @Path("courseId") courseId: Int
    ): Response<AssignmentsResponse>

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
    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("courseId") courseId: Int
    ): Response<LeaderboardResponse>

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