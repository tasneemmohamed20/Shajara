package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

// 1. Top-level wrapper model
data class CourseAssignmentsResponse(
    val courses: List<CourseAssignmentGroup> = emptyList(),
    val warnings: List<MoodleWarning> = emptyList()
)

// 2. Course container group
data class CourseAssignmentGroup(
    val id: Int = 0,

    @SerializedName("fullname")
    val fullName: String = "",

    @SerializedName("shortname")
    val shortName: String = "",

    @SerializedName("timemodified")
    val timeModified: Long = 0L,

    val assignments: List<MoodleAssignment> = emptyList()
)

// 3. Detailed assignment object
data class MoodleAssignment(
    val id: Int = 0,
    val cmid: Int = 0,
    val course: Int = 0,
    val name: String = "",
    val grade: Int = 0,
    val intro: String = "",

    @SerializedName("nosubmissions")
    val noSubmissions: Int = 0,

    @SerializedName("submissiondrafts")
    val submissionDrafts: Int = 0,

    @SerializedName("sendnotifications")
    val sendNotifications: Int = 0,

    @SerializedName("sendlatenotifications")
    val sendLateNotifications: Int = 0,

    @SerializedName("sendstudentnotifications")
    val sendStudentNotifications: Int = 0,

    @SerializedName("duedate")
    val dueDate: Long = 0L,

    @SerializedName("allowsubmissionsfromdate")
    val allowSubmissionsFromDate: Long = 0L,

    @SerializedName("gradepenalty")
    val gradePenalty: Int = 0,

    @SerializedName("timemodified")
    val timeModified: Long = 0L,

    @SerializedName("completionsubmit")
    val completionSubmit: Int = 0,

    @SerializedName("cutoffdate")
    val cutoffDate: Long = 0L,

    @SerializedName("gradingduedate")
    val gradingDueDate: Long = 0L,

    @SerializedName("teamsubmission")
    val teamSubmission: Int = 0,

    @SerializedName("requireallteammemberssubmit")
    val requireAllTeamMembersSubmit: Int = 0,

    @SerializedName("teamsubmissiongroupingid")
    val teamSubmissionGroupingId: Int = 0,

    @SerializedName("blindmarking")
    val blindMarking: Int = 0,

    @SerializedName("hidegrader")
    val hideGrader: Int = 0,

    @SerializedName("revealidentities")
    val revealIdentities: Int = 0,

    @SerializedName("attemptreopenmethod")
    val attemptReopenMethod: String = "none",

    @SerializedName("maxattempts")
    val maxAttempts: Int = 0,

    @SerializedName("markingworkflow")
    val markingWorkflow: Int = 0,

    @SerializedName("markingallocation")
    val markingAllocation: Int = 0,

    @SerializedName("markinganonymous")
    val markingAnonymous: Int = 0,

    @SerializedName("requiresubmissionstatement")
    val requireSubmissionStatement: Int = 0,

    @SerializedName("preventsubmissionnotingroup")
    val preventSubmissionNotInGroup: Int = 0,

    @SerializedName("introformat")
    val introFormat: Int = 1,

    @SerializedName("timelimit")
    val timeLimit: Int = 0,

    @SerializedName("submissionattachments")
    val submissionAttachments: Int = 0,

    val configs: List<PluginConfig>? = emptyList(),

    @SerializedName("introfiles")
    val introFiles: List<String>? = emptyList(),

    @SerializedName("introattachments")
    val introAttachments: List<String>? = emptyList()
)

// 4. Inner plugin configurations block
data class PluginConfig(
    val plugin: String,
    val subtype: String,
    val name: String,
    val value: String
)

// 5. Shared generic Moodle warning metadata structure
data class MoodleWarning(
    val item: String? = null,
    val itemid: Int? = null,
    val warningcode: String = "",
    val message: String = ""
)