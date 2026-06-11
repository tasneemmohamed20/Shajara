package com.example.moodlegovapp.core

import android.content.Context
import com.example.moodlegovapp.data.repository.AppDependencies
import com.example.moodlegovapp.data.session.AppSession
import com.example.moodlegovapp.presentation.viewmodels.CourseDetailViewModel
import com.example.moodlegovapp.presentation.viewmodels.CoursesViewModel
import com.example.moodlegovapp.presentation.viewmodels.DashboardViewModel
import com.example.moodlegovapp.presentation.viewmodels.LoginViewModel
import com.example.moodlegovapp.presentation.viewmodels.NotificationsViewModel
import com.example.moodlegovapp.presentation.viewmodels.ProfileViewModel

class DependencyContainer private constructor(private val deps: AppDependencies) {

    companion object {
        @Volatile private var instance: DependencyContainer? = null

        fun getInstance(context: Context): DependencyContainer =
            instance ?: synchronized(this) {
                instance ?: DependencyContainer(AppDependencies.Companion.getInstance(context)).also { instance = it }
            }
    }

    // mirrors iOS: public lazy var sharedSession: AppSession
    val sharedSession: AppSession by lazy {
        AppSession(
            authRepository = deps.authRepository,
            userRepository = deps.userRepository,
            dataStoreManager = deps.dataStoreManager
        )
    }

    // mirrors iOS: public func makeLoginViewModel() -> LoginViewModelDI
    fun makeLoginViewModel(): LoginViewModel =
        LoginViewModel(session = sharedSession)

    // mirrors iOS: public func makeDashboardViewModel() -> DashboardViewModelDI
    fun makeDashboardViewModel(): DashboardViewModel =
        DashboardViewModel(
            userRepository = deps.userRepository,
            coursesRepository = deps.coursesRepository,
            notificationsRepository = deps.notificationsRepository
        )

    // mirrors iOS: public func makeCoursesViewModel() -> CoursesViewModelDI
    fun makeCoursesViewModel(): CoursesViewModel =
        CoursesViewModel(coursesRepository = deps.coursesRepository)

    // mirrors iOS: public func makeCourseDetailViewModel(courseId:) -> CourseDetailViewModelDI
    fun makeCourseDetailViewModel(courseId: Int): CourseDetailViewModel =
        CourseDetailViewModel(
            courseId = courseId,
            coursesRepository = deps.coursesRepository
        )

    fun makeNotificationsViewModel(): NotificationsViewModel =
        NotificationsViewModel(notificationsRepository = deps.notificationsRepository)

    fun makeProfileViewModel(): ProfileViewModel =
        ProfileViewModel(
            userRepository         = deps.userRepository,
            certificatesRepository = deps.certificatesRepository
        )
}