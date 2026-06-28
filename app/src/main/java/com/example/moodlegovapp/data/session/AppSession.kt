package com.example.moodlegovapp.data.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.AuthRepository
import com.example.moodlegovapp.data.repository.UserRepository
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.AuthToken
import com.example.moodlegovapp.domain.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppSession(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository?,
    private val dataStoreManager: DataStoreManager
) {
    private val _authToken = MutableLiveData<AuthToken?>(null)
    val authToken: LiveData<AuthToken?> = _authToken

    private val _currentUser = MutableLiveData<UserProfile?>(null)
    val currentUser: LiveData<UserProfile?> = _currentUser

    private val _isInitialized = MutableLiveData<Boolean>(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    // mirrors iOS: var isAuthenticated
    val isAuthenticated: Boolean get() = _authToken.value != null

    // Auto-restore session on init — mirrors iOS init block
    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = dataStoreManager.get<String>(DataStoreManager.KEY_TOKEN)
                if (token != null) {
                    val privateToken =
                        dataStoreManager.get<String>(DataStoreManager.KEY_PRIVATE_TOKEN)
                    _authToken.postValue(
                        AuthToken(
                            token = token, privateToken = privateToken
                        )
                    )
                    userRepository?.getUserProfile()?.let { result ->
                        if (result is AppResult.Success) {
                            _currentUser.postValue(result.data)
                            dataStoreManager.save(DataStoreManager.KEY_USER_ID, result.data.id)
                        }
                    }
                }
            } finally {
                _isInitialized.postValue(true)
            }
        }
    }

    suspend fun login(
        username: String,
        password: String
    ): AppResult<AuthToken> {

        val result = authRepository.login(
            username = username,
            password = password
        )

        if (result is AppResult.Success) {

            dataStoreManager.save(
                DataStoreManager.KEY_TOKEN,
                result.data.token
            )

            result.data.privateToken?.let {
                dataStoreManager.save(
                    DataStoreManager.KEY_PRIVATE_TOKEN,
                    it
                )
            }

            dataStoreManager.save(
                DataStoreManager.KEY_USERNAME,
                username
            )

            _authToken.postValue(result.data)

            // IMPORTANT:
            // Load profile only after login succeeds
            when (val userResult = userRepository?.getUserProfile()) {

                is AppResult.Success -> {

                    _currentUser.postValue(userResult.data)

                    dataStoreManager.save(
                        DataStoreManager.KEY_USER_ID,
                        userResult.data.id
                    )
                }

                else -> {}
            }
        }

        return result
    }

    suspend fun logout() {
        authRepository.logout()
        dataStoreManager.clearAll()
        _authToken.postValue(null)
        _currentUser.postValue(null)
    }

    fun updateUser(userProfile: UserProfile) {
        _currentUser.postValue(userProfile)
    }
}