package com.example.moodlegovapp.data.repository

import android.content.Context
import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.MockApiService
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.network.RealApiService
import com.example.moodlegovapp.data.network.RetrofitClient
import com.example.moodlegovapp.data.service.DataStoreManager


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
    val dataStoreManager: DataStoreManager = DataStoreManager.Companion.getInstance(context)

    // ── Local Mock (for fallback) ─────────────
    private val localMock: MockApiService by lazy {
        MockApiService(context, dataStoreManager)
    }


    val apiService: ApiServiceProtocol by lazy {
        when {
            NetworkConfig.USE_MOCK && NetworkConfig.USE_REMOTE_MOCK -> {
                // Call remote Postman mock via RealApiService
                val retrofit = RetrofitClient.create(dataStoreManager, isDebug = true)
                RealApiService(retrofit, dataStoreManager)
            }
            NetworkConfig.USE_MOCK -> {
                // Read local JSON files
                localMock
            }
            else -> {
                // Real Moodle API
                val retrofit = RetrofitClient.create(dataStoreManager, isDebug = false)
                RealApiService(retrofit, dataStoreManager)
            }
        }
    }

    // ── Repositories ──────────────────────────
    // mirrors iOS: lazy var authRepository, coursesRepository...

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            api       = apiService,
            dataStoreManager  = dataStoreManager,
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