package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.PasswordResetResult
import com.example.moodlegovapp.domain.repositoryinterface.AuthRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(private val authRepository: AuthRepositoryProtocol): ViewModel(){
    private val _email = MutableStateFlow(""); val email: StateFlow<String> = _email
    private val _isLoading = MutableStateFlow(false); val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null); val error: StateFlow<String?> = _error
    private val _result = MutableStateFlow<PasswordResetResult?>(null); val result: StateFlow<PasswordResetResult?> = _result
    private val _showCheckEmail = MutableStateFlow(false); val showCheckEmail: StateFlow<Boolean> = _showCheckEmail
    fun onEmailChange(v:String){ _email.value=v }
    fun requestReset(){ viewModelScope.launch{ val e=_email.value.trim(); if(e.isBlank()){_error.value="Enter your email"; return@launch}; _isLoading.value=true; _error.value=null; try{ when(val r=authRepository.requestPasswordReset(e)){ is AppResult.Success->{ _result.value=r.data; _showCheckEmail.value = r.data.status == "emailpasswordconfirmmaybesent" || r.data.status.contains("sent", true) }; is AppResult.Failure -> _error.value=r.error.errorDescription; is AppResult.Loading -> Unit } } finally{ _isLoading.value=false } } }
    fun consumeNavigation(){ _showCheckEmail.value=false }
}
