package com.example.moodlegovapp.core.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.models.Course
import com.example.moodlegovapp.core.domain.repositoryinterface.CoursesRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoursesViewModel(
    private val coursesRepository: CoursesRepositoryProtocol
) : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchCourses() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = coursesRepository.getEnrolledCourses()

                when (result) {
                    is AppResult.Success -> {
                        _courses.value = result.data
                    }

                    is AppResult.Failure -> {
                        _errorMessage.value = result.error.errorDescription
                    }

                    is AppResult.Loading -> {
                        // optional
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}