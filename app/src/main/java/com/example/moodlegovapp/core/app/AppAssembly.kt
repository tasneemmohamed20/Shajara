package com.example.moodlegovapp.core.app

import com.example.moodlegovapp.core.presentation.viewmodels.CourseDetailViewModel
import com.example.moodlegovapp.core.presentation.viewmodels.CoursesViewModel
import com.example.moodlegovapp.core.presentation.viewmodels.DashboardViewModel
import com.example.moodlegovapp.core.presentation.viewmodels.LoginViewModel
import android.content.Context
import com.example.moodlegovapp.core.data.repository.AppDependencies
import com.example.moodlegovapp.core.data.session.AppSession

class AppAssembly private constructor(private val deps: AppDependencies) {

    companion object {
        @Volatile private var instance: AppAssembly? = null

        fun getInstance(context: Context): AppAssembly =
            instance ?: synchronized(this) {
                instance ?: AppAssembly(AppDependencies.getInstance(context)).also { instance = it }
            }
    }

    // mirrors iOS: public lazy var sharedSession: AppSession
    val sharedSession: AppSession by lazy {
        AppSession(
            authRepository = deps.authRepository,
            userRepository = deps.userRepository,
            dataStoreManager       = deps.dataStoreManager
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

//    fun makeNotificationsViewModel(): NotificationsViewModel =
//        NotificationsViewModel(notificationsRepository = deps.notificationsRepository)
//
//    fun makeProfileViewModel(): ProfileViewModel =
//        ProfileViewModel(
//            userRepository        = deps.userRepository,
//            certificatesRepository = deps.certificatesRepository,
//            session               = sharedSession
//        )
}