package com.example.moodlegovapp.core.domianmodels
import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val fullName: String,
    val role: String,
    val profileImageUrl: String,
    val level: Int,
    val rank: String,
    val totalXP: Int,
    val xpToNextLevel: Int,
    val nextLevel: Int,
    val overallProgress: Int,
    val averageGrade: Int,
    val taskCompletion: Int
)
