package com.example.moodlegovapp.core.data.session

import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.data.repository.AuthRepository
import com.example.moodlegovapp.core.data.repository.UserRepository
import com.example.moodlegovapp.core.data.service.SecureStorage
import com.example.moodlegovapp.core.domain.models.AuthToken
import com.example.moodlegovapp.core.domain.models.User
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class AppSession(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository?,
    private val secureStorage: SecureStorage
) {
    // mirrors iOS @Published var authToken
    private val _authToken = MutableLiveData<AuthToken?>(null)
    val authToken: LiveData<AuthToken?> = _authToken

    // mirrors iOS @Published var currentUser
    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    // mirrors iOS: var isAuthenticated
    val isAuthenticated: Boolean get() = authRepository.isLoggedIn

    // Auto-restore session on init — mirrors iOS init block
    init {
        if (secureStorage.isLoggedIn) {
            secureStorage.getToken()?.let { token ->
                _authToken.postValue(AuthToken(
                    token = token,
                    privateToken = secureStorage.getPrivateToken()
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    userRepository?.getUserProfile()?.let { result ->
                        if (result is AppResult.Success) {
                            _currentUser.postValue(result.data)
                            secureStorage.saveUserId(result.data.id)
                        }
                    }
                }
            }
        }
    }

    // mirrors iOS: func login(username:password:) async -> AppResult<AuthToken>
    suspend fun login(username: String, password: String): AppResult<AuthToken> {
        val result = authRepository.login(username, password)

        if (result is AppResult.Success) {
            secureStorage.saveToken(result.data.token)
            result.data.privateToken?.let { secureStorage.savePrivateToken(it) }
            _authToken.postValue(result.data)

            // fetch user profile after login
            userRepository?.getUserProfile()?.let { userResult ->
                if (userResult is AppResult.Success) {
                    _currentUser.postValue(userResult.data)
                    secureStorage.saveUserId(userResult.data.id)
                }
            }
        }

        return result
    }

    // mirrors iOS: func logout()
    fun logout() {
        authRepository.logout()
        secureStorage.clearAll()
        _authToken.postValue(null)
        _currentUser.postValue(null)
    }
}