package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.TasksRepository
import com.example.moodlegovapp.domain.models.GovTaskDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: TasksRepository): ViewModel() {
    private val _tasks = MutableStateFlow<List<GovTaskDto>>(emptyList())
    val tasks: StateFlow<List<GovTaskDto>> = _tasks
    private val _activeCount = MutableStateFlow(0); val activeCount: StateFlow<Int> = _activeCount
    private val _certificatesCount = MutableStateFlow(0); val certificatesCount: StateFlow<Int> = _certificatesCount
    private val _pendingCount = MutableStateFlow(0); val pendingCount: StateFlow<Int> = _pendingCount
    private val _isLoading = MutableStateFlow(false); val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    fun load() { viewModelScope.launch { _isLoading.value=true; _error.value=null; try { when(val r=repository.getTasks()){ is AppResult.Success->{ _tasks.value=r.data.tasks; _activeCount.value=r.data.activeTasksCount ?: r.data.tasks.size; _certificatesCount.value=r.data.certificatesCount ?: 0; _pendingCount.value=r.data.pendingTasksCount ?: r.data.tasks.count{(it.status ?: "").contains("pending",true)} }; is AppResult.Failure -> _error.value=r.error.errorDescription; is AppResult.Loading -> Unit } } finally { _isLoading.value=false } } }
}
