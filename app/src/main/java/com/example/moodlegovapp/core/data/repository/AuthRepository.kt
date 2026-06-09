package com.example.moodlegovapp.core.data.repository

import android.util.Log
import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppError
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.data.network.MockApiService
import com.example.moodlegovapp.core.data.network.NetworkConfig
import com.example.moodlegovapp.core.data.service.DataStoreManager
import com.example.moodlegovapp.core.data.service.DataStoreManager.Companion.KEY_TOKEN
import com.example.moodlegovapp.core.domain.repositoryinterface.AuthRepositoryProtocol
import com.example.moodlegovapp.core.domain.models.AuthToken

class AuthRepository(
    private val api: ApiServiceProtocol,
    private val dataStoreManager: DataStoreManager,
    private val localMock: MockApiService? = null  // fallback if remote mock fails
) : AuthRepositoryProtocol {

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        val result = api.login(username, password)

        return when (result) {
            is AppResult.Success -> {
//                dataStoreManager.saveToken(result.data.token)
                dataStoreManager.save(KEY_TOKEN, result.data.token)
                result
            }
            is AppResult.Failure -> {
                // mirrors iOS AuthRepository fallback:
                // if remote mock returns 404 or 503 → fall back to local MockApiService
                val shouldFallback = when (result.error) {
                    is AppError.NotFound              -> true
                    is AppError.ServerError           -> result.error.code == 503
                    is AppError.NetworkError          -> true
                    else                              -> false
                }

                if (shouldFallback && NetworkConfig.USE_MOCK && localMock != null) {
                    Log.d("AuthRepository", "Remote mock failed — falling back to local MockApiService")
                    val fallback = localMock.login(username, password)
                    if (fallback is AppResult.Success) {
//                        dataStoreManager.saveToken(fallback.data.token)
                        dataStoreManager.save(KEY_TOKEN, fallback.data.token)
                    }
                    fallback
                } else {
                    result
                }
            }
            is AppResult.Loading -> result
        }
    }

    override suspend fun logout() = dataStoreManager.clearAll()

    suspend fun checkUserStatus(): Boolean {
        val token = dataStoreManager.get<String>(DataStoreManager.KEY_TOKEN)
        return token != null
    }
}