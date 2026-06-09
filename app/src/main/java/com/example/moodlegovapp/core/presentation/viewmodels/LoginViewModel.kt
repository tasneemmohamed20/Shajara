package com.example.moodlegovapp.core.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.data.session.AppSession
import com.example.moodlegovapp.core.domain.models.Course
import com.example.moodlegovapp.core.domain.models.Notification
import com.example.moodlegovapp.core.domain.models.User
import com.example.moodlegovapp.core.domain.repositoryinterface.CoursesRepositoryProtocol
import com.example.moodlegovapp.core.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.example.moodlegovapp.core.domain.repositoryinterface.UserRepositoryProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val session: AppSession
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true

            try {
                val result = session.login(
                    username = _username.value,
                    password = _password.value
                )

                when (result) {
                    is AppResult.Success -> {
                        // session already handled token + profile
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