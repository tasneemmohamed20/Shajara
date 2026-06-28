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
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
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
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_user_get_users_by_field",
        @Query("moodlewsrestformat") format: String = "json",
        @Query("field") field: String = "username",
        @Query("values[0]") value: String
    ): Response<List<com.example.moodlegovapp.domain.models.StudentUser>>

    @FormUrlEncoded
    @POST("login/token.php")
    suspend fun loginToken(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("service") service: String = "govlms_mobile"
    ): Response<AuthToken>
    @GET("progress/overview")
    suspend fun getPerformanceOverview(
        @Query("id") userId: Int
    ): Response<PerformanceOverview>

    // COURSES
    @GET("webservice/rest/server.php")
    suspend fun getEnrolledCourses(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_enrol_get_users_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<List<Course>>

    @GET("webservice/rest/server.php")
    suspend fun getCourseContents(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
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
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_assign_get_assignments",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseAssignmentsResponse>

    @GET("webservice/rest/server.php")
    suspend fun getAssignmentsByCourse(
        @Query("courseids[0]") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
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
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_resource_get_resources_by_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.MoodleResourcesResponse>

    // NOTIFICATIONS / CALENDAR EVENTS
    @GET("webservice/rest/server.php")
    suspend fun getNotifications(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_calendar_get_calendar_events",
        @Query("moodlewsrestformat") format: String = "json",
        @Query("options[userevents]") userEvents: Int = 1,
        @Query("options[siteevents]") siteEvents: Int = 1
    ): Response<JsonElement>

    // CERTIFICATES
    @GET("webservice/rest/server.php")
    suspend fun getCertificates(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_certificateapi_get_issued_certificates",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<JsonElement>

    // LEADERBOARD
    @GET("webservice/rest/server.php")
    suspend fun getLeaderboard(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_leaderboardapi_get_ranking",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<JsonElement>

    // BADGES
    @GET("user/preferences")
    suspend fun getBadges(
        @Query("id") userId: Int
    ): Response<JsonElement>

    // EVENTS
    @GET("webservice/rest/server.php")
    suspend fun getUpcomingEvents(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_calendar_get_calendar_events",
        @Query("moodlewsrestformat") format: String = "json",
        @Query("options[userevents]") userEvents: Int = 1,
        @Query("options[siteevents]") siteEvents: Int = 1
    ): Response<JsonElement>


    // ── Complete Moodle staging endpoint set ───────────────────────────────
    @GET("webservice/rest/server.php")
    suspend fun getUserPreferences(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_user_get_user_preferences",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.UserPreferencesResponse>

    @GET("webservice/rest/server.php")
    suspend fun updatePreference(
        @Query("type") type: String,
        @Query("value") value: String,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_preferencesapi_update_preference",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.PreferenceUpdateResult>

    @GET("webservice/rest/server.php")
    suspend fun updateUserPicture(
        @Query("draftitemid") draftItemId: Long,
        @Query("delete") delete: Int = 0,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_user_update_picture",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.UserPictureUpdateResult>

    @Multipart
    @POST("webservice/upload.php")
    suspend fun uploadDraftFile(
        @Part("token") token: RequestBody,
        @Part("filearea") filearea: RequestBody,
        @Part("itemid") itemid: RequestBody,
        @Part("filepath") filepath: RequestBody,
        @Part("filename") filename: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<List<com.example.moodlegovapp.domain.models.MoodleUploadedFile>>

    @GET("webservice/rest/server.php")
    suspend fun getCourseModuleByInstance(
        @Query("module") module: String,
        @Query("instance") instance: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_course_get_course_module_by_instance",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseModuleResponse>

    @GET("webservice/rest/server.php")
    suspend fun getCourseModule(
        @Query("cmid") cmid: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_course_get_course_module",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseModuleResponse>

    @GET("webservice/rest/server.php")
    suspend fun getGradeItems(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_grades_get_gradeitems",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GradeItemsResponse>

    @GET("webservice/rest/server.php")
    suspend fun getOverviewCourseGrades(
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "gradereport_overview_get_course_grades",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseGradesResponse>

    @GET("webservice/rest/server.php")
    suspend fun searchCoursesRemote(
        @Query("criteriavalue") query: String,
        @Query("criterianame") criteriaName: String = "search",
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_course_search_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseSearchResponse>

    @GET("webservice/rest/server.php")
    suspend fun viewCertificate(
        @Query("cmid") cmid: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_thirdpartyapi_view_certificate",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CertificateViewResponse>

    @GET("webservice/rest/server.php")
    suspend fun updateActivityCompletionRemote(
        @Query("cmid") cmid: Int,
        @Query("completed") completed: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_mark_activity_complete",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.ActivityCompletionUpdateResult>


    // GOV LMS mobile endpoints
    @GET("webservice/rest/server.php")
    suspend fun getGovDashboard(
        @Query("filter") filter: String = "all",
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_participant_dashboard",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovDashboardDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovCourseOverview(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_course_overview",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovCourseOverviewDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovCourseResources(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_course_resources",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovCourseResourcesDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovLesson(
        @Query("cmid") cmid: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_lesson",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovLessonDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovTasks(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_tasks",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovTasksDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovGrades(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_grades",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovGradesDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovNotifications(
        @Query("unread_only") unreadOnly: Int? = null,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_notifications",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovNotificationsDto>


    @GET("webservice/rest/server.php")
    suspend fun getGovCalendarEvents(
        @Query("limit") limit: Int = 50,
        @Query("timestart") timeStart: Long? = null,
        @Query("timeend") timeEnd: Long? = null,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_calendar_events",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovCalendarEventsDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovLibrary(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_library",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovLibraryDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovUserProfile(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_user_profile",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovProfileDto>

    @GET("webservice/rest/server.php")
    suspend fun loadGovEditableProfile(
        @Query("save") save: Int = 0,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_update_user_profile",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovProfileDto>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun updateGovUserProfile(
        @Field("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Field("wsfunction") wsfunction: String = "local_mobileapi_update_user_profile",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("save") save: Int = 1,
        @Field("mobile_number") mobileNumber: String? = null,
        @Field("emergency_contact_name") emergencyContactName: String? = null,
        @Field("emergency_contact_relationship") emergencyContactRelationship: String? = null,
        @Field("emergency_contact_mobile") emergencyContactMobile: String? = null,
        @Field("user_locale") userLocale: String? = null,
        @Field("notif_assignments") notifAssignments: Int? = null,
        @Field("notif_courses") notifCourses: Int? = null,
        @Field("notif_announcements") notifAnnouncements: Int? = null,
        @Field("notif_certificates") notifCertificates: Int? = null
    ): Response<com.example.moodlegovapp.domain.models.GovProfileDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovAssignmentDetails(
        @Query("assignid") assignId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_assignment_details",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovAssignmentDetailsDto>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun submitGovAssignment(
        @Field("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Field("wsfunction") wsfunction: String = "local_mobileapi_submit_assignment",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("assignid") assignId: Int,
        @Field("submit") submit: Int = 1,
        @Field("submission_type") submissionType: String,
        @Field("online_text") onlineText: String? = null,
        @Field("file_draft_itemid") fileDraftItemId: Long? = null
    ): Response<com.example.moodlegovapp.domain.models.GovGenericSuccessDto>

    @GET("webservice/rest/server.php")
    suspend fun getGovQuizDetails(
        @Query("quizid") quizId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_quiz_details",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.google.gson.JsonElement>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun processGovQuiz(
        @Field("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Field("wsfunction") wsfunction: String = "local_mobileapi_process_quiz",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("action") action: String,
        @Field("quizid") quizId: Int? = null,
        @Field("attemptid") attemptId: Int? = null,
        @Field("page") page: Int? = null,
        @FieldMap encodedAnswers: Map<String, String> = emptyMap()
    ): Response<com.google.gson.JsonElement>

    @GET("webservice/rest/server.php")
    suspend fun getGovFeedbackDetails(
        @Query("feedbackid") feedbackId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_feedback_details",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.google.gson.JsonElement>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun submitGovFeedbackAction(
        @Field("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Field("wsfunction") wsfunction: String = "local_mobileapi_submit_feedback",
        @Field("moodlewsrestformat") format: String = "json",
        @Field("action") action: String,
        @Field("feedbackid") feedbackId: Int,
        @Field("page") page: Int? = null,
        @FieldMap encodedResponses: Map<String, String> = emptyMap()
    ): Response<com.google.gson.JsonElement>

    @GET("webservice/rest/server.php")
    suspend fun getGovCertificate(
        @Query("cmid") cmid: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_get_certificate",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovCertificateDto>

    @GET("webservice/rest/server.php")
    suspend fun markAllGovNotificationsRead(
        @Query("mark_all") markAll: Int = 1,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_mobileapi_mark_notifications_read",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GovGenericSuccessDto>

    @GET("webservice/rest/server.php")
    suspend fun getSignupSettings(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "auth_email_get_signup_settings",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.SignupSettings>

    @FormUrlEncoded
    @POST("webservice/rest/server.php")
    suspend fun requestPasswordReset(
        @Field("email") email: String,
        @Field("username") username: String = email,
        @Field("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Field("wsfunction") wsfunction: String = "core_auth_request_password_reset",
        @Field("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.PasswordResetResult>

    @GET("webservice/rest/server.php")
    suspend fun getCourseCompletionStatus(
        @Query("courseid") courseId: Int,
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_completion_get_course_completion_status",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.CourseCompletionStatusResponse>

    @GET("webservice/rest/server.php")
    suspend fun getUserGradesTable(
        @Query("courseid") courseId: Int,
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "gradereport_user_get_grades_table",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.GradesTableResponse>

    @GET("webservice/rest/server.php")
    suspend fun getCourseActivities(
        @Query("courseid") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_courseapi_get_activities",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.ActivitiesResponse>

    @GET("webservice/rest/server.php")
    suspend fun getSubmissionStatusRemote(
        @Query("assignid") assignmentId: Int,
        @Query("userid") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_assign_get_submission_status",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.AssignmentSubmissionStatusResponse>

    @GET("webservice/rest/server.php")
    suspend fun saveAssignmentSubmissionRemote(
        @Query("assignmentid") assignmentId: Int,
        @Query("plugindata[onlinetext_editor][text]") text: String,
        @Query("plugindata[onlinetext_editor][format]") formatText: Int = 1,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_assign_save_submission",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.SubmissionSaveResponse>

    @GET("webservice/rest/server.php")
    suspend fun submitAssignmentForGradingRemote(
        @Query("assignmentid") assignmentId: Int,
        @Query("acceptsubmissionstatement") acceptStatement: Int = 1,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_assign_submit_for_grading",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.SubmissionFinalizeResponse>

    @GET("webservice/rest/server.php")
    suspend fun getQuizzesByCourses(
        @Query("courseids[0]") courseId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "mod_quiz_get_quizzes_by_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.QuizResponse>

    @GET("webservice/rest/server.php")
    suspend fun getSiteInfo(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_webservice_get_site_info",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<com.example.moodlegovapp.domain.models.SiteInfo>


    @GET("webservice/rest/server.php")
    suspend fun markNotificationRead(
        @Query("notificationid") notificationId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_message_mark_notification_read",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<JsonElement>

    @GET("webservice/rest/server.php")
    suspend fun getUnreadPopupNotificationCount(
        @Query("useridto") userId: Int,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "message_popup_get_unread_popup_notification_count",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<Int>

    @GET("webservice/rest/server.php")
    suspend fun getActionEventsByTimesort(
        @Query("timesortfrom") from: Long,
        @Query("timesortto") to: Long,
        @Query("limitnum") limit: Int = 20,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_calendar_get_action_events_by_timesort",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<JsonElement>

    @GET("webservice/rest/server.php")
    suspend fun getAllCourses(
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "core_course_get_courses",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<List<Course>>

    @GET("webservice/rest/server.php")
    suspend fun loginUserDetails(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("wstoken") token: String = NetworkConfig.WS_TOKEN,
        @Query("wsfunction") wsfunction: String = "local_getuserdetailsapi_login",
        @Query("moodlewsrestformat") format: String = "json"
    ): Response<JsonElement>

    // STATS (comes from progress/overview)
    @GET("progress/overview")
    suspend fun getTrainingStats(
        @Query("id") userId: Int
    ): Response<TrainingStats>
}