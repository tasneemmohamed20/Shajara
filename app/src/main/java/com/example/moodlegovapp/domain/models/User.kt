package com.example.moodlegovapp.domain.models

// 1. The top-level API Wrapper response
data class UserResponse(
    val success: Boolean,
    val data: UserProfile? = null
)

// 2. The main User Profile (replaces your old User class)
data class UserProfile(
    val id: Int,
    val fullName: String,
    val email: String,
    val profileImageUrl: String,
    val role: String,
    val department: String,
    val institution: String,
    val batch: String,
    val rank: String,
    val rankNumber: Int,
    val level: Int,
    val totalXP: Int,
    val xpToNextLevel: Int,
    val xpProgressPercent: Int,
    val performance: Performance,
    val badges: List<UserBadge>,
    val certificates: List<UserCertificate>,
    val settings: Settings
)

// 3. Nested Performance metrics
data class Performance(
    val overallProgress: Int,
    val overallProgressLabel: String,
    val averageGrade: Int,
    val averageGradeLabel: String,
    val taskCompletion: Int,
    val taskCompletionLabel: String
)

// 4. Nested Badge model
data class UserBadge(
    val id: String,
    val name: String,
    val icon: String,
    val earnedAt: String
)

// 5. Nested Certificate model (with nullability handle for pending state)
data class UserCertificate(
    val id: String,
    val courseName: String,
    val instructorName: String? = null,
    val status: String,
    val approvalStatus: String,
    val completedAtFormatted: String,
    val viewUrl: String?,          // Nullable because cert_2 has it as null
    val downloadUrl: String?,      // Nullable because cert_2 has it as null
    val isAvailable: Boolean,
    val pendingMessage: String? = null // Optional/Nullable for completed ones
)

// 6. Nested Settings model
data class Settings(
    val language: String,
    val notificationsEnabled: Boolean
)
