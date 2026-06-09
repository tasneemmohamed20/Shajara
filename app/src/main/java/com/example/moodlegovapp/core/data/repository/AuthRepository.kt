package com.example.moodlegovapp.core.data.repository

import android.util.Log
import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppError
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.data.network.MockApiService
import com.example.moodlegovapp.core.data.network.NetworkConfig
import com.example.moodlegovapp.core.data.service.SecureStorage
import com.example.moodlegovapp.core.domain.repositoryinterface.AuthRepositoryProtocol
import com.example.moodlegovapp.core.domain.models.AuthToken

class AuthRepository(
    private val api: ApiServiceProtocol,
    private val keychain: SecureStorage,
    private val localMock: MockApiService? = null  // fallback if remote mock fails
) : AuthRepositoryProtocol {

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        val result = api.login(username, password)

        return when (result) {
            is AppResult.Success -> {
                keychain.saveToken(result.data.token)
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
                        keychain.saveToken(fallback.data.token)
                    }
                    fallback
                } else {
                    result
                }
            }
            is AppResult.Loading -> result
        }
    }

    override fun logout() = keychain.clearAll()

    override val isLoggedIn: Boolean get() = keychain.isLoggedIn
}
