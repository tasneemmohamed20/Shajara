package com.example.moodlegovapp.data.network

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
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatusResponse
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseModule

import com.example.moodlegovapp.domain.models.FileUploadResponse
import com.example.moodlegovapp.domain.models.LeaderboardResponse
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.SubmissionFinalizeResponse
import com.example.moodlegovapp.domain.models.SubmissionSaveResponse
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    // AUTH & USER
    @GET("webservice/rest/server.php")
    suspend fun getUserByField(
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "core_user_get_users_by_field",
        @Query("moodlewsrestformat") format: String = "json",
        @Query("field") field: String = "username",
        @Query("values[0]") value: String
    ): Response<List<com.example.moodlegovapp.domain.models.StudentUser>>

    @GET("progress/overview")
    suspend fun getPerformanceOverview(
        @Query("id") userId: Int
    ): Response<PerformanceOverview>

    // COURSES
    @GET("webservice/rest/server.php")
    suspend fun getEnrolledCourses(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<List<Course>>

    @GET("webservice/rest/server.php")
    suspend fun getCourseContents(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "core_course_get_contents",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<List<com.example.moodlegovapp.domain.models.CourseSection>>

    @GET("courses")
    suspend fun getCourseModules(
        @Query("courseId") courseId: Int,
        @Query("contents") contents: String = "true"
    ): Response<List<CourseModule>>

    // ── Assignments ────────────────────────────────────────────────────────

    @GET("webservice/rest/server.php")
    suspend fun getAssignments(
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseAssignmentsResponse>

    @GET("webservice/rest/server.php")
    suspend fun getAssignmentsByCourse(
        @Query("courseids[0]") courseId: Int,
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseAssignmentsResponse>

    /** GET /api/courses/assignments/{assignmentId}/submission?userId={userId} */
    @GET("api/courses/assignments/{assignmentId}/submission")
    suspend fun getSubmissionStatus(
        @Path("assignmentId") assignmentId: Int,
        @Query("userId")      userId:       Int
    ): Response<AssignmentSubmissionStatusResponse>

    /** POST /api/courses/assignments/{assignmentId}/submission/save?userId={userId} */
    @POST("api/courses/assignments/{assignmentId}/submission/save")
    suspend fun saveSubmission(
        @Path("assignmentId") assignmentId: Int,
        @Query("userId")      userId:       Int,
        @Body                 body:         AssignmentSubmission
    ): Response<SubmissionSaveResponse>

    /** POST /api/courses/assignments/{assignmentId}/submission/finalize?userId={userId} */
    @POST("api/courses/assignments/{assignmentId}/submission/finalize")
    suspend fun finalizeSubmission(
        @Path("assignmentId") assignmentId: Int,
        @Query("userId")      userId:       Int,
        @Body                 body: AssignmentSubmissionFinalize
    ): Response<SubmissionFinalizeResponse>

    /**
     * POST /api/courses/assignments/{assignmentId}/upload?userId={userId}
     * Content-Type: multipart/form-data
     */
    @Multipart
    @POST("api/courses/assignments/{assignmentId}/upload")
    suspend fun uploadAssignmentFile(
        @Path("assignmentId")   assignmentId: Int,
        @Query("userId")        userId:       Int,
        @Part                   file:         MultipartBody.Part,
        @Part("draftItemId")    draftItemId:  RequestBody
    ): Response<FileUploadResponse>

    // ── Course resources ───────────────────────────────────────────────────

    @GET("webservice/rest/server.php")
    suspend fun getCourseResources(
        @Query("courseids[0]") courseId: Int,
        @Query("wstoken") token: String = "b4cd92a9bbb816fc54ae1a43a01d1dcc",
        @Query("wsfunction") wsfunction: String = "mod_resource_get_resources_by_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.MoodleResourcesResponse>

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