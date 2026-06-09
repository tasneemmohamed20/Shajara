package com.example.moodlegovapp.core.domain.models

enum class SubmissionStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    GRADED,
    LATE
}

enum class SubmissionType {
    FILE_UPLOAD,
    TEXT_ENTRY
}

data class Assignment(
    val id: Int,
    val title: String,              // "Crime Scene Analysis Report"
    val courseTitle: String,        // "CRIMINAL INVESTIGATION"
    val courseCategory: String,     // "Crime Scene Analysis"
    val dueInDays: Int,             // 3
    val timeRemaining: String,      // "2 Days, 4 Hours"
    val deadline: String,           // "Oct 24, 11:59 PM"
    val overview: String,
    val learningObjective: String,
    val attempts: String,           // "1/2"
    val submissionFormats: List<String>, // ["PDF","DOCX"]
    val grading: String,            // "100 Points"
    val cutOffDate: String,         // "15% of Final"
    val detailedInstructions: String,
    val resources: List<CourseResource>,
    val submissionStatus: SubmissionStatus,
    val submissionType: SubmissionType,
    val requirements: List<String>  // integrity rules
)

data class AssignmentSubmission(
    val assignmentId: Int,
    val submissionType: SubmissionType,
    val uploadedFiles: List<UploadedFile>,
    val textEntry: String?,
    val hasConfirmedIntegrity: Boolean,
    val comments: String?
)

data class UploadedFile(
    val fileName: String,     // "crime_scene_report_v..."
    val fileSize: String,     // "2.4 MB"
    val status: String,       // "Uploaded"
    val fileType: String      // PDF / DOCX / JPG
)