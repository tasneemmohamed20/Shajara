package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter

data class GovTokenResponse(
    val token: String? = null,
    val privatetoken: String? = null,
    val error: String? = null
)

data class GovPasswordResetResult(
    val status: String? = null,
    val notice: String? = null,
    val warnings: List<GovWarning> = emptyList()
)

data class GovWarning(
    val item: String? = null,
    val itemid: Int? = null,
    val warningcode: String? = null,
    val message: String? = null
)

data class GovSiteInfo(
    val sitename: String? = null,
    val username: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    @SerializedName("leaderboard_user_name") val leaderboardUserName: String? = null,
    val fullname: String? = null,
    val lang: String? = null,
    @SerializedName("leaderboard_user_id") val leaderboardUserId: Int? = null,
    val userid: Int? = null,
    val siteurl: String? = null,
    val userpictureurl: String? = null,
    val userprivateaccesskey: String? = null,
    val functions: List<GovSiteFunction> = emptyList(),
    val uploadfiles: Int? = null,
    val downloadfiles: Int? = null
)

data class GovSiteFunction(val name: String? = null, val version: String? = null)

data class GovUserContextDto(
    val id: Int? = null,
    val username: String? = null,
    @SerializedName("leaderboard_user_name") val leaderboardUserName: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    val contextid: Int? = null,
    val profileimageurl: String? = null,
    val profileimageurlsmall: String? = null
)

data class GovDashboardDto(
    @SerializedName("overall_progress") val overallProgress: Int? = null,
    @JsonAdapter(IntFromNumberAdapter::class)
        @SerializedName("average_grade") val averageGrade: Int? = null,
    @SerializedName("assignment_completion_rate") val assignmentCompletionRate: Double? = null,
    @SerializedName("total_xp") val totalXp: Int? = null,
    @SerializedName("user_level") val userLevel: Int? = null,
    @SerializedName("xp_to_next_level") val xpToNextLevel: Int? = null,
    @SerializedName("my_rank") val myRank: Int? = null,
    @SerializedName("active_courses_count") val activeCoursesCount: Int? = null,
    @SerializedName("completed_count") val completedCount: Int? = null,
    @SerializedName("activities_due_count") val activitiesDueCount: Int? = null,
    @SerializedName("in_progress_courses") val inProgressCourses: List<GovCourseCardDto> = emptyList(),
    @SerializedName("completed_courses") val completedCourses: List<GovCourseCardDto> = emptyList(),
    @SerializedName("upcoming_events") val upcomingEvents: List<GovCalendarEventDto> = emptyList(),
    val leaderboard: List<GovLeaderboardDto> = emptyList(),
    @SerializedName("user_badges") val userBadges: List<GovBadgeDto> = emptyList(),
    val badges: List<GovBadgeDto> = emptyList()
)

data class GovCourseCardDto(
    @SerializedName("course_id") val courseId: Int? = null,
    val id: Int? = null,
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("course_image") val courseImage: String? = null,
    @SerializedName("course_status") val courseStatus: String? = null,
    @SerializedName("instructor_name") val instructorName: String? = null,
    @JsonAdapter(IntFromNumberAdapter::class)
    @SerializedName("progress") val progress: Int? = null,
    @JsonAdapter(IntFromNumberAdapter::class)
    @SerializedName("course_progress") val courseProgress: Int? = null,
    val status: String? = null,
    @SerializedName("due_label") val dueLabel: String? = null
)

data class GovLeaderboardDto(
    @SerializedName("leaderboard_rank") val leaderboardRank: Int? = null,
    val rank: Int? = null,
    @SerializedName("leaderboard_user_id") val leaderboardUserId: Int? = null,
    val userid: Int? = null,
    @SerializedName("leaderboard_user_name") val leaderboardUserName: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    @SerializedName("leaderboard_xp") val leaderboardXp: Int? = null,
    val xp: Int? = null,
    val points: Int? = null,
    @SerializedName("leaderboard_user_avatar") val leaderboardUserAvatar: String? = null,
    val avatar: String? = null
)

data class GovBadgeDto(
    @SerializedName("badge_name") val badgeName: String? = null,
    @SerializedName("badge_icon") val badgeIcon: String? = null
)

data class GovCalendarEventDto(
    @SerializedName("event_id") val eventId: Int? = null,
    val id: Int? = null,
    @SerializedName("event_title") val eventTitle: String? = null,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerializedName("event_datetime") val eventDatetime: Long? = null,
    val time: Long? = null,
    val timestart: Long? = null,
    @SerializedName("event_type") val eventType: String? = null,
    val type: String? = null,
    val course: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("course_id") val courseId: Int? = null,
    @SerializedName("event_instructor") val eventInstructor: String? = null,
    val location: String? = null
)

data class GovCourseOverviewDto(
    @SerializedName("course_status") val courseStatus: String? = null,
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("instructor_name") val instructorName: String? = null,
    @SerializedName("instructor_avatar_url") val instructorAvatarUrl: String? = null,
    @SerializedName("start_date") val startDate: Long? = null,
    @SerializedName("end_date") val endDate: Long? = null,
    @SerializedName("has_certificate") val hasCertificate: Int? = null,
    @SerializedName("course_progress") val courseProgress: Int? = null,
    @SerializedName("course_image") val courseImage: String? = null,
    @SerializedName("next_assignment_name") val nextAssignmentName: String? = null,
    @SerializedName("next_assignment_due") val nextAssignmentDue: Long? = null,
    @SerializedName("modules_count") val modulesCount: Int? = null,
    val modules: List<GovCourseModuleDto> = emptyList()
)

data class GovCourseModuleDto(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("module_title") val moduleTitle: String? = null,
    @SerializedName("module_status") val moduleStatus: String? = null,
    @SerializedName("module_progress") val moduleProgress: Int? = null,
    @SerializedName("module_locked") val moduleLocked: Int? = null,
    @SerializedName("module_activities_count") val moduleActivitiesCount: Int? = null,
    val activities: List<GovCourseActivityDto> = emptyList()
)

data class GovCourseActivityDto(
    @SerializedName("activity_id") val activityId: Int? = null,
    @SerializedName("cmid") val cmid: Int? = null,
    @SerializedName("assign_id") val assignId: Int? = null,
    @SerializedName("modname") val modName: String? = null,
    @SerializedName("activity_title") val activityTitle: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("activity_duration") val activityDuration: String? = null,
    @SerializedName("activity_status") val activityStatus: String? = null,
    @SerializedName("activity_due_date") val activityDueDate: Long? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("activity_is_current") val activityIsCurrent: Boolean? = null
)

data class GovCourseResourcesDto(
    @SerializedName("course_title") val courseTitle: String? = null,
    @SerializedName("instructor_name") val instructorName: String? = null,
    @SerializedName("total_resources_count") val totalResourcesCount: Int? = null,
    @SerializedName("resource_groups") val resourceGroups: List<GovResourceGroupDto> = emptyList()
)

data class GovResourceGroupDto(
    @SerializedName("group_name") val groupName: String? = null,
    @SerializedName("files_count") val filesCount: Int? = null,
    val files: List<GovResourceFileDto> = emptyList()
)

data class GovResourceFileDto(
    val name: String? = null,
    val type: String? = null,
    @SerializedName("size_mb") val sizeMb: Double? = null,
    val url: String? = null
)

data class GovLessonDto(
    val cmid: Int? = null,
    @SerializedName("activity_title") val activityTitle: String? = null,
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("module_number") val moduleNumber: Int? = null,
    @SerializedName("module_title") val moduleTitle: String? = null,
    @SerializedName("lesson_position") val lessonPosition: String? = null,
    @SerializedName("duration_seconds") val durationSeconds: Int? = null,
    @SerializedName("is_live_training") val isLiveTraining: Boolean? = null,
    @SerializedName("is_completed") val isCompleted: Boolean? = null,
    @SerializedName("module_progress_percent") val moduleProgressPercent: Int? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("launch_url") val launchUrl: String? = null,
    @SerializedName("content_url") val contentUrl: String? = null,
    @SerializedName("resume_position_seconds") val resumePositionSeconds: Int? = null,
    @SerializedName("instructor_name") val instructorName: String? = null,
    @SerializedName("instructor_avatar_url") val instructorAvatarUrl: String? = null,
    @SerializedName("instructor_role") val instructorRole: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    @SerializedName("learning_objectives") val learningObjectives: List<String> = emptyList(),
    @SerializedName("has_previous") val hasPrevious: Boolean? = null,
    @SerializedName("previous_cmid") val previousCmid: Int? = null,
    @SerializedName("has_next") val hasNext: Boolean? = null,
    @SerializedName("next_cmid") val nextCmid: Int? = null
)

data class GovTasksDto(
    @SerializedName("active_tasks_count") val activeTasksCount: Int? = null,
    @SerializedName("certificates_count") val certificatesCount: Int? = null,
    @SerializedName("pending_tasks_count") val pendingTasksCount: Int? = null,
    val tasks: List<GovTaskDto> = emptyList()
)

data class GovTaskDto(
    val id: Int? = null,
    val title: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("due_date") val dueDate: Long? = null,
    @SerializedName("is_overdue") val isOverdue: Boolean? = null,
    @SerializedName("is_priority") val isPriority: Boolean? = null,
    @SerializedName("grade_percent") val gradePercent: Int? = null,
    @SerializedName("grade_label") val gradeLabel: String? = null,
    @SerializedName("assign_id") val assignId: Int? = null,
    val cmid: Int? = null
)

data class GovGradesDto(
    @SerializedName("overall_performance_percent") val overallPerformancePercent: Int? = null,
    @SerializedName("completed_count") val completedCount: Int? = null,
    @SerializedName("certificates_count") val certificatesCount: Int? = null,
    @SerializedName("pending_count") val pendingCount: Int? = null,
    val assessments: List<GovAssessmentDto> = emptyList()
)

data class GovAssessmentDto(
    val id: Int? = null,
    val title: String? = null,
    val type: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("grade_raw") val gradeRaw: Double? = null,
    @SerializedName("grade_max") val gradeMax: Double? = null,
    @SerializedName("grade_percent") val gradePercent: Int? = null,
    @SerializedName("grade_label") val gradeLabel: String? = null,
    val status: String? = null,
    val cmid: Int? = null
)

data class GovNotificationsDto(
    @SerializedName("total_unread") val totalUnread: Int? = null,
    val notifications: List<GovNotificationDto> = emptyList()
)

data class GovNotificationDto(
    val id: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val time: Long? = null,
    val type: String? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("is_read") val isRead: Boolean? = null
)

data class GovProfileDto(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("user_designation") val userDesignation: String? = null,
    @SerializedName("batch_number") val batchNumber: String? = null,
    @SerializedName("academy_id") val academyId: String? = null,
    val email: String? = null,
    @SerializedName("mobile_number") val mobileNumber: String? = null,
    @SerializedName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerializedName("emergency_contact_relationship") val emergencyContactRelationship: String? = null,
    @SerializedName("emergency_contact_mobile") val emergencyContactMobile: String? = null,
    @SerializedName("user_locale") val userLocale: String? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("notif_assignments") val notifAssignments: Boolean? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("notif_courses") val notifCourses: Boolean? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("notif_announcements") val notifAnnouncements: Boolean? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("notif_certificates") val notifCertificates: Boolean? = null,
    @JsonAdapter(IntFromBooleanAdapter::class)
    @SerializedName("biometric_enabled") val biometricEnabled: Boolean? = null,
    @SerializedName("user_level") val userLevel: Int? = null,
    @SerializedName("cohort_rank") val cohortRank: Int? = null,
    @SerializedName("total_xp") val totalXp: Int? = null,
    @SerializedName("xp_to_next_level") val xpToNextLevel: Int? = null,
    @SerializedName("overall_progress") val overallProgress: Int? = null,
    @JsonAdapter(IntFromNumberAdapter::class)
        @SerializedName("average_grade") val averageGrade: Int? = null,
    @SerializedName("task_completion_rate") val taskCompletionRate: Double? = null,
    @SerializedName("assignment_completion_rate") val assignmentCompletionRate: Double? = null,
    val badges: List<GovBadgeDto> = emptyList(),
    val certificates: List<GovCertificateDto> = emptyList(),
    val success: Boolean? = null,
    val message: String? = null
)

data class GovCertificateDto(
    @SerializedName("certificate_title") val certificateTitle: String? = null,
    @SerializedName("certificate_status") val certificateStatus: String? = null,
    @SerializedName("certificate_date") val certificateDate: Long? = null,
    @SerializedName("certificate_download_url") val certificateDownloadUrl: String? = null,
    @SerializedName("pending_approval") val pendingApproval: Int? = null
)

data class GovUploadDto(
    val contextid: Int? = null,
    val component: String? = null,
    val filearea: String? = null,
    val itemid: Long? = null,
    val filepath: String? = null,
    val filename: String? = null,
    val url: String? = null
)

data class GovPictureUpdateDto(
    val success: Boolean? = null,
    val profileimageurl: String? = null,
    val warnings: List<GovWarning> = emptyList()
)

data class GovGenericSuccessDto(
    val success: Boolean? = null,
    val message: String? = null
)

data class GovCalendarEventsDto(
    @SerializedName("total_count") val totalCount: Int? = null,
    val events: List<GovCalendarEventDto> = emptyList()
)

data class GovLibraryDto(
    @SerializedName("total_resources_count") val totalResourcesCount: Int? = null,
    val courses: List<GovLibraryCourseDto> = emptyList()
)

data class GovLibraryCourseDto(
    @SerializedName("course_id") val courseId: Int? = null,
    @SerializedName("course_name") val courseName: String? = null,
    @SerializedName("course_image") val courseImage: String? = null,
    val resources: List<GovLibraryResourceDto> = emptyList()
)

data class GovLibraryResourceDto(
    @SerializedName("resource_id") val resourceId: Int? = null,
    @SerializedName("resource_name") val resourceName: String? = null,
    @SerializedName("resource_type") val resourceType: String? = null,
    @SerializedName("file_size_kb") val fileSizeKb: Long? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("section_name") val sectionName: String? = null
)

data class GovAssignmentDetailsDto(
    @SerializedName("assign_id") val assignId: Int? = null,
    val id: Int? = null,
    val cmid: Int? = null,
    @SerializedName("assignment_title") val assignmentTitle: String? = null,
    val title: String? = null,
    @SerializedName("course_name") val courseName: String? = null,
    val description: String? = null,
    @SerializedName("due_date") val dueDate: Long? = null,
    @SerializedName("submission_status") val submissionStatus: String? = null,
    @SerializedName("submission_type") val submissionType: String? = null,
    @SerializedName("allow_online_text") @JsonAdapter(IntFromBooleanAdapter::class) val allowOnlineText: Boolean? = null,
    @SerializedName("allow_file_upload") @JsonAdapter(IntFromBooleanAdapter::class) val allowFileUpload: Boolean? = null,
    @SerializedName("max_files") val maxFiles: Int? = null,
    @SerializedName("max_file_size") val maxFileSize: Long? = null,
    val files: List<GovLibraryResourceDto> = emptyList()
)

data class ParticipantDashboard(
    val overallProgress: Int = 0,
    val activeCoursesCount: Int = 0,
    val activitiesDueCount: Int = 0,
    val completedCount: Int = 0,
    val enrolledCount: Int = 0,
    val averageGrade: Int = 0,
    val assignmentCompletionRate: Double = 0.0,
    val inProgressCourses: List<Course> = emptyList(),
    val completedCourses: List<Course> = emptyList(),
    val upcomingEvents: List<TrainingEvent> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val badges: List<UserBadge> = emptyList()
)
