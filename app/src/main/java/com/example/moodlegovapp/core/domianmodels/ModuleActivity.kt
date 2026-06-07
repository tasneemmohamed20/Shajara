package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

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
