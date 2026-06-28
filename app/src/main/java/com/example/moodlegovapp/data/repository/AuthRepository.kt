package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.data.service.DataStoreManager.Companion.KEY_TOKEN
import com.example.moodlegovapp.domain.repositoryinterface.AuthRepositoryProtocol
import com.example.moodlegovapp.domain.models.AuthToken

class AuthRepository(
    private val api: ApiServiceProtocol,
    private val dataStoreManager: DataStoreManager
) : AuthRepositoryProtocol {

    override suspend fun login(username: String, password: String): AppResult<AuthToken> {
        return when (val result = api.login(username, password)) {
            is AppResult.Success -> {
                dataStoreManager.save(DataStoreManager.Companion.KEY_TOKEN, result.data.token)
                dataStoreManager.save(DataStoreManager.Companion.KEY_USERNAME, username)
                result
            }
            else -> result
        }
    }

    override suspend fun requestPasswordReset(email: String) = api.requestPasswordReset(email)

    override suspend fun getSignupSettings() = api.getSignupSettings()

    override suspend fun getSiteInfo() = api.getSiteInfo()

    override suspend fun logout() = dataStoreManager.clearAll()

    suspend fun checkUserStatus(): Boolean {
        val token = dataStoreManager.get<String>(KEY_TOKEN)
        return token != null
    }
}
