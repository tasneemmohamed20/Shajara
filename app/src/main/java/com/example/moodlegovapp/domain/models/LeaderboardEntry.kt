package com.example.moodlegovapp.domain.models

// 1. Top-level API Wrapper Response
data class LeaderboardResponse(
    val success: Boolean,
    val data: LeaderboardData? = null
)

// 2. The core Leaderboard data wrapper
data class LeaderboardData(
    val currentUserRank: Int,
    val totalParticipants: Int,
    val leaderboard: List<LeaderboardEntry>
)

// 3. Individual leaderboard row items
data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val fullName: String,
    val profileImageUrl: String,
    val xp: Int,
    val level: Int,
    val course: String?,              // Nullable because items 2, 4, 5, and 6 return null
    val isCurrentUser: Boolean,
    val xpThisWeek: Int? = null       // Nullable/Optional because only the current user profile has this
)