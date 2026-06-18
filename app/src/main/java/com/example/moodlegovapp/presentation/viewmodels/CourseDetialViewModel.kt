package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.*
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseId: Int,
    val courseName: String,
    val progress: Int,
    private val coursesRepository: CoursesRepositoryProtocol
) : ViewModel() {

    private val _courseSections = MutableStateFlow<List<CourseSection>>(emptyList())
    val courseSections: StateFlow<List<CourseSection>> = _courseSections

    val modules: StateFlow<List<CourseModule>> = _courseSections
        .map { sections -> sections.flatMap { it.modules } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val overallProgress: StateFlow<Int> = MutableStateFlow(progress)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                when (val result = coursesRepository.getCourseContents(courseId)) {
                    is AppResult.Success -> _courseSections.value = result.data
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