package com.example.moodlegovapp.domain.models

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
