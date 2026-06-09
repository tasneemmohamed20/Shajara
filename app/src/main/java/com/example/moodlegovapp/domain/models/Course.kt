package com.example.moodlegovapp.domain.models

enum class CourseStatus {
    ACTIVE,
    NEW,
    COMPLETED,
    IN_PROGRESS
}

data class Course(
    val id: Int,
    val title: String,
    val category: String,
    val imageUrl: String,
    val instructorName: String,
    val instructorImage: String,
    val progress: Int,
    val dueIn: String,
    val dueDate: String,
    val startDate: String,
    val endDate: String,
    val status: CourseStatus,
    val tasks: Int,
    val totalModules: Int,
    val certificates: Int,
    val isFavorite: Boolean,
    val modules: List<CourseModule>
)
