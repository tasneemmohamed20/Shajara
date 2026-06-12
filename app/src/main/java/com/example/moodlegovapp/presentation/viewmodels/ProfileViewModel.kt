package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.Certificate
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepositoryProtocol,
    private val certificatesRepository: CertificatesRepositoryProtocol
) : ViewModel() {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user

    private val _performance = MutableStateFlow<PerformanceOverview?>(null)
    val performance: StateFlow<PerformanceOverview?> = _performance

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    private val _certificates = MutableStateFlow<List<Certificate>>(emptyList())
    val certificates: StateFlow<List<Certificate>> = _certificates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userDeferred = async { userRepository.getUserProfile() }
                val perfDeferred = async { userRepository.getPerformanceOverview() }
                val badgesDeferred = async { userRepository.getBadges() }
                val certsDeferred = async { certificatesRepository.getCertificates() }

                (userDeferred.await() as? AppResult.Success)?.let { _user.value = it.data }
                (perfDeferred.await() as? AppResult.Success)?.let { _performance.value = it.data }
                (badgesDeferred.await() as? AppResult.Success)?.let { _badges.value = it.data }
                (certsDeferred.await() as? AppResult.Success)?.let { _certificates.value = it.data }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }
}
