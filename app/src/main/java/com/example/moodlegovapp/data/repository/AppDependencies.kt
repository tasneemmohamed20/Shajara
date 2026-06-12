package com.example.moodlegovapp.data.repository

import android.content.Context
import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.FallbackApiService
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

    val dataStoreManager: DataStoreManager = DataStoreManager.Companion.getInstance(context)

    private val localMock: MockApiService by lazy {
        MockApiService(context, dataStoreManager)
    }

    private val networkApi: RealApiService by lazy {
        val retrofit = RetrofitClient.create(
            dataStoreManager,
            isDebug = NetworkConfig.USE_REMOTE_MOCK
        )
        RealApiService(retrofit, dataStoreManager)
    }

    val apiService: ApiServiceProtocol by lazy {
        if (NetworkConfig.ENABLE_LOCAL_FALLBACK) {
            FallbackApiService(networkApi, localMock)
        } else {
            networkApi
        }
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            api = apiService,
            dataStoreManager = dataStoreManager
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository(api = apiService)
    }

    val coursesRepository: CoursesRepository by lazy {
        CoursesRepository(api = apiService)
    }

    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(notificationsDataSource = apiService)
    }

    val certificatesRepository: CertificatesRepository by lazy {
        CertificatesRepository(certificatesDataSource = apiService)
    }
}
