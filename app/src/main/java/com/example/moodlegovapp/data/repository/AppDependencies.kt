package com.example.moodlegovapp.data.repository

import android.content.Context
import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.network.RealApiService
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetrofitClient
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.db.OfflineDatabase
import com.example.moodlegovapp.data.offline.download.FileDownloadManager
import com.example.moodlegovapp.data.offline.sync.PendingActionQueue
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.repositoryinterface.AssignmentsRepositoryProtocol


class AppDependencies private constructor(context: Context) {

    companion object {
        @Volatile private var instance: AppDependencies? = null

        fun getInstance(context: Context): AppDependencies =
            instance ?: synchronized(this) {
                instance ?: AppDependencies(context.applicationContext).also { instance = it }
            }
    }

    val dataStoreManager: DataStoreManager = DataStoreManager.getInstance(context)

    // ── Offline support ────────────────────────────────────────────────────
    private val offlineDatabase: OfflineDatabase by lazy { OfflineDatabase.getInstance(context) }
    val connectivityObserver: ConnectivityObserver by lazy { ConnectivityObserver.getInstance(context) }
    val fileDownloadManager: FileDownloadManager by lazy { FileDownloadManager.getInstance(context) }
    val pendingActionQueue: PendingActionQueue by lazy { PendingActionQueue(offlineDatabase.pendingActionDao()) }
    private val offlineCache: OfflineCache by lazy { OfflineCache(offlineDatabase.cachedResponseDao(), connectivityObserver) }
    private val retrofitService: RetrofitApiService by lazy {
        RetrofitClient.create(
            dataStoreManager,
            isDebug = NetworkConfig.USE_REMOTE_MOCK
        )
    }
    private val networkApi: RealApiService by lazy {
        val retrofit = RetrofitClient.create(
            dataStoreManager,
            isDebug = NetworkConfig.USE_REMOTE_MOCK
        )
        RealApiService(retrofit, dataStoreManager)
    }
    val apiService: ApiServiceProtocol by lazy { networkApi }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            api = apiService,
            dataStoreManager = dataStoreManager
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepository(api = apiService, offlineCache = offlineCache, dataStoreManager = dataStoreManager)
    }

    val coursesRepository: CoursesRepository by lazy {
        CoursesRepository(
            api = apiService,
            offlineCache = offlineCache,
            connectivity = connectivityObserver,
            pendingActions = pendingActionQueue,
            dataStoreManager = dataStoreManager
        )
    }

    val notificationsRepository: NotificationsRepository by lazy {
        NotificationsRepository(
            notificationsDataSource = apiService,
            offlineCache = offlineCache,
            connectivity = connectivityObserver,
            pendingActions = pendingActionQueue,
            dataStoreManager = dataStoreManager
        )
    }

    val certificatesRepository: CertificatesRepository by lazy {
        CertificatesRepository(
            certificatesDataSource = apiService,
            offlineCache = offlineCache,
            dataStoreManager = dataStoreManager
        )
    }
    val assignmentsRepository: AssignmentsRepositoryProtocol by lazy {
        AssignmentsRepository(
            retrofit = retrofitService,
            dataStoreManager = dataStoreManager,
            offlineCache = offlineCache,
            connectivity = connectivityObserver,
            pendingActions = pendingActionQueue
        )
    }
    val tasksRepository: TasksRepository by lazy { TasksRepository(retrofitService) }
    val gradesRepository: GradesRepository by lazy { GradesRepository(retrofitService) }
    val courseResourcesRepository: CourseResourcesRepository by lazy { CourseResourcesRepository(retrofitService) }
    val lessonRepository: LessonRepository by lazy { LessonRepository(retrofitService) }
    val quizFeedbackRepository: QuizFeedbackRepository by lazy { QuizFeedbackRepository(retrofitService) }

}
