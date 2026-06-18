package com.example.moodlegovapp.domain.models
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.google.gson.annotations.SerializedName

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

fun StudentUser.toUserProfile(): UserProfile {
    Log.i("user", this.toString())
    return UserProfile(
        id = this.id ?: 0,
        fullName = this.fullname ?: this.firstname ?: "Student User",
        email = this.email ?: "",
        profileImageUrl = this.profileImageUrl ?: "",
        role = "Student", // Defaulting as not available in StudentUser
        department = this.department ?: "",
        institution = this.institution ?: "",
        batch = "N/A",
        rank = "N/A",
        rankNumber = 0,
        level = 1,
        totalXP = 0,
        xpToNextLevel = 100,
        xpProgressPercent = 0,
        performance = Performance(0, "0%", 0, "0%", 0, "0%"),
        badges = emptyList(),
        certificates = emptyList(),
        settings = Settings(this.lang ?: "en", true)
    )
}


@Serializable
data class StudentUser(
    val id: Int? = null,
    val username: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    val department: String? = null,
    val institution: String? = null,
    val firstaccess: Long? = null,          // Using Long for UNIX timestamps
    val lastaccess: Long? = null,           // Using Long for UNIX timestamps
    val auth: String? = null,
    val suspended: Boolean? = null,
    val confirmed: Boolean? = null,
    val lang: String? = null,
    val theme: String? = null,
    val timezone: String? = null,           // Kept as String since "99" is in quotes

    @SerialName("mailformat")
    @SerializedName("mailformat")
    val mailFormat: Int? = null,

    @SerialName("trackforums")
    @SerializedName("trackforums")
    val trackForums: Int? = null,

    val description: String? = null,

    @SerialName("descriptionformat")
    @SerializedName("descriptionformat")
    val descriptionFormat: Int? = null,

    @SerialName("profileimageurlsmall")
    @SerializedName("profileimageurlsmall")
    val profileImageUrlSmall: String? = null,

    @SerialName("profileimageurl")
    @SerializedName("profileimageurl")
    val profileImageUrl: String? = null
)
