package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 1. The root collection is parsed as List<CourseSection>
@Serializable
data class CourseSection(
    val id: Int,
    val name: String,
    val visible: Int,
    val summary: String,

    @SerializedName("summaryformat")
    val summaryFormat: Int,

    val section: Int,

    @SerializedName("hiddenbynumsections")
    val hiddenByNumSections: Int,

    @SerializedName("uservisible")
    val userVisible: Boolean,

    val component: String? = null,

    @SerializedName("itemid")
    val itemId: Int? = null,

    val modules: List<CourseModule>
)



// 2. Individual content blocks/activities inside a section

@Serializable

data class CourseModule(

    val id: Int,

    val url: String? = null, // Can be null (e.g. for text labels)

    val name: String,

    val instance: Int,



    @SerializedName("contextid")

    val contextId: Int,



    val description: String? = null,

    val visible: Int,



    @SerializedName("uservisible")

    val userVisible: Boolean,



    @SerializedName("visibleoncoursepage")

    val visibleOnCoursePage: Int,



    @SerializedName("modicon")

    val modIcon: String,



    @SerializedName("modname")

    val modName: String,



    val purpose: String,

    val branded: Boolean,



    @SerializedName("modplural")

    val modPlural: String,



    val availability: String? = null,

    val indent: Int,

    val onclick: String,



    @SerializedName("afterlink")

    val afterLink: String? = null,



    @SerializedName("activitybadge")
    val activityBadge: com.google.gson.JsonElement? = null,



    @SerializedName("customdata")

    val customData: String, // Kept as raw string since it contains a JSON sub-string escape layer



    @SerializedName("noviewlink")

    val noViewLink: Boolean,



    @SerializedName("candisplay")

    val canDisplay: Boolean,



    val completion: Int,



    @SerializedName("completiondata")

    val completionData: CompletionData? = null,



    @SerializedName("downloadcontent")

    val downloadContent: Int,



    val dates: List<ActivityDate> = emptyList(),



    @SerializedName("groupmode")

    val groupMode: Int,



    val contents: List<ModuleContent> = emptyList(),



    @SerializedName("contentsinfo")

    val contentsInfo: ContentsSummary? = null

)



// 3. Activity tags (e.g. PDF, DOCX status badges)

@Serializable

data class ActivityBadge(

    @SerializedName("badgecontent")

    val badgeContent: String,



    @SerializedName("badgestyle")

    val badgeStyle: String

)



// 4. Progress and tracking states for user activities

@Serializable

data class CompletionData(

    val state: Int,



    @SerializedName("timecompleted")

    val timeCompleted: Long,



    @SerializedName("overrideby")

    val overrideBy: Int? = null,



    @SerializedName("valueused")

    val valueUsed: Boolean,



    @SerializedName("hascompletion")

    val hasCompletion: Boolean,



    @SerializedName("isautomatic")

    val isAutomatic: Boolean,



    @SerializedName("istrackeduser")

    val isTrackedUser: Boolean,



    @SerializedName("uservisible")

    val userVisible: Boolean,



    val details: List<String> = emptyList(),



    @SerializedName("isoverallcomplete")

    val isOverallComplete: Boolean

)



// 5. Open and Due times metric array blocks

@Serializable

data class ActivityDate(

    val label: String,

    val timestamp: Long,



    @SerializedName("dataid")

    val dataId: String

)



// 6. Direct attachments / Links linked inside the module layer

@Serializable

data class ModuleContent(

    val type: String,

    val filename: String,



    @SerializedName("filepath")

    val filePath: String?,



    @SerializedName("filesize")

    val fileSize: Int,



    @SerializedName("fileurl")

    val fileUrl: String,



    @SerializedName("timecreated")

    val timeCreated: Long? = null,



    @SerializedName("timemodified")

    val timeModified: Long,



    @SerializedName("sortorder")

    val sortOrder: Int? = null,



    @SerializedName("isexternalfile")

    val isExternalFile: Boolean = false,



    @SerializedName("userid")

    val userId: Int? = null,



    val author: String? = null,

    val license: String? = null

)



// 7. General bundle summary info for files block

@Serializable

data class ContentsSummary(

    @SerializedName("filescount")

    val filesCount: Int,



    @SerializedName("filessize")

    val filesSize: Int,



    @SerializedName("lastmodified")

    val lastModified: Long,



    val mimetypes: List<String> = emptyList(),



    @SerializedName("repositorytype")

    val repositoryType: String

)