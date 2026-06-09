package com.example.moodlegovapp.domain.models

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
    val overallProgress: Int = 0,
)
