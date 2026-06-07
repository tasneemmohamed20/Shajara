package com.example.moodlegovapp.core.domianmodels

enum class CertificateStatus {
    COMPLETED,
    PENDING_APPROVAL,
    IN_PROGRESS,
    FAILED
}

data class Certificate(
    val id: Int,
    val courseTitle: String,    // "Advanced Criminal Investigation"
    val instructorName: String,
    val completionDate: String, // "Oct 12, 2024"
    val grade: Int,             // 95
    val status: CertificateStatus,
    val certificateUrl: String,
    val canDownload: Boolean
)