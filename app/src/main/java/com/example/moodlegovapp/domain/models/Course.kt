package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter

data class Course(
    val id: Int,

    @SerializedName("shortname")
    val shortName: String? = null,

    @SerializedName("fullname")
    val fullName: String? = null,

    @SerializedName("displayname")
    val displayName: String? = null,

    @SerializedName("enrolledusercount")
    val enrolledUserCount: Int? = null,

    @SerializedName("idnumber")
    val idNumber: String? = null,

    val visible: Int? = null, // Can change to Boolean if you use a custom deserializer mapping 1/0 to true/false
    val summary: String? = null,

    @SerializedName("summaryformat")
    val summaryFormat: Int? = null,

    val format: String? = null,

    @SerializedName("courseimage")
    val courseImage: String? = null,

    @SerializedName("showgrades")
    val showGrades: Boolean? = null,

    val lang: String? = null,

    @SerializedName("enablecompletion")
    val enableCompletion: Boolean? = null,

    @SerializedName("completionhascriteria")
    val completionHasCriteria: Boolean? = null,

    @SerializedName("completionusertracked")
    val completionUserTracked: Boolean? = null,

    val category: Int? = null,
    @JsonAdapter(IntFromNumberAdapter::class)
    val progress: Int? = null,
    val completed: Boolean? = null,

    @SerializedName("startdate")
    val startDate: Long? = null, // UNIX timestamps

    @SerializedName("enddate")
    val endDate: Long? = null,   // UNIX timestamps

    val marker: Int? = null,

    @SerializedName("lastaccess")
    val lastAccess: Long? = null, // Nullable just in case a user has never accessed it

    @SerializedName("isfavourite")
    val isFavourite: Boolean? = null,

    val hidden: Boolean? = null,

    @SerializedName("overviewfiles")
    val overviewFiles: List<OverviewFile> = emptyList(),

    @SerializedName("showactivitydates")
    val showActivityDates: Boolean? = null,

    @SerializedName("showcompletionconditions")
    val showCompletionConditions: Boolean? = null,

    @SerializedName("timemodified")
    val timeModified: Long? = null
)

data class OverviewFile(
    val filename: String? = null,
    val filepath: String? = null,
    val filesize: Int? = null,
    val fileurl: String? = null,

    @SerializedName("timemodified")
    val timeModified: Long? = null,

    val mimetype: String? = null
)