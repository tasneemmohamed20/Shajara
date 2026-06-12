package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: Int,
    val title: String,
    val body: String,
    @SerializedName("NotificationType")
    val notificationType: String,
    val shortBody: String,
    val type: String,
    val read: Boolean,
    val createdAt: String,
    val createdAtFormatted: String,
    val deepLink: String,
    val iconType: String,
    val courseName: String,
    val sessionDate: String,
    val sessionTime: String,
    val location: String
)
