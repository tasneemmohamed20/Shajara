package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

enum class CertificateStatus {
    COMPLETED,
    PENDING_APPROVAL,
    IN_PROGRESS,
    FAILED
}

data class Certificate(
    @SerializedName(value = "id", alternate = ["issueid"])
    val id: Int = 0,
    @SerializedName(value = "courseTitle", alternate = ["name"])
    val courseTitle: String = "N/A",
    val instructorName: String = "N/A",
    @SerializedName(value = "completionDate", alternate = ["timecreated"])
    val completionDate: String = "N/A",
    val grade: Int = 0,
    val status: CertificateStatus = CertificateStatus.COMPLETED,
    @SerializedName(value = "certificateUrl", alternate = ["downloadurl"])
    val certificateUrl: String = "",
    val canDownload: Boolean = true
)
