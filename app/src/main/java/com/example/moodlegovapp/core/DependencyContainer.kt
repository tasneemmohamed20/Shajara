package com.example.moodlegovapp.core

import android.content.Context
import com.example.moodlegovapp.data.repository.AppDependencies
import com.example.moodlegovapp.data.session.AppSession
import com.example.moodlegovapp.presentation.viewmodels.AssignmentsViewModel
import com.example.moodlegovapp.presentation.viewmodels.CourseDetailViewModel
import com.example.moodlegovapp.presentation.viewmodels.CoursesViewModel
import com.example.moodlegovapp.presentation.viewmodels.DashboardViewModel
import com.example.moodlegovapp.presentation.viewmodels.LoginViewModel
import com.example.moodlegovapp.presentation.viewmodels.NotificationsViewModel
import com.example.moodlegovapp.presentation.viewmodels.ProfileViewModel
import com.example.moodlegovapp.presentation.viewmodels.ForgotPasswordViewModel
import com.example.moodlegovapp.presentation.viewmodels.LessonVideoViewModel
import com.example.moodlegovapp.presentation.viewmodels.CourseResourcesViewModel
import com.example.moodlegovapp.presentation.viewmodels.GradesViewModel
import com.example.moodlegovapp.presentation.viewmodels.TasksViewModel
import com.example.moodlegovapp.presentation.viewmodels.QuizAttemptViewModel
import com.example.moodlegovapp.presentation.viewmodels.FeedbackSurveyViewModel

class DependencyContainer private constructor(private val deps: AppDependencies) {

    companion object {
        @Volatile private var instance: DependencyContainer? = null

        fun getInstance(context: Context): DependencyContainer =
            instance ?: synchronized(this) {
                instance ?: DependencyContainer(AppDependencies.getInstance(context)).also { instance = it }
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

    // ── Offline support accessors ───────────────────────────────────────────
    val connectivityObserver get() = deps.connectivityObserver
    val fileDownloadManager get() = deps.fileDownloadManager
    val pendingActionQueue get() = deps.pendingActionQueue
    val dataStoreManager get() = deps.dataStoreManager
    fun syncEngine(context: Context) = com.example.moodlegovapp.data.offline.sync.SyncEngine.getInstance(context)

    // mirrors iOS: public func makeLoginViewModel() -> LoginViewModelDI
    fun makeLoginViewModel(): LoginViewModel =
        LoginViewModel(
            session = sharedSession,
            dataStoreManager = deps.dataStoreManager
        )

    // mirrors iOS: public func makeDashboardViewModel() -> DashboardViewModelDI
    fun makeDashboardViewModel(): DashboardViewModel =
        DashboardViewModel(
            userRepository = deps.userRepository,
            coursesRepository = deps.coursesRepository,
            notificationsRepository = deps.notificationsRepository,
            certificatesRepository = deps.certificatesRepository
        )

    // mirrors iOS: public func makeCoursesViewModel() -> CoursesViewModelDI
    fun makeCoursesViewModel(): CoursesViewModel =
        CoursesViewModel(coursesRepository = deps.coursesRepository)

    fun makeCourseDetailViewModel(courseId: Int, courseName: String, progress: Int): CourseDetailViewModel =
        CourseDetailViewModel(
            courseId = courseId,
            courseName = courseName,
            progress = progress,
            coursesRepository = deps.coursesRepository
        )

    fun makeNotificationsViewModel(): NotificationsViewModel =
        NotificationsViewModel(notificationsRepository = deps.notificationsRepository)

    fun makeProfileViewModel(): ProfileViewModel =
        ProfileViewModel(
            userRepository         = deps.userRepository,
            session                = sharedSession
        )
    fun makeAssignmentsViewModel(): AssignmentsViewModel =
        AssignmentsViewModel(
            // Make sure your AppDependencies class exposes assignmentsRepository
            // the same way it exposes coursesRepository and userRepository!
            repository = deps.assignmentsRepository
        )
    fun makeTasksViewModel(): TasksViewModel = TasksViewModel(deps.tasksRepository)

    fun makeGradesViewModel(): GradesViewModel = GradesViewModel(deps.gradesRepository)

    fun makeCourseResourcesViewModel(): CourseResourcesViewModel = CourseResourcesViewModel(deps.courseResourcesRepository)

    fun makeLessonVideoViewModel(): LessonVideoViewModel = LessonVideoViewModel(deps.lessonRepository)

    fun makeForgotPasswordViewModel(): ForgotPasswordViewModel = ForgotPasswordViewModel(deps.authRepository)

    fun makeQuizAttemptViewModel(): QuizAttemptViewModel = QuizAttemptViewModel(deps.quizFeedbackRepository)

    fun makeFeedbackSurveyViewModel(): FeedbackSurveyViewModel = FeedbackSurveyViewModel(deps.quizFeedbackRepository)

}