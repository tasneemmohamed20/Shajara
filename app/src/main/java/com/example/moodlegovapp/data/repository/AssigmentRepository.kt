package com.example.moodlegovapp.data.repository

import android.util.Log
import com.example.moodlegovapp.data.network.AppError
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.db.PendingActionType
import com.example.moodlegovapp.data.offline.sync.PendingActionQueue
import com.example.moodlegovapp.data.offline.sync.PendingFileUpload
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.models.MoodleAssignment
import com.example.moodlegovapp.domain.models.MoodleResource
import com.example.moodlegovapp.domain.repositoryinterface.AssignmentsRepositoryProtocol
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "AssignmentsRepo"

/**
 * Assignment repository backed by the real Moodle API only.
 *
 * Local Room cache is still used for offline reads and pending actions are still
 * queued for offline writes, but there is no Postman/local mock fallback.
 */
class AssignmentsRepository(
    private val retrofit: RetrofitApiService,
    private val dataStoreManager: DataStoreManager,
    private val offlineCache: OfflineCache,
    private val connectivity: ConnectivityObserver,
    private val pendingActions: PendingActionQueue,
    private val retryPolicy: RetryPolicy = RetryPolicy.DEFAULT
) : AssignmentsRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 0

    private suspend fun <T> realCall(
        tag: String,
        networkCall: suspend () -> AppResult<T>
    ): AppResult<T> = try {
        networkCall()
    } catch (e: Exception) {
        Log.e(TAG, "$tag failed", e)
        AppResult.Failure(AppError.NetworkError(e.message ?: "Network request failed"))
    }

    override suspend fun getAllAssignments(): AppResult<List<MoodleAssignment>> =
        offlineCache.fetch(
            key = "all_assignments:${userId()}",
            typeToken = object : TypeToken<List<MoodleAssignment>>() {},
            networkCall = { fetchAllAssignmentsRemote() }
        )


    private fun govTaskToAssignment(t: com.example.moodlegovapp.domain.models.GovTaskDto): MoodleAssignment =
        MoodleAssignment(
            id = t.assignId ?: t.id ?: 0,
            cmid = t.cmid ?: 0,
            course = 0,
            name = t.title ?: "Task",
            grade = t.gradePercent ?: 0,
            intro = t.courseName ?: "",
            dueDate = t.dueDate ?: 0L
        )

    private fun govDetailsToAssignment(d: com.example.moodlegovapp.domain.models.GovAssignmentDetailsDto): MoodleAssignment =
        MoodleAssignment(
            id = d.assignId ?: d.id ?: 0,
            cmid = d.cmid ?: 0,
            course = 0,
            name = d.assignmentTitle ?: d.title ?: "Assignment",
            grade = 0,
            intro = d.description ?: d.courseName ?: "",
            dueDate = d.dueDate ?: 0L
        )

    private suspend fun fetchAllAssignmentsRemote(): AppResult<List<MoodleAssignment>> =
        realCall(tag = "getAllAssignments") {
            when (val r = NetworkCallHandler.safeCall(retryPolicy) { retrofit.getGovTasks() }) {
                is AppResult.Success -> AppResult.Success(r.data.tasks.map { govTaskToAssignment(it) })
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun getAssignments(courseId: Int): AppResult<List<MoodleAssignment>> =
        offlineCache.fetch(
            key = OfflineCache.assignmentsKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<List<MoodleAssignment>>() {},
            networkCall = { fetchAssignmentsRemote(courseId) }
        )

    private suspend fun fetchAssignmentsRemote(courseId: Int): AppResult<List<MoodleAssignment>> =
        fetchAllAssignmentsRemote()

    override suspend fun getAssignmentDetail(courseId: Int, assignmentId: Int): AppResult<MoodleAssignment> =
        offlineCache.fetch(
            key = OfflineCache.assignmentDetailKey(assignmentId),
            courseId = courseId,
            typeToken = object : TypeToken<MoodleAssignment>() {},
            networkCall = { fetchAssignmentDetailRemote(courseId, assignmentId) }
        )

    private suspend fun fetchAssignmentDetailRemote(courseId: Int, assignmentId: Int): AppResult<MoodleAssignment> =
        realCall(tag = "getAssignmentDetail($assignmentId)") {
            when (val r = NetworkCallHandler.safeCall(retryPolicy) { retrofit.getGovAssignmentDetails(assignmentId) }) {
                is AppResult.Success -> AppResult.Success(govDetailsToAssignment(r.data))
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun getSubmissionStatus(assignmentId: Int): AppResult<AssignmentSubmissionStatus> =
        offlineCache.fetch(
            key = "submission_status:$assignmentId",
            typeToken = object : TypeToken<AssignmentSubmissionStatus>() {},
            networkCall = { fetchSubmissionStatusRemote(assignmentId) }
        )

    private suspend fun fetchSubmissionStatusRemote(assignmentId: Int): AppResult<AssignmentSubmissionStatus> =
        realCall(tag = "getSubmissionStatus($assignmentId)") {
            when (val r = NetworkCallHandler.safeCall(retryPolicy) { retrofit.getGovAssignmentDetails(assignmentId) }) {
                is AppResult.Success -> {
                    val d = r.data
                    AppResult.Success(
                        AssignmentSubmissionStatus(
                            assignmentId = assignmentId,
                            status = d.submissionStatus ?: "not_submitted",
                            attemptsUsed = 0,
                            maxAttempts = 0,
                            timeRemaining = null,
                            deadline = (d.dueDate ?: 0L).takeIf { it > 0 }?.toString(),
                            gradingStatus = "not_graded",
                            grade = null,
                            feedback = null,
                            submittedFiles = emptyList(),
                            onlineText = null,
                            canEdit = true,
                            canSubmit = true,
                            isLate = false,
                            previousAttempts = emptyList()
                        )
                    )
                }
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun saveSubmission(submission: AssignmentSubmission): AppResult<Unit> {
        if (!connectivity.isOnlineNow()) {
            pendingActions.enqueue(PendingActionType.SAVE_SUBMISSION, submission)
            return AppResult.Success(Unit)
        }
        val result = saveSubmissionRemote(submission)
        if (result is AppResult.Failure && result.error.isRetryable) {
            pendingActions.enqueue(PendingActionType.SAVE_SUBMISSION, submission)
            return AppResult.Success(Unit)
        }
        return result
    }

    private suspend fun saveSubmissionRemote(submission: AssignmentSubmission): AppResult<Unit> =
        realCall(tag = "submitAssignment(assignId=${submission.assignmentId})") {
            val type = when {
                submission.draftItemId != null -> "file_upload"
                submission.submissionType.contains("file", ignoreCase = true) -> "file_upload"
                else -> "online_text"
            }
            when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                retrofit.submitGovAssignment(
                    assignId = submission.assignmentId,
                    submissionType = type,
                    onlineText = submission.onlineText,
                    fileDraftItemId = submission.draftItemId?.toLong()
                )
            }) {
                is AppResult.Success -> AppResult.Success(Unit)
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun finalizeSubmission(finalize: AssignmentSubmissionFinalize): AppResult<Unit> {
        if (!connectivity.isOnlineNow()) {
            pendingActions.enqueue(PendingActionType.FINALIZE_SUBMISSION, finalize)
            return AppResult.Success(Unit)
        }
        val result = finalizeSubmissionRemote(finalize)
        if (result is AppResult.Failure && result.error.isRetryable) {
            pendingActions.enqueue(PendingActionType.FINALIZE_SUBMISSION, finalize)
            return AppResult.Success(Unit)
        }
        return result
    }

    private suspend fun finalizeSubmissionRemote(finalize: AssignmentSubmissionFinalize): AppResult<Unit> =
        realCall(tag = "finalizeSubmission(assignId=${finalize.assignmentId})") {
            when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                retrofit.submitGovAssignment(
                    assignId = finalize.assignmentId,
                    submissionType = "online_text",
                    onlineText = finalize.comments,
                    fileDraftItemId = null
                )
            }) {
                is AppResult.Success -> AppResult.Success(Unit)
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun uploadFile(
        assignmentId: Int,
        fileName: String,
        fileBytes: ByteArray
    ): AppResult<FileUploadResult> {
        if (!connectivity.isOnlineNow()) {
            pendingActions.enqueue(
                PendingActionType.UPLOAD_FILE,
                PendingFileUpload(
                    assignmentId = assignmentId,
                    fileName = fileName,
                    fileBytesBase64 = android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP)
                )
            )
            return AppResult.Failure(AppError.NetworkError("لا يوجد اتصال بالإنترنت، سيتم رفع الملف عند الاتصال مجدداً"))
        }
        return uploadFileRemote(assignmentId, fileName, fileBytes)
    }

    private suspend fun uploadFileRemote(
        assignmentId: Int,
        fileName: String,
        fileBytes: ByteArray
    ): AppResult<FileUploadResult> =
        realCall(tag = "uploadFile(assignmentId=$assignmentId, file=$fileName)") {
            val mimeType = guessMimeType(fileName)
            val reqBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, reqBody)
            val tokenBody = com.example.moodlegovapp.data.network.NetworkConfig.WS_TOKEN.toRequestBody("text/plain".toMediaTypeOrNull())
            val fileAreaBody = "draft".toRequestBody("text/plain".toMediaTypeOrNull())
            val itemIdBody = "0".toRequestBody("text/plain".toMediaTypeOrNull())
            val filePathBody = "/".toRequestBody("text/plain".toMediaTypeOrNull())
            val filenameBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())

            when (val r = NetworkCallHandler.safeCall(retryPolicy) {
                retrofit.uploadDraftFile(tokenBody, fileAreaBody, itemIdBody, filePathBody, filenameBody, part)
            }) {
                is AppResult.Success -> {
                    val uploaded = r.data.firstOrNull()
                    if (uploaded != null) {
                        AppResult.Success(
                            FileUploadResult(
                                draftItemId = uploaded.itemId.toInt(),
                                file = com.example.moodlegovapp.domain.models.UploadedFileInfo(
                                    fileName = uploaded.filename,
                                    fileSizeLabel = null,
                                    mimeType = uploaded.mimetype,
                                    previewUrl = uploaded.fileurl ?: uploaded.url
                                )
                            )
                        )
                    } else AppResult.Failure(AppError.DecodingError)
                }
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    override suspend fun getCourseResources(courseId: Int): AppResult<List<MoodleResource>> =
        offlineCache.fetch(
            key = OfflineCache.courseResourcesKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<List<MoodleResource>>() {},
            networkCall = { fetchCourseResourcesRemote(courseId) }
        )

    private suspend fun fetchCourseResourcesRemote(courseId: Int): AppResult<List<MoodleResource>> =
        realCall(tag = "getCourseResources(courseId=$courseId)") {
            when (val r = NetworkCallHandler.safeCall(retryPolicy) { retrofit.getCourseResources(courseId) }) {
                is AppResult.Success -> AppResult.Success(r.data.resources)
                is AppResult.Failure -> r
                is AppResult.Loading -> AppResult.Loading
            }
        }

    private fun guessMimeType(fileName: String): String = when {
        fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
        fileName.endsWith(".docx", ignoreCase = true) ->
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        fileName.endsWith(".jpg", ignoreCase = true) ||
            fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        fileName.endsWith(".png", ignoreCase = true) -> "image/png"
        fileName.endsWith(".zip", ignoreCase = true) -> "application/zip"
        else -> "application/octet-stream"
    }
}
