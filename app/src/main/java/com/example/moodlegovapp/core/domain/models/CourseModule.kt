package com.example.moodlegovapp.core.domain.models

data class CourseModule(
    val id: Int,
    val moduleNumber: Int,
    val title: String,
    val isCompleted: Boolean,
    val totalActivities: Int,
    val xpReward: Int,
    val progress: Int,
    val isExpanded: Boolean = false,
    val isLocked: Boolean = false,
    val activities: List<ModuleActivity>
)
