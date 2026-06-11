package com.example.moodlegovapp.data.repository

import android.content.Context
import com.example.moodlegovapp.data.network.MockApiService
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.network.RetrofitClient
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.data.network.datasource.RemoteDataSource
import com.example.moodlegovapp.data.service.DataStoreManager

/**
 * Dependency injection container using Service Locator pattern.
 * Manages creation and caching of all data sources, repositories, and services.
 *
 * Future: Migrate to Hilt for compile-time dependency injection.
 */
class AppDependencies private constructor(context: Context) {

    companion object {
        @Volatile private var instance: AppDependencies? = null

        fun getInstance(context: Context): AppDependencies =
            instance ?: synchronized(this) {
                instance ?: AppDependencies(context.applicationContext).also { instance = it }
            }
    }

    // ── Secure Storage ────────────────────────────────────────────
    val dataStoreManager: DataStoreManager = DataStoreManager.Companion.getInstance(context)

    // ── Retry Policies ────────────────────────────────────────────
    private val defaultRetryPolicy: RetryPolicy = RetryPolicy.DEFAULT
    private val aggressiveRetryPolicy: RetryPolicy = RetryPolicy.AGGRESSIVE

    // ── Local Mock (for fallback) ─────────────────────────────────
    private val localMock: MockApiService by lazy {
        MockApiService(context, dataStoreManager)
    }

    // ── Retrofit Service ──────────────────────────────────────────
    private val retrofitService by lazy {
        when {
            NetworkConfig.USE_MOCK && NetworkConfig.USE_REMOTE_MOCK -> {
                RetrofitClient.create(dataStoreManager, isDebug = true)
            }
            NetworkConfig.USE_MOCK -> {
                // Local mock doesn't need Retrofit
                RetrofitClient.create(dataStoreManager, isDebug = false)
            }
            else -> {
                // Real Moodle API
                RetrofitClient.create(dataStoreManager, isDebug = false)
            }
        }
    }

    // ── Remote Data Source ────────────────────────────────────────
    private val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource(
            retrofit = retrofitService,
            dataStoreManager = dataStoreManager,
            retryPolicy = if (NetworkConfig.USE_MOCK) aggressiveRetryPolicy else defaultRetryPolicy
        )
    }

    // ── Repositories ──────────────────────────────────────────────

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            remoteDataSource = remoteDataSource,
            dataStoreManager = dataStoreManager,
            localMock = if (NetworkConfig.USE_REMOTE_MOCK) localMock else null
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository(
            userDataSource = remoteDataSource,
            badgesDataSource = remoteDataSource,
            leaderboardDataSource = remoteDataSource,
            eventsDataSource = remoteDataSource,
            statsDataSource = remoteDataSource
        )
    }

    val coursesRepository: CoursesRepository by lazy {
        CoursesRepository(
            coursesDataSource = remoteDataSource,
            assignmentsDataSource = remoteDataSource,
            searchDataSource = remoteDataSource,
            activityDataSource = remoteDataSource
        )
    }

    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(
            notificationsDataSource = remoteDataSource
        )
    }

    val certificatesRepository: CertificatesRepository by lazy {
        CertificatesRepository(
            certificatesDataSource = remoteDataSource
        )
    }
}