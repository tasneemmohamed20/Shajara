package com.example.moodlegovapp.data.repository

import android.content.Context
import com.example.moodlegovapp.data.network.MockApiService
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.network.RetrofitClient
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.data.network.datasource.ActivityDataSource
import com.example.moodlegovapp.data.network.datasource.AssignmentsDataSource
import com.example.moodlegovapp.data.network.datasource.AuthDataSource
import com.example.moodlegovapp.data.network.datasource.BadgesDataSource
import com.example.moodlegovapp.data.network.datasource.CertificatesDataSource
import com.example.moodlegovapp.data.network.datasource.CoursesDataSource
import com.example.moodlegovapp.data.network.datasource.EventsDataSource
import com.example.moodlegovapp.data.network.datasource.LeaderboardDataSource
import com.example.moodlegovapp.data.network.datasource.NotificationsDataSource
import com.example.moodlegovapp.data.network.datasource.RemoteDataSource
import com.example.moodlegovapp.data.network.datasource.SearchDataSource
import com.example.moodlegovapp.data.network.datasource.StatsDataSource
import com.example.moodlegovapp.data.network.datasource.UserDataSource
import com.example.moodlegovapp.data.service.DataStoreManager

/**
 * Dependency injection container using Service Locator pattern.
 * Manages creation and caching of all data sources, repositories, and services.
 *
 * Data source routing:
 *  - USE_MOCK=false            → RemoteDataSource  (real Moodle API)
 *  - USE_MOCK=true, USE_REMOTE_MOCK=true  → RemoteDataSource  (Postman mock server)
 *  - USE_MOCK=true, USE_REMOTE_MOCK=false → MockApiService    (local JSON assets)
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

    // ── Local Mock (local JSON files — no network) ────────────────
    private val localMock: MockApiService by lazy {
        MockApiService(context, dataStoreManager)
    }

    // ── Remote Data Source (Retrofit — network required) ──────────
    private val remoteDataSource: RemoteDataSource by lazy {
        val isDebug = NetworkConfig.USE_MOCK && NetworkConfig.USE_REMOTE_MOCK
        RemoteDataSource(
            retrofit = RetrofitClient.create(dataStoreManager, isDebug = isDebug),
            dataStoreManager = dataStoreManager,
            retryPolicy = defaultRetryPolicy
        )
    }

    // ── Active Data Source: local mock OR remote ──────────────────
    // When USE_MOCK=true and USE_REMOTE_MOCK=false, all repos use local JSON.
    private val useLocalMock: Boolean
        get() = NetworkConfig.USE_MOCK && !NetworkConfig.USE_REMOTE_MOCK

    private val activeAuthSource: AuthDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeUserSource: UserDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeCoursesSource: CoursesDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeAssignmentsSource: AssignmentsDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeNotificationsSource: NotificationsDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeCertificatesSource: CertificatesDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeLeaderboardSource: LeaderboardDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeBadgesSource: BadgesDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeEventsSource: EventsDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeStatsSource: StatsDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeSearchSource: SearchDataSource get() = if (useLocalMock) localMock else remoteDataSource
    private val activeActivitySource: ActivityDataSource get() = if (useLocalMock) localMock else remoteDataSource

    // ── Repositories ──────────────────────────────────────────────

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            remoteDataSource = activeAuthSource,
            dataStoreManager = dataStoreManager
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository(
            userDataSource = activeUserSource,
            badgesDataSource = activeBadgesSource,
            leaderboardDataSource = activeLeaderboardSource,
            eventsDataSource = activeEventsSource,
            statsDataSource = activeStatsSource
        )
    }

    val coursesRepository: CoursesRepository by lazy {
        CoursesRepository(
            coursesDataSource = activeCoursesSource,
            assignmentsDataSource = activeAssignmentsSource,
            searchDataSource = activeSearchSource,
            activityDataSource = activeActivitySource
        )
    }

    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(
            notificationsDataSource = activeNotificationsSource
        )
    }

    val certificatesRepository: CertificatesRepository by lazy {
        CertificatesRepository(
            certificatesDataSource = activeCertificatesSource
        )
    }
}