package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    COURSE,
    ACHIEVEMENT,
    SCHEDULE,
    CERTIFICATE,
    ASSIGNMENT
}

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timeCreated: Long,
    val isRead: Boolean,
    val type: NotificationType,
    val iconUrl: String
)
