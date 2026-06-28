package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

// Supports both the old mock wrapper and the real Moodle response.
data class LeaderboardResponse(
    val success: Boolean? = null,
    val data: LeaderboardData? = null,
    val courseid: Int? = null,
    val ranking: List<MoodleRankingEntry> = emptyList(),
    val warnings: List<Any> = emptyList()
)

data class MoodleRankingEntry(
    val rank: Int = 0,
    @SerializedName("userid") val userId: Int = 0,
    @SerializedName("fullname") val fullName: String = "",
    val email: String? = null,
    @SerializedName("finalgrade") val finalGrade: Int? = null,
    val percentage: Int? = null
)

data class LeaderboardData(
    val currentUserRank: Int = 0,
    val totalParticipants: Int = 0,
    val leaderboard: List<LeaderboardEntry> = emptyList()
)

data class LeaderboardEntry(
    val rank: Int = 0,
    val userId: Int = 0,
    val fullName: String = "",
    val profileImageUrl: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val course: String? = null,
    val isCurrentUser: Boolean = false,
    val xpThisWeek: Int? = null
)
