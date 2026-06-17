package com.example.moodlegovapp.presentation.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.domain.models.AssignmentSubmissionStatus
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.models.FileUploadResult
import com.example.moodlegovapp.domain.repositoryinterface.AssignmentsRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Assignments and Assignment Submission screens.
 *
 * Exposes granular StateFlows so the View can react to each concern
 * independently (list loading, detail loading, upload progress, etc.)
 */
class AssignmentsViewModel(
    private val repository: AssignmentsRepositoryProtocol
) : ViewModel() {

    // ── Assignment list ────────────────────────────────────────────────────

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _isLoadingList = MutableStateFlow(false)
    val isLoadingList: StateFlow<Boolean> = _isLoadingList.asStateFlow()

    // ── Assignment detail ──────────────────────────────────────────────────

    private val _selectedAssignment = MutableStateFlow<Assignment?>(null)
    val selectedAssignment: StateFlow<Assignment?> = _selectedAssignment.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    // ── Submission status ──────────────────────────────────────────────────

    private val _submissionStatus = MutableStateFlow<AssignmentSubmissionStatus?>(null)
    val submissionStatus: StateFlow<AssignmentSubmissionStatus?> = _submissionStatus.asStateFlow()

    private val _isLoadingStatus = MutableStateFlow(false)
    val isLoadingStatus: StateFlow<Boolean> = _isLoadingStatus.asStateFlow()

    // ── File upload ────────────────────────────────────────────────────────

    /** Non-null while an upload is in progress; null otherwise. */
    private val _uploadProgress = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadProgress: StateFlow<UploadState> = _uploadProgress.asStateFlow()

    /** Last successfully uploaded file (cleared when a new upload starts). */
    private val _uploadedFile = MutableStateFlow<FileUploadResult?>(null)
    val uploadedFile: StateFlow<FileUploadResult?> = _uploadedFile.asStateFlow()

    // ── Submission result ──────────────────────────────────────────────────

    private val _submissionResult = MutableStateFlow<SubmissionResult>(SubmissionResult.Idle)
    val submissionResult: StateFlow<SubmissionResult> = _submissionResult.asStateFlow()

    // ── Course resources ───────────────────────────────────────────────────

    private val _courseResources = MutableStateFlow<List<CourseResource>>(emptyList())
    val courseResources: StateFlow<List<CourseResource>> = _courseResources.asStateFlow()

    private val _isLoadingResources = MutableStateFlow(false)
    val isLoadingResources: StateFlow<Boolean> = _isLoadingResources.asStateFlow()

    // ── Shared error ───────────────────────────────────────────────────────

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Public actions ─────────────────────────────────────────────────────

    /**
     * Load all assignments for the current user (across all courses).
     * Pass a [courseId] to scope the list to a single course.
     */
    fun fetchAssignments(courseId: Int = -1) {
        viewModelScope.launch {
            _isLoadingList.value = true
            _errorMessage.value  = null
            try {
                val result = if (courseId <= 0) repository.getAllAssignments()
                else               repository.getAssignments(courseId)
                when (result) {
                    is AppResult.Success -> _assignments.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    is AppResult.Loading -> Unit
                }
            } finally {
                _isLoadingList.value = false
            }
        }
    }

    /** Load full detail for a single assignment and its submission status. */
    fun fetchAssignmentDetail(assignmentId: Int) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _errorMessage.value    = null
            try {
                when (val result = repository.getAssignmentDetail(assignmentId)) {
                    is AppResult.Success -> _selectedAssignment.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    is AppResult.Loading -> Unit
                }
            } finally {
                _isLoadingDetail.value = false
            }
        }
        fetchSubmissionStatus(assignmentId)
    }

    /** Refresh the submission status for the currently open assignment. */
    fun fetchSubmissionStatus(assignmentId: Int) {
        viewModelScope.launch {
            _isLoadingStatus.value = true
            try {
                when (val result = repository.getSubmissionStatus(assignmentId)) {
                    is AppResult.Success -> _submissionStatus.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    is AppResult.Loading -> Unit
                }
            } finally {
                _isLoadingStatus.value = false
            }
        }
    }

    /** Upload a file; result is reflected in [uploadProgress] / [uploadedFile]. */
    fun uploadFile(assignmentId: Int, fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            _uploadProgress.value = UploadState.Uploading
            _uploadedFile.value   = null
            _errorMessage.value   = null
            when (val result = repository.uploadFile(assignmentId, fileName, fileBytes)) {
                is AppResult.Success -> {
                    _uploadedFile.value   = result.data
                    _uploadProgress.value = UploadState.Done
                }
                is AppResult.Failure -> {
                    _errorMessage.value   = result.error.errorDescription
                    _uploadProgress.value = UploadState.Error(result.error.errorDescription)
                }
                is AppResult.Loading -> Unit
            }
        }
    }
    fun selectAssignment(assignment: Assignment) {
        _selectedAssignment.value = assignment
        fetchSubmissionStatus(assignment.id)
    }
    /**
     * Save a draft submission (text and/or the draftItemId from a prior upload).
     * Does NOT finalize — call [finalizeSubmission] when the user taps Submit.
     */
    fun saveSubmission(submission: AssignmentSubmission) {
        viewModelScope.launch {
            _submissionResult.value = SubmissionResult.Loading
            when (val result = repository.saveSubmission(submission)) {
                is AppResult.Success -> _submissionResult.value = SubmissionResult.Saved
                is AppResult.Failure -> {
                    _errorMessage.value     = result.error.errorDescription
                    _submissionResult.value = SubmissionResult.Error(result.error.errorDescription)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Finalize and officially submit the assignment.
     * [finalize.acceptIntegrityStatement] must be true or the server (and mock)
     * will return a validation error.
     */
    fun finalizeSubmission(finalize: AssignmentSubmissionFinalize) {
        viewModelScope.launch {
            _submissionResult.value = SubmissionResult.Loading
            when (val result = repository.finalizeSubmission(finalize)) {
                is AppResult.Success -> {
                    _submissionResult.value = SubmissionResult.Submitted
                    // Refresh status so the UI reflects "submitted" state immediately.
                    fetchSubmissionStatus(finalize.assignmentId)
                }
                is AppResult.Failure -> {
                    _errorMessage.value     = result.error.errorDescription
                    _submissionResult.value = SubmissionResult.Error(result.error.errorDescription)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    /** Load downloadable resources for a course. */
    fun fetchCourseResources(courseId: Int) {
        viewModelScope.launch {
            _isLoadingResources.value = true
            _errorMessage.value       = null
            try {
                when (val result = repository.getCourseResources(courseId)) {
                    is AppResult.Success -> _courseResources.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    is AppResult.Loading -> Unit
                }
            } finally {
                _isLoadingResources.value = false
            }
        }
    }

    /** Call after the View has shown the error to prevent re-showing it. */
    fun clearError() { _errorMessage.value = null }

    /** Reset the submission result state (e.g., when navigating away). */
    fun clearSubmissionResult() { _submissionResult.value = SubmissionResult.Idle }

    /** Reset upload state so the user can upload another file. */
    fun clearUpload() {
        _uploadProgress.value = UploadState.Idle
        _uploadedFile.value   = null
    }

    // ── Sealed UI states ───────────────────────────────────────────────────

    sealed class UploadState {
        object Idle      : UploadState()
        object Uploading : UploadState()
        object Done      : UploadState()
        data class Error(val message: String) : UploadState()
    }

    sealed class SubmissionResult {
        object Idle      : SubmissionResult()
        object Loading   : SubmissionResult()
        object Saved     : SubmissionResult()   // draft saved
        object Submitted : SubmissionResult()   // finalized & submitted
        data class Error(val message: String) : SubmissionResult()
    }
}