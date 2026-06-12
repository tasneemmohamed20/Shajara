package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

// 1. Top-level API Wrapper Response
data class CourseDetailsResponse(
    val success: Boolean,
    val data: CourseDetail? = null
)

// 2. The main Course details model
data class CourseDetail(
    @SerializedName("courseId", alternate = ["id"])
    val id: Int,
    val fullName: String,
    val instructor: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val hasCertificate: Boolean,
    val overallProgress: Int,
    val nextRequiredAssignment: NextAssignment? = null,
    val totalModules: Int,
    val courseResources: CourseResources,
    val modules: List<CourseModule>
)

// 3. Upcoming required assignment metadata
data class NextAssignment(
    val id: Int,
    val name: String,
    val type: String,
    val dueDate: String? = null,
    val dueLabel: String,
    val cmid: Int = 0
)

// 4. Shared resources block
data class CourseResources(
    val label: String,
    val description: String,
    val url: String? = null
)

// 5. Course Module structure
data class CourseModule(
    val id: Int,
    val name: String,
    val shortName: String,
    val section: Int,
    val status: String,
    val completionPercent: Int = 0,
    val totalActivities: Int,
    val completedActivities: Int,
    val isLocked: Boolean,
    val activities: List<ModuleActivity>? = null
) {
    val progressPercent: Int
        get() = completionPercent.takeIf { it > 0 }
            ?: if (totalActivities > 0) (completedActivities * 100) / totalActivities else 0

    val isCompleted: Boolean
        get() = status.equals("completed", ignoreCase = true)
}

// 6. Individual Activity details inside a module
data class ModuleActivity(
    val id: Int,
    val name: String,
    val type: String,
    val duration: String? = null,
    val status: String,
    val dueLabel: String? = null,
    val dueDate: String? = null
)