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

    private val _courseResources = MutableStateFlow<List<MoodleResource>>(emptyList())
    val courseResources: StateFlow<List<MoodleResource>> = _courseResources

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
                // Fetch sections
                val sectionsResult = coursesRepository.getCourseContents(courseId)
                if (sectionsResult is AppResult.Success) {
                    _courseSections.value = sectionsResult.data
                } else if (sectionsResult is AppResult.Failure) {
                    _errorMessage.value = sectionsResult.error.errorDescription
                }

                // Fetch resources to get file URLs
                val resourcesResult = coursesRepository.getCourseResources(courseId)
                if (resourcesResult is AppResult.Success) {
                    _courseResources.value = resourcesResult.data
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = load()
}