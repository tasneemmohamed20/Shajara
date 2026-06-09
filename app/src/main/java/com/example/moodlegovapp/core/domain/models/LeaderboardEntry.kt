package com.example.moodlegovapp.core.domain.models

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val fullName: String,      // "Ahmed Al-Hashims"
    val courseName: String,    // "Criminal Investigation"
    val points: Int,           // 1850
    val avatarUrl: String,
    val isCurrentUser: Boolean
)