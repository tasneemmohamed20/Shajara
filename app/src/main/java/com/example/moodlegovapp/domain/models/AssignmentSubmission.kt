package com.example.moodlegovapp.domain.models

// ── Submission ────────────────────────────────────────────────────────────────

data class AssignmentSubmissionStatus(
    val assignmentId: Int,
    val status: String,                        // "not_submitted" | "draft" | "submitted"
    val attemptsUsed: Int,
    val maxAttempts: Int,
    val timeRemaining: String?,
    val deadline: String?,
    val gradingStatus: String,                 // "not_graded" | "graded"
    val grade: Double?,
    val feedback: String?,
    val submittedFiles: List<SubmittedFile>,
    val onlineText: String?,
    val canEdit: Boolean,
    val canSubmit: Boolean,
    val isLate: Boolean,
    val previousAttempts: List<PreviousAttempt>
)

data class SubmittedFile(
    val fileName: String,
    val fileSizeLabel: String?,
    val mimeType: String?,
    val previewUrl: String?
)

data class PreviousAttempt(
    val attemptNumber: Int,
    val status: String,
    val submittedAt: String?,
    val grade: Double?,
    val feedback: String?
)

// ── Submission request payloads ───────────────────────────────────────────────

/**
 * Sent to save/upload a draft submission (text or file reference).
 */
data class AssignmentSubmission(
    val assignmentId: Int,
    val submissionType: String,                // "file" | "onlinetext"
    val draftItemId: Int?,
    val onlineText: String?,
    val comments: String?
)

/**
 * Sent to finalize / officially submit the assignment.
 */
data class AssignmentSubmissionFinalize(
    val assignmentId: Int,
    val submissionId: Int,
    val acceptIntegrityStatement: Boolean,
    val comments: String?
)

// ── Upload response ───────────────────────────────────────────────────────────

data class FileUploadResult(
    val draftItemId: Int,
    val file: UploadedFileInfo
)

data class UploadedFileInfo(
    val fileName: String,
    val fileSizeLabel: String?,
    val mimeType: String?,
    val previewUrl: String?
)

// ── Save/finalize response wrappers ──────────────────────────────────────────

data class SubmissionSaveResponse(
    val success: Boolean,
    val message: String?,
    val data: SubmissionSaveData?
)

data class SubmissionSaveData(
    val submissionId: Int,
    val status: String,
    val savedAt: String?
)

data class SubmissionFinalizeResponse(
    val success: Boolean,
    val message: String?,
    val data: SubmissionFinalizeData?
)

data class SubmissionFinalizeData(
    val submissionId: Int,
    val status: String,
    val submittedAt: String?,
    val gradingStatus: String?
)

// ── Submission status wrapper ─────────────────────────────────────────────────

data class AssignmentSubmissionStatusResponse(
    val success: Boolean,
    val data: AssignmentSubmissionStatus?
)

// ── File upload wrapper ───────────────────────────────────────────────────────

data class FileUploadResponse(
    val success: Boolean,
    val data: FileUploadResult?
)
