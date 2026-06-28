package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: Int = 0,
    @SerializedName(value = "title", alternate = ["name"])
    val title: String = "",
    @SerializedName(value = "body", alternate = ["description"])
    val body: String = "",
    @SerializedName("NotificationType")
    val notificationType: String = "event",
    val shortBody: String = "",
    val type: String = "event",
    val read: Boolean = false,
    val createdAt: String = "",
    val createdAtFormatted: String = "",
    val deepLink: String = "",
    val iconType: String = "event",
    val courseName: String = "",
    val sessionDate: String = "",
    val sessionTime: String = "",
    val location: String = ""
)
