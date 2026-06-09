package com.example.moodlegovapp.core.domain.models


enum class ResourceType {
    ZIP,
    PDF,
    DOC,
    IMAGE,
    VIDEO
}

data class CourseResource(
    val id: Int,
    val fileName: String,
    val fileSize: String,
    val fileType: ResourceType,
    val fileUrl: String,
    val moduleId: Int?
)
