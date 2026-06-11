package com.example.moodlegovapp.data.repository

import android.util.Log
import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppError
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.MockApiService
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.network.datasource.AuthDataSource
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.data.service.DataStoreManager.Companion.KEY_TOKEN
import com.example.moodlegovapp.domain.repositoryinterface.AuthRepositoryProtocol
import com.example.moodlegovapp.domain.models.AuthToken

/**
 * Auth Repository that coordinates remote and local data sources.
 * Implements retry logic and fallback mechanisms for login operations.
 */
class AuthRepository(
    private val remoteDataSource: AuthDataSource,
    private val dataStoreManager: DataStoreManager,
    private val localMock: MockApiService? = null  // fallback if remote mock fails
) : AuthRepositoryProtocol {

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        val result = remoteDataSource.login(username, password)

        return when (result) {
            is AppResult.Success -> {
                // Cache the token securely
                dataStoreManager.save(KEY_TOKEN, result.data.token)
                Log.d("AuthRepository", "Login successful, token cached")
                result
            }
            is AppResult.Failure -> {
                // Implement intelligent fallback strategy
                val shouldFallback = when (result.error) {
                    is AppError.NotFound              -> true
                    is AppError.ServiceUnavailable    -> true
                    is AppError.Timeout               -> true
                    is AppError.NetworkError          -> true
                    is AppError.ServerError           -> result.error.code == 503
                    else                              -> false
                }

                if (shouldFallback && NetworkConfig.USE_MOCK && localMock != null) {
                    Log.d("AuthRepository", "Remote failed (${result.error.errorDescription}) — falling back to local mock")
                    val fallback = localMock.login(username, password)
                    if (fallback is AppResult.Success) {
                        dataStoreManager.save(KEY_TOKEN, fallback.data.token)
                        Log.d("AuthRepository", "Fallback login succeeded")
                    }
                    fallback
                } else {
                    Log.e("AuthRepository", "Login failed: ${result.error.errorDescription}")
                    result
                }
            }
            is AppResult.Loading -> result
        }
    }

    override suspend fun logout() {
        dataStoreManager.clearAll()
        Log.d("AuthRepository", "User logged out, cache cleared")
    }

    suspend fun checkUserStatus(): Boolean {
        val token = dataStoreManager.get<String>(KEY_TOKEN)
        return token != null
    }
}