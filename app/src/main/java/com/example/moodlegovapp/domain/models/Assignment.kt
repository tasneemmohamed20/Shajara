package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 1. Top-level wrapper model
@Serializable
data class CourseAssignmentsResponse(
    val courses: List<CourseAssignmentGroup> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

// 2. Course container group
@Serializable
data class CourseAssignmentGroup(
    val id: Int,

    @SerializedName("fullname")
    val fullName: String,

    @SerializedName("shortname")
    val shortName: String,

    @SerializedName("timemodified")
    val timeModified: Long,

    val assignments: List<MoodleAssignment> = emptyList()
)

// 3. Detailed assignment object
@Serializable
data class MoodleAssignment(
    val id: Int,
    val cmid: Int,
    val course: Int,
    val name: String,
    val grade: Int,
    val intro: String,

    @SerializedName("nosubmissions")
    val noSubmissions: Int,

    @SerializedName("submissiondrafts")
    val submissionDrafts: Int,

    @SerializedName("sendnotifications")
    val sendNotifications: Int,

    @SerializedName("sendlatenotifications")
    val sendLateNotifications: Int,

    @SerializedName("sendstudentnotifications")
    val sendStudentNotifications: Int,

    @SerializedName("duedate")
    val dueDate: Long,

    @SerializedName("allowsubmissionsfromdate")
    val allowSubmissionsFromDate: Long,

    @SerializedName("gradepenalty")
    val gradePenalty: Int,

    @SerializedName("timemodified")
    val timeModified: Long,

    @SerializedName("completionsubmit")
    val completionSubmit: Int,

    @SerializedName("cutoffdate")
    val cutoffDate: Long,

    @SerializedName("gradingduedate")
    val gradingDueDate: Long,

    @SerializedName("teamsubmission")
    val teamSubmission: Int,

    @SerializedName("requireallteammemberssubmit")
    val requireAllTeamMembersSubmit: Int,

    @SerializedName("teamsubmissiongroupingid")
    val teamSubmissionGroupingId: Int,

    @SerializedName("blindmarking")
    val blindMarking: Int,

    @SerializedName("hidegrader")
    val hideGrader: Int,

    @SerializedName("revealidentities")
    val revealIdentities: Int,

    @SerializedName("attemptreopenmethod")
    val attemptReopenMethod: String,

    @SerializedName("maxattempts")
    val maxAttempts: Int,

    @SerializedName("markingworkflow")
    val markingWorkflow: Int,

    @SerializedName("markingallocation")
    val markingAllocation: Int,

    @SerializedName("markinganonymous")
    val markingAnonymous: Int,

    @SerializedName("requiresubmissionstatement")
    val requireSubmissionStatement: Int,

    @SerializedName("preventsubmissionnotingroup")
    val preventSubmissionNotInGroup: Int,

    @SerializedName("introformat")
    val introFormat: Int,

    @SerializedName("timelimit")
    val timeLimit: Int,

    @SerializedName("submissionattachments")
    val submissionAttachments: Int,

    val configs: List<PluginConfig> = emptyList(),

    @SerializedName("introfiles")
    val introFiles: List<String> = emptyList(),

    @SerializedName("introattachments")
    val introAttachments: List<String> = emptyList()
)

// 4. Inner plugin configurations block
@Serializable
data class PluginConfig(
    val plugin: String,
    val subtype: String,
    val name: String,
    val value: String
)

// 5. Shared generic Moodle warning metadata structure
@Serializable
data class MoodleWarning(
    val item: String? = null,
    val itemid: Int? = null,
    val warningcode: String,
    val message: String
)