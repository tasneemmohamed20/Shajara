package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

enum class ScheduleFilter {
    TODAY, THIS_WEEK, THIS_MONTH
}

data class TrainingEvent(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val date: String,
    val location: String,
    val instructorName: String
)
