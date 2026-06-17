package com.example.moodlegovapp.data.repository
import android.util.Log
import com.example.moodlegovapp.data.network.AppError
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.MockApiService
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.repositoryinterface.AssignmentsRepositoryProtocol
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "AssignmentsRepo"

/**
 * Single-source-of-truth for assignment data.
 *
 * Strategy:
 *  1. Attempt the live [retrofit] endpoint via [NetworkCallHandler].
 *  2. On any recoverable error, fall back to [mock] (local JSON).
 *
 * This mirrors the FallbackApiService pattern already in the project so the
 * approach is consistent across all feature modules.
 */
class AssignmentsRepository(
    private val retrofit:        RetrofitApiService,
    private val mock:            MockApiService,
    private val dataStoreManager: DataStoreManager,
    private val retryPolicy:     RetryPolicy = RetryPolicy.DEFAULT
) : AssignmentsRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    // ── Fallback helper ────────────────────────────────────────────────────

    private suspend fun <T> withFallback(
        tag:         String,
        networkCall: suspend () -> AppResult<T>,
        mockCall:    suspend () -> AppResult<T>
    ): AppResult<T> {
        val result = try { networkCall() }
        catch (e: Exception) {
            Log.w(TAG, "$tag threw ${e.message} — falling back to mock")
            return mockCall()
        }
        return when {
            result is AppResult.Success -> result
            result is AppResult.Failure && result.error.isFallbackable() -> {
                Log.w(TAG, "$tag network failure (${result.error}) — using local mock")
                mockCall()
            }
            else -> result
        }
    }

    // UPDATED: Broadened fallback trigger!
    // Now it will ALWAYS fall back to MockApiService unless the user is completely unauthorized.
    private fun AppError.isFallbackable(): Boolean = this !is AppError.Unauthorized

    // ── Listing ────────────────────────────────────────────────────────────

    override suspend fun getAllAssignments(): AppResult<List<Assignment>> =
        withFallback(
            tag = "getAllAssignments",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.getAssignments(userId())
                }) {
                    is AppResult.Success -> AppResult.Success(r.data.data?.assignments ?: emptyList())
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.getAssignments(courseId = -1) }
        )

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> =
        withFallback(
            tag = "getAssignments(courseId=$courseId)",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.getAssignmentsByCourse(userId(), courseId)
                }) {
                    is AppResult.Success -> AppResult.Success(r.data.data?.assignments ?: emptyList())
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.getAssignments(courseId) }
        )

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> =
        withFallback(
            tag = "getAssignmentDetail($assignmentId)",
            networkCall = {
                when (val r = getAllAssignments()) {
                    is AppResult.Success -> {
                        Log.d("AssignmentsRepo", "All IDs: ${r.data.map { it.id }}")
                        r.data.find { it.id == assignmentId }
                            ?.let { AppResult.Success(it) }
                            ?: AppResult.Failure(AppError.NotFound)
                    }
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = {
                Log.d("AssignmentsRepo", "Falling back to mock for assignmentId=$assignmentId")
                mock.getAssignmentDetail(assignmentId)
            }
        )
    // ── Submission status ──────────────────────────────────────────────────

    override suspend fun getSubmissionStatus(assignmentId: Int): AppResult<AssignmentSubmissionStatus> =
        withFallback(
            tag = "getSubmissionStatus($assignmentId)",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.getSubmissionStatus(assignmentId, userId())
                }) {
                    is AppResult.Success -> r.data.data
                        ?.let { AppResult.Success(it) }
                        ?: AppResult.Failure(AppError.DecodingError)
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.getSubmissionStatus(assignmentId) }
        )

    // ── Submit ─────────────────────────────────────────────────────────────

    override suspend fun saveSubmission(submission: AssignmentSubmission): AppResult<Unit> =
        withFallback(
            tag = "saveSubmission(assignmentId=${submission.assignmentId})",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.saveSubmission(submission.assignmentId, userId(), submission)
                }) {
                    is AppResult.Success -> AppResult.Success(Unit)
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.submitAssignment(submission) }
        )

    override suspend fun finalizeSubmission(finalize: AssignmentSubmissionFinalize): AppResult<Unit> =
        withFallback(
            tag = "finalizeSubmission(assignmentId=${finalize.assignmentId})",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.finalizeSubmission(finalize.assignmentId, userId(), finalize)
                }) {
                    is AppResult.Success -> AppResult.Success(Unit)
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.finalizeSubmission(finalize).map { } }
        )

    // ── File upload ────────────────────────────────────────────────────────

    override suspend fun uploadFile(
        assignmentId: Int,
        fileName:     String,
        fileBytes:    ByteArray
    ): AppResult<FileUploadResult> =
        withFallback(
            tag = "uploadFile(assignmentId=$assignmentId, file=$fileName)",
            networkCall = {
                val mimeType  = guessMimeType(fileName)
                val reqBody   = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part      = MultipartBody.Part.createFormData("file", fileName, reqBody)
                val draftBody = "0".toRequestBody("text/plain".toMediaTypeOrNull())

                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.uploadAssignmentFile(assignmentId, userId(), part, draftBody)
                }) {
                    is AppResult.Success -> r.data.data
                        ?.let { AppResult.Success(it) }
                        ?: AppResult.Failure(AppError.DecodingError)
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.uploadFile(assignmentId, fileName, fileBytes.size.toLong()) }
        )

    // ── Resources ──────────────────────────────────────────────────────────

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> =
        withFallback(
            tag = "getCourseResources(courseId=$courseId)",
            networkCall = {
                when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                    retrofit.getCourseResources(courseId, userId())
                }) {
                    is AppResult.Success -> r.data.data?.resources
                        ?.let { AppResult.Success(it) }
                        ?: AppResult.Success(emptyList())
                    is AppResult.Failure -> r
                    is AppResult.Loading -> AppResult.Loading
                }
            },
            mockCall = { mock.getCourseResources(courseId) }
        )

    // ── Private helpers ────────────────────────────────────────────────────

    private fun guessMimeType(fileName: String): String = when {
        fileName.endsWith(".pdf",  ignoreCase = true) -> "application/pdf"
        fileName.endsWith(".docx", ignoreCase = true) ->
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        fileName.endsWith(".jpg",  ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        fileName.endsWith(".png",  ignoreCase = true) -> "image/png"
        fileName.endsWith(".zip",  ignoreCase = true) -> "application/zip"
        else                                          -> "application/octet-stream"
    }
}

// ── AppResult.map extension (add once to a shared file if not present) ────────
private fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
    is AppResult.Loading -> AppResult.Loading
}