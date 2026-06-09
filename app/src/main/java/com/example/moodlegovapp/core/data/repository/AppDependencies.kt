package com.example.moodlegovapp.core.data.repository

import android.content.Context
import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.MockApiService
import com.example.moodlegovapp.core.data.network.NetworkConfig
import com.example.moodlegovapp.core.data.network.RealApiService
import com.example.moodlegovapp.core.data.network.RetrofitClient
import com.example.moodlegovapp.core.data.service.SecureStorage


class AppDependencies private constructor(context: Context) {

    companion object {
        @Volatile private var instance: AppDependencies? = null

        fun getInstance(context: Context): AppDependencies =
            instance ?: synchronized(this) {
                instance ?: AppDependencies(context.applicationContext).also { instance = it }
            }
    }

    // ── Secure Storage ────────────────────────
    // mirrors iOS: let keychainManager: KeychainManager = .shared
    val secureStorage: SecureStorage = SecureStorage.getInstance(context)

    // ── Local Mock (for fallback) ─────────────
    private val localMock: MockApiService by lazy {
        MockApiService(context, secureStorage)
    }


    val apiService: ApiServiceProtocol by lazy {
        when {
            NetworkConfig.USE_MOCK && NetworkConfig.USE_REMOTE_MOCK -> {
                // Call remote Postman mock via RealApiService
                val retrofit = RetrofitClient.create(secureStorage, isDebug = true)
                RealApiService(retrofit, secureStorage)
            }
            NetworkConfig.USE_MOCK -> {
                // Read local JSON files
                localMock
            }
            else -> {
                // Real Moodle API
                val retrofit = RetrofitClient.create(secureStorage, isDebug = false)
                RealApiService(retrofit, secureStorage)
            }
        }
    }

    // ── Repositories ──────────────────────────
    // mirrors iOS: lazy var authRepository, coursesRepository...

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            api       = apiService,
            keychain  = secureStorage,
            localMock = if (NetworkConfig.USE_REMOTE_MOCK) localMock else null
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository(api = apiService)
    }

    val coursesRepository: CoursesRepository by lazy {
        CoursesRepository(api = apiService)
    }

    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(api = apiService)
    }

    val certificatesRepository: CertificatesRepository by lazy {
        CertificatesRepository(api = apiService)
    }
}