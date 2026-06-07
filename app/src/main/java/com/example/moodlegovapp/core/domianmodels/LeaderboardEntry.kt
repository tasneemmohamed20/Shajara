package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val fullName: String,      // "Ahmed Al-Hashims"
    val courseName: String,    // "Criminal Investigation"
    val points: Int,           // 1850
    val avatarUrl: String,
    val isCurrentUser: Boolean
)