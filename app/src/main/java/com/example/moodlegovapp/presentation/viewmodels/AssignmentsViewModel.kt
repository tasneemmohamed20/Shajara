package com.example.moodlegovapp.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.AssignmentItem
import com.example.moodlegovapp.domain.models.AssignmentStatusFilter
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.repositoryinterface.IAssignmentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssignmentsViewModel(
    private val assignmentsRepository: IAssignmentsRepository
) : ViewModel() {

    init {
        Log.d(TAG, "Initialized AssignmentsViewModel")
    }

    private val _assignments = MutableStateFlow<List<AssignmentItem>>(emptyList())
    val assignments: StateFlow<List<AssignmentItem>> = _assignments

    private val _selectedAssignment = MutableStateFlow<AssignmentItem?>(null)
    val selectedAssignment: StateFlow<AssignmentItem?> = _selectedAssignment

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _statusFilter = MutableStateFlow(AssignmentStatusFilter.ALL)
    val statusFilter: StateFlow<AssignmentStatusFilter> = _statusFilter

    private val _courseFilter = MutableStateFlow<Int?>(null)
    val courseFilter: StateFlow<Int?> = _courseFilter

    val filteredAssignments: StateFlow<List<AssignmentItem>> = combine(
        _assignments,
        _statusFilter,
        _courseFilter
    ) { items, status, courseId ->
        items
            .filter { courseId == null || it.courseId == courseId }
            .filter { matchesStatusFilter(it, status) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingCount: StateFlow<Int> = _assignments
        .combine(_courseFilter) { items, courseId ->
            items
                .filter { courseId == null || it.courseId == courseId }
                .count { it.status.orEmpty().equals("pending", ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val overdueCount: StateFlow<Int> = _assignments
        .combine(_courseFilter) { items, courseId ->
            items
                .filter { courseId == null || it.courseId == courseId }
                .count { it.status.orEmpty().equals("overdue", ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _detailErrorMessage = MutableStateFlow<String?>(null)
    val detailErrorMessage: StateFlow<String?> = _detailErrorMessage

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    fun loadAll(courseId: Int) {
        viewModelScope.launch {
            val hasCached = _assignments.value.isNotEmpty()
            if (!hasCached) _isLoading.value = true
            _errorMessage.value = null
            try {
                when (val result = assignmentsRepository.getAllAssignments(courseId)) {
                    is AppResult.Success -> {
                        _assignments.value = result.data
                        _totalCount.value = result.data.size
                        Log.d(TAG, "Loaded ${result.data.size} assignments")
                    }
                    is AppResult.Failure -> {
                        Log.e(TAG, "Failed to load assignments: ${result.error}")
                        _errorMessage.value = result.error.errorDescription
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAll threw", e)
                _errorMessage.value = e.localizedMessage ?: "Failed to load assignments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh(courseId: Int) = loadAll(courseId)

    fun setStatusFilter(filter: AssignmentStatusFilter) {
        _statusFilter.value = filter
    }

    fun setCourseFilter(courseId: Int?) {
        _courseFilter.value = courseId
    }

    fun selectAssignment(assignment: AssignmentItem) {
        _selectedAssignment.value = assignment
        _detailErrorMessage.value = null
    }

    fun clearSelection() {
        _selectedAssignment.value = null
        _detailErrorMessage.value = null
    }

    /** Loads all user assignments for a course and selects the one matching [assignmentId]. */
    fun selectAssignmentById(courseId: Int, assignmentId: Int) {
        Log.d(TAG, "selectAssignmentById called with courseId: $courseId, assignmentId: $assignmentId")
        viewModelScope.launch {
            _isDetailLoading.value = true
            _detailErrorMessage.value = null
            try {
                val cached = _assignments.value.find { it.id == assignmentId }
                if (cached != null) {
                    Log.d(TAG, "Found assignment $assignmentId in cache")
                    _selectedAssignment.value = cached
                } else {
                    Log.d(TAG, "Assignment $assignmentId not in cache. Fetching for course $courseId")
                    when (val result = assignmentsRepository.getAllAssignments(courseId)) {
                        is AppResult.Success -> {
                            Log.d(TAG, "Successfully fetched ${result.data.size} assignments for course $courseId")
                            _assignments.value = result.data
                            _totalCount.value = result.data.size
                            result.data.find { it.id == assignmentId }?.let { match ->
                                Log.d(TAG, "Matched assignment $assignmentId from network response")
                                _selectedAssignment.value = match
                            } ?: run {
                                Log.e(TAG, "Assignment $assignmentId not found in the fetched data")
                                _detailErrorMessage.value = "Assignment not found"
                            }
                        }
                        is AppResult.Failure -> {
                            Log.e(TAG, "Failed to fetch assignments: ${result.error}")
                            _detailErrorMessage.value = result.error.errorDescription
                        }
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in selectAssignmentById", e)
                _detailErrorMessage.value = e.localizedMessage ?: "Failed to load assignment"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun submitAssignment(submission: AssignmentSubmission) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitSuccess.value = false
            _errorMessage.value = null
            try {
                when (val result = assignmentsRepository.submitAssignment(submission)) {
                    is AppResult.Success -> {
                        _submitSuccess.value = true
                        loadAll(submission.courseId)
                        selectAssignmentById(submission.courseId, submission.assignmentId)
                    }
                    is AppResult.Failure -> {
                        _errorMessage.value = result.error.errorDescription
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to submit assignment"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearSubmitSuccess() {
        _submitSuccess.value = false
    }

    private fun matchesStatusFilter(item: AssignmentItem, filter: AssignmentStatusFilter): Boolean {
        val status = item.status.orEmpty()
        return when (filter) {
            AssignmentStatusFilter.ALL -> true
            AssignmentStatusFilter.PENDING ->
                status.equals("pending", ignoreCase = true)
            AssignmentStatusFilter.OVERDUE ->
                status.equals("overdue", ignoreCase = true)
            AssignmentStatusFilter.SUBMITTED ->
                status.equals("submitted", ignoreCase = true) ||
                    status.equals("completed", ignoreCase = true)
        }
    }

    companion object {
        private const val TAG = "AssignmentsViewModel"
    }
}
