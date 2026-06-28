package com.example.moodlegovapp.domain.models

enum class ScheduleFilter {
    TODAY, THIS_WEEK, THIS_MONTH
}

data class TrainingEvent(
    val id: Int = 0,
    val title: String = "",
    val type: String = "event",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val instructorName: String = "",
    val rawTimeStart: Long = 0L,
    val courseId: Int = 0,
    val moduleName: String = ""
)
