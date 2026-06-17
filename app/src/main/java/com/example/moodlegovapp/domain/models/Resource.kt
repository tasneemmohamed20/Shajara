package com.example.moodlegovapp.domain.models

// ── Course resource ───────────────────────────────────────────────────────────
// Matches the /api/courses/{courseId}/resources response shape from courses.json

data class CourseResource(
    val id: String,
    val name: String,
    val fileName: String?,
    val mimeType: String?,
    val fileSizeLabel: String?,
    val downloadUrl: String?
)

data class CourseResourcesResponse(
    val success: Boolean,
    val data: CourseResourcesData?
)

data class CourseResourcesData(
    val resources: List<CourseResource>
)