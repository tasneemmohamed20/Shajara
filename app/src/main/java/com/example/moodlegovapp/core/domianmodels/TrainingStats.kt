package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

data class TrainingStats(
    val activeCourses: Int,
    val activitiesDue: Int,
    val completedCourses: Int
)
