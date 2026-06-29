package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.CourseResourcesRepository
import com.example.moodlegovapp.domain.models.GovCourseResourcesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseResourcesViewModel(private val repository: CourseResourcesRepository): ViewModel(){
    private val _data = MutableStateFlow<GovCourseResourcesDto?>(null); val data: StateFlow<GovCourseResourcesDto?> = _data
    private val _isLoading = MutableStateFlow(false); val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    fun load(courseId:Int){ viewModelScope.launch{ _isLoading.value=true; _error.value=null; try{ when(val r=repository.getResources(courseId)){ is AppResult.Success -> _data.value=r.data; is AppResult.Failure -> _error.value=r.error.errorDescription; is AppResult.Loading -> Unit } } finally{ _isLoading.value=false } } }
}
