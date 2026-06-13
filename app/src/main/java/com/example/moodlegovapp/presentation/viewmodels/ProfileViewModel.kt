package com.example.moodlegovapp.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.data.session.AppSession
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.models.UserResponse
import com.example.moodlegovapp.domain.repositoryinterface.CertificatesRepositoryProtocol
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepositoryProtocol,
    private val certificatesRepository: CertificatesRepositoryProtocol,
    private val session: AppSession
) : ViewModel() {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user

    private val _profileResponse = MutableStateFlow<UserResponse?>(null)
    val profileResponse: StateFlow<UserResponse?> = _profileResponse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        hydrateFromSession()
    }

    private fun hydrateFromSession() {
        session.currentUser.value?.let { cached ->
            _user.value = cached
            _profileResponse.value = UserResponse(success = true, data = cached)
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            val hasCachedProfile = _profileResponse.value?.data != null
            if (!hasCachedProfile) {
                _isLoading.value = true
            }
            _errorMessage.value = null
            try {
                when (val result = userRepository.getUserProfile()) {
                    is AppResult.Success -> {
                        val userProfile = result.data
                        _user.value = userProfile
                        _profileResponse.value = UserResponse(success = true, data = userProfile)
                        Log.d(TAG, "Profile loaded for ${userProfile.fullName}")
                    }
                    is AppResult.Failure -> {
                        Log.e(TAG, "Profile load failed: ${result.error}")
                        _errorMessage.value = result.error.errorDescription
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "Profile load threw", e)
                _errorMessage.value = e.localizedMessage ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() = loadAll()

    fun toggleNotifications(enabled: Boolean) {
        _user.value?.let { currentUser ->
            val updatedUser = currentUser.copy(
                settings = currentUser.settings.copy(notificationsEnabled = enabled)
            )
            _user.value = updatedUser
            _profileResponse.value = UserResponse(success = true, data = updatedUser)
        }
    }

    fun toggleLanguage(context: Context) {
        val currentUser = _user.value ?: return
        val currentLang = currentUser.settings.language
        val nextLang = if (currentLang.lowercase() == "ar") "en" else "ar"

        viewModelScope.launch {
            val dsm = DataStoreManager.getInstance(context)
            dsm.save(DataStoreManager.KEY_LANGUAGE, nextLang)

            val updatedUser = currentUser.copy(
                settings = currentUser.settings.copy(language = nextLang)
            )
            _user.value = updatedUser
            _profileResponse.value = UserResponse(success = true, data = updatedUser)

            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(nextLang)
            )
        }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
