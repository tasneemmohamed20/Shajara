package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.GradesRepository
import com.example.moodlegovapp.domain.models.GovGradesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GradesViewModel(private val repository: GradesRepository): ViewModel() {
    private val _data = MutableStateFlow<GovGradesDto?>(null); val data: StateFlow<GovGradesDto?> = _data
    private val _isLoading = MutableStateFlow(false); val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    fun load(){ viewModelScope.launch{ _isLoading.value=true; _error.value=null; try{ when(val r=repository.getGrades()){ is AppResult.Success -> _data.value=r.data; is AppResult.Failure -> _error.value=r.error.errorDescription; is AppResult.Loading -> Unit } } finally { _isLoading.value=false } } }
}
