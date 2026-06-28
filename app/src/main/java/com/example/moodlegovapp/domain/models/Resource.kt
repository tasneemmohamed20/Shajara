package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

// ── Course resource ───────────────────────────────────────────────────────────
// Matches the /api/courses/{courseId}/resources response shape from courses.json

data class MoodleResource(
    val id: Int,
    val coursemodule: Int,
    val course: Int,
    val name: String,
    val section: Int,
    val visible: Boolean,
    @SerializedName("contentfiles")
    val contentFiles: List<MoodleResourceFile>? = emptyList()
)

data class MoodleResourceFile(
    val filename: String,
    val filepath: String,
    val filesize: Long,
    val fileurl: String,
    val timemodified: Long,
    val mimetype: String,
    val isexternalfile: Boolean,
    val icon: String
)

data class MoodleResourcesResponse(
    val resources: List<MoodleResource>,
    val warnings: List<MoodleWarning> = emptyList()
)