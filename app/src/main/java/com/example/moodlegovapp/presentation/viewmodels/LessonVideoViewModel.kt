package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.LessonRepository
import com.example.moodlegovapp.domain.models.GovLessonDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LessonVideoViewModel(private val repository: LessonRepository): ViewModel(){
    private val _lesson = MutableStateFlow<GovLessonDto?>(null); val lesson: StateFlow<GovLessonDto?> = _lesson
    private val _isLoading = MutableStateFlow(false); val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    fun load(cmid:Int){ viewModelScope.launch{ _isLoading.value=true; _error.value=null; try{ when(val r=repository.getLesson(cmid)){ is AppResult.Success -> _lesson.value=r.data; is AppResult.Failure -> _error.value=r.error.errorDescription; is AppResult.Loading -> Unit } } finally{ _isLoading.value=false } } }
}
