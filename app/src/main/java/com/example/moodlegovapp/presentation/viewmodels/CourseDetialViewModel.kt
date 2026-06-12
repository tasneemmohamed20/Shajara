package com.example.moodlegovapp.presentation.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseId: Int,
    private val coursesRepository: CoursesRepositoryProtocol
) : ViewModel() {

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = coursesRepository.getCourseDetail(courseId)

                if (result is AppResult.Success) {
                    _course.value = result.data
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}