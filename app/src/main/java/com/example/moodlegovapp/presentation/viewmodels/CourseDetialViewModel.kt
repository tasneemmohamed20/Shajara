package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.CourseDetail
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResources
import com.example.moodlegovapp.domain.models.NextAssignment
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseId: Int,
    private val coursesRepository: CoursesRepositoryProtocol
) : ViewModel() {

    private val _courseDetail = MutableStateFlow<CourseDetail?>(null)
    val courseDetail: StateFlow<CourseDetail?> = _courseDetail

    val modules: StateFlow<List<CourseModule>> = _courseDetail
        .map { it?.modules.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val nextAssignment: StateFlow<NextAssignment?> = _courseDetail
        .map { it?.nextRequiredAssignment }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val courseResources: StateFlow<CourseResources?> = _courseDetail
        .map { it?.courseResources }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val overallProgress: StateFlow<Int> = _courseDetail
        .map { it?.overallProgress ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = coursesRepository.getCourseDetail(courseId)) {
                    is AppResult.Success -> _courseDetail.value = result.data
                    is AppResult.Failure -> _errorMessage.value = result.error.errorDescription
                    else -> Unit
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = load()
}