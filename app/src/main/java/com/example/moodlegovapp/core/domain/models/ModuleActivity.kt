package com.example.moodlegovapp.core.domain.models

enum class ActivityType {
    VIDEO,
    READING,
    QUIZ,
    ASSIGNMENT
}

data class ModuleActivity(
    val id: Int,
    val title: String,
    val type: ActivityType,
    val duration: String,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val contentUrl: String
)
