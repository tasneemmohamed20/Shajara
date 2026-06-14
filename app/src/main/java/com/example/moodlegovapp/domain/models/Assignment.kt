package com.example.moodlegovapp.domain.models

// 1. Individual assignment/task item model
data class AssignmentItem(
    val id: Int,
    val courseId: Int,
    val courseName: String? = null,
    val cmid: Int,
    val name: String? = null,
    val type: String? = null,
    val intro: String? = null,
    val learningObjective: String? = null,
    val maxAttempts: Int = 0,
    val usedAttempts: Int = 0,
    val maxGrade: Int = 0,
    val gradeWeightPercent: Int = 0,
    val submissionTypes: List<String>? = null,
    val allowedFileTypes: List<String>? = null,
    val maxFileSizeMB: Int = 0,
    val requirements: List<String>? = null,
    val requireIntegrityStatement: Boolean = false,
    val integrityStatementText: String? = null,
    val dueDate: String? = null,
    val dueDateFormatted: String? = null,
    val dueLabel: String? = null,
    val status: String? = null,
    val resources: List<AssignmentResource>? = null
)

// 2. Nested assignment resource links
data class AssignmentResource(
    val name: String? = null,
    val fileSizeLabel: String? = null,
    val mimeType: String? = null,
    val downloadUrl: String? = null
)

// 3. List response envelope — GET user/assignments
data class AssignmentsResponse(
    val success: Boolean,
    val data: AssignmentsData? = null
)

data class AssignmentsData(
    val total: Int,
    val assignments: List<AssignmentItem>
)

// 4. Detail response envelope — GET courses?activity=assignment&assignid=
data class AssignmentDetailResponse(
    val success: Boolean,
    val data: AssignmentItem? = null
)

// 5. Submission payload for upload / text submit
data class AssignmentSubmission(
    val assignmentId: Int,
    val courseId: Int,
    val onlineText: String? = null,
    val fileUrls: List<String> = emptyList(),
    val integrityAccepted: Boolean = false
)

enum class AssignmentStatusFilter {
    ALL, PENDING, OVERDUE, SUBMITTED
}

fun AssignmentItem.displayType(): String =
    type?.takeIf { it.isNotBlank() }
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        ?: "Assignment"

fun AssignmentItem.displayStatus(): String = status.orEmpty()

fun AssignmentItem.displayName(): String = name.orEmpty()

fun AssignmentItem.isOverdue(): Boolean =
    status.orEmpty().equals("overdue", ignoreCase = true)
