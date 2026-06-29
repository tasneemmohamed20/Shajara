package com.example.moodlegovapp.domain.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// User / preferences / profile image
data class UserPreferencesResponse(
    val preferences: List<UserPreference> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class UserPreference(
    val name: String = "",
    val value: String? = null
)

data class PreferenceUpdateResult(
    val success: Boolean = false,
    val message: String = "",
    @SerializedName("userid") val userId: Int = 0,
    val username: String = "",
    val preference: String = "",
    val value: String = ""
)

data class UserPictureUpdateResult(
    val success: Boolean = false,
    @SerializedName("profileimageurl") val profileImageUrl: String = "",
    val warnings: List<MoodleWarning> = emptyList()
)

data class MoodleUploadedFile(
    @SerializedName("itemid") val itemId: Long = 0L,
    val filename: String = "",
    val filepath: String = "",
    val fileurl: String? = null,
    val url: String? = null,
    val mimetype: String? = null
)

// Auth / site info
data class PasswordResetResult(
    val status: String = "",
    val notice: String = "",
    val warnings: List<MoodleWarning> = emptyList()
)

data class SignupSettings(
    val namefields: List<String> = emptyList(),
    val passwordpolicy: String = "",
    val defaultcity: String = "",
    val country: String = "",
    val extendedusernamechars: Boolean = false,
    val warnings: List<MoodleWarning> = emptyList()
)

data class SiteInfo(
    @SerializedName("userid") val userId: Int = 0,
    val username: String = "",
    val firstname: String? = null,
    val lastname: String? = null,
    val fullname: String? = null,
    val siteurl: String? = null,
    val sitename: String? = null,
    val functions: List<SiteFunction> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class SiteFunction(
    val name: String = "",
    val version: String? = null
)

// Courses / modules / activities
data class CourseSearchResponse(
    val total: Int = 0,
    val courses: List<Course> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class MoodleCoursesResponse(
    val courses: List<Course> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class CourseModuleResponse(
    val cm: MoodleCourseModuleDetail? = null,
    val warnings: List<MoodleWarning> = emptyList()
)

data class MoodleCourseModuleDetail(
    val id: Int = 0,
    val course: Int = 0,
    val module: Int = 0,
    val name: String = "",
    val modname: String = "",
    val instance: Int = 0,
    val section: Int = 0,
    val sectionnum: Int = 0,
    val completion: Int = 0,
    val visible: Int = 0,
    val grade: Int? = null,
    val gradepass: String? = null,
    val availability: String? = null,
    val advancedgrading: List<AdvancedGradingArea> = emptyList()
)

data class AdvancedGradingArea(
    val area: String = "",
    val method: String = ""
)

data class ActivitiesResponse(
    val activities: List<MoodleActivity> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)
data class ModuleDate(
    val label: String = "",
    val timestamp: Long = 0L,
    val dataid: String = ""
)
data class MoodleActivity(
    val id: Int = 0,
    val cmid: Int = 0,
    val courseid: Int = 0,
    val name: String = "",
    val modname: String = "",
    val instance: Int = 0,
    val visible: Int = 0,
    val completion: Int = 0,
    val completionstate: Int? = null,
    val url: String? = null,
    val description: String? = null,
    val dates: List<ModuleDate> = emptyList()
)

data class ActivityCompletionUpdateResult(
    val status: Boolean = false,
    val warnings: List<MoodleWarning> = emptyList()
)

// Grades / completion
data class GradeItemsResponse(
    @SerializedName(value = "gradeItems", alternate = ["gradeitems"])
    val gradeItems: List<GradeItem> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class GradeItem(
    val id: String = "",
    val itemname: String = "",
    val category: String = ""
)

data class CourseGradesResponse(
    val grades: List<CourseGrade> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class CourseGrade(
    val courseid: Int = 0,
    val grade: String = "",
    val rawgrade: String = ""
)

data class CourseCompletionStatusResponse(
    val completionstatus: CourseCompletionStatus = CourseCompletionStatus(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class CourseCompletionStatus(
    val completed: Boolean = false,
    val aggregation: Int = 0,
    val completions: List<CourseCompletionCriteria> = emptyList()
)

data class CourseCompletionCriteria(
    val type: Int = 0,
    val title: String = "",
    val status: String = "",
    val complete: Boolean = false,
    val timecompleted: Long? = null,
    val details: CompletionDetails? = null
)

data class CompletionDetails(
    val type: String = "",
    val criteria: String = "",
    val requirement: String = "",
    val status: String = ""
)

data class GradesTableResponse(
    val tables: List<GradeTable> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class GradeTable(
    val courseid: Int = 0,
    val userid: Int = 0,
    val userfullname: String = "",
    val maxdepth: Int = 0,
    val tabledata: List<JsonElement> = emptyList()
)

// Assignments / quizzes
data class QuizResponse(
    val courses: List<QuizCourse> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class QuizCourse(
    val id: Int = 0,
    val quizzes: List<MoodleQuiz> = emptyList()
)

data class MoodleQuiz(
    val id: Int = 0,
    val course: Int = 0,
    val cmid: Int = 0,
    val name: String = "",
    val intro: String = "",
    val timeopen: Long = 0L,
    val timeclose: Long = 0L,
    val grade: Double = 0.0
)

// Notifications / calendar
data class UnreadNotificationCountResponse(
    val count: Int = 0,
    val warnings: List<MoodleWarning> = emptyList()
)

data class CalendarEventsResponse(
    val events: List<MoodleCalendarEvent> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class MoodleCalendarEvent(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val format: Int = 1,
    val courseid: Int = 0,
    val groupid: Int = 0,
    val userid: Int = 0,
    val repeatid: Int = 0,
    val modulename: String? = null,
    val instance: Int = 0,
    val eventtype: String = "",
    val timestart: Long = 0L,
    val timeduration: Long = 0L,
    val visible: Int = 1,
    val timemodified: Long = 0L,
    val location: String? = null
)

// Certificates
data class CertificatesResponse(
    val certificates: List<Certificate> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

data class CertificateViewResponse(
    val success: Boolean = false,
    val cmid: Int = 0,
    val customcertid: Int = 0,
    val name: String = "",
    val downloadurl: String = "",
    val message: String = ""
)
