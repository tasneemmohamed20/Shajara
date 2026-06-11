package com.example.moodlegovapp.data.repository

import android.util.Log
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.AuthDataSource
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.data.service.DataStoreManager.Companion.KEY_TOKEN
import com.example.moodlegovapp.domain.repositoryinterface.AuthRepositoryProtocol
import com.example.moodlegovapp.domain.models.AuthToken

/**
 * Auth Repository that coordinates the active auth data source (local mock or remote).
 * Data source selection is handled upstream in AppDependencies.
 */
class AuthRepository(
    private val remoteDataSource: AuthDataSource,
    private val dataStoreManager: DataStoreManager
) : AuthRepositoryProtocol {

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        val result = remoteDataSource.login(username, password)

        return when (result) {
            is AppResult.Success -> {
                dataStoreManager.save(KEY_TOKEN, result.data.token)
                Log.d("AuthRepository", "Login successful, token cached")
                result
            }
            is AppResult.Failure -> {
                Log.e("AuthRepository", "Login failed: ${result.error.errorDescription}")
                result
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