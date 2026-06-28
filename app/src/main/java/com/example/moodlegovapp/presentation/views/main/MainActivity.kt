package com.example.moodlegovapp.presentation.views.main

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.presentation.utils.ScreensRoute
import com.example.moodlegovapp.presentation.views.Profile.ProfileScreen
import com.example.moodlegovapp.presentation.views.assigments.AssignmentDetailsScreen
import com.example.moodlegovapp.presentation.views.assigments.AssignmentSubmissionScreen
import com.example.moodlegovapp.presentation.views.auth.LoginStepOneView
import com.example.moodlegovapp.presentation.views.auth.ForgotPasswordScreen
import com.example.moodlegovapp.presentation.views.auth.CheckEmailScreen
import com.example.moodlegovapp.presentation.views.tasks.TasksScreen
import com.example.moodlegovapp.presentation.views.grades.GradesScreen
import com.example.moodlegovapp.presentation.views.notifications.NotificationScreen
import com.example.moodlegovapp.presentation.views.resources.CourseResourcesScreen
import com.example.moodlegovapp.presentation.views.lesson.LessonVideoScreen
import com.example.moodlegovapp.presentation.views.quiz.QuizAttemptScreen
import com.example.moodlegovapp.presentation.views.feedback.FeedbackSurveyScreen
import com.example.moodlegovapp.presentation.views.coursedetails.CourseOverviewScreen
import com.example.moodlegovapp.presentation.views.dashboard.DashboardScreen
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography
import com.gov.moodleapp.presentation.auth.LoginStepTwoView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var assembly: DependencyContainer
    private var isKeepShowing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isKeepShowing }

        // Read persisted language and apply it before super.onCreate() to avoid layout flashing
        val dsm = DataStoreManager.getInstance(applicationContext)
        val savedLang = runBlocking {
            dsm.get<String>(DataStoreManager.KEY_LANGUAGE)
        }
        if (savedLang != null) {
            val locales = AppCompatDelegate.getApplicationLocales()
            val currentLang = if (locales.isEmpty) "en" else locales[0]?.language ?: "en"
            if (savedLang != currentLang) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(savedLang)
                )
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AppColors.Navy.toArgb(), AppColors.Navy.toArgb()
            )
        )
        assembly = DependencyContainer.getInstance(this)

        // ── Offline sync bootstrap ──────────────────────────────────────────
        // Schedules the WorkManager-driven background sync (approximates the
        // Moodle doc's "automatic, every 10 minutes" behaviour) and also syncs
        // immediately whenever connectivity is regained while the app is open,
        // which is the tighter, in-app equivalent of that same behaviour.
        com.example.moodlegovapp.data.offline.sync.PeriodicSyncWorker.schedule(applicationContext)
        val connectivityObserver = com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver.getInstance(applicationContext)
        lifecycleScope.launch {
            var wasOnline = connectivityObserver.isOnlineNow()
            connectivityObserver.isOnline.collect { online ->
                if (online && !wasOnline) {
                    com.example.moodlegovapp.data.offline.sync.SyncEngine.getInstance(applicationContext).syncNow()
                }
                wasOnline = online
            }
        }

        setContent {
            MoodleGovAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
                    val session = remember { assembly.sharedSession }

                    val isInitialized by session.isInitialized.observeAsState(initial = false)

                    if (isInitialized) {
                        val rootNavController = rememberNavController()
                        val startRoute =
                            if (session.isAuthenticated) "main_app_root" else "auth_root"

                        LaunchedEffect(Unit) {
                            isKeepShowing = false
                        }

                        NavHost(
                            navController = rootNavController,
                            startDestination = startRoute,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Unauthenticated Context Wrapper
                            composable("auth_root") {
                                val loginNavController = rememberNavController()
                                NavHost(
                                    navController = loginNavController,
                                    startDestination = ScreensRoute.LoginStepOne.route
                                ) {
                                    authGraph(
                                        navController = loginNavController,
                                        showBackButton = false,
                                        onLoginSuccess = {
                                            rootNavController.navigate("main_app_root") {
                                                popUpTo("auth_root") { inclusive = true }
                                            }
                                        },
                                        assembly = assembly
                                    )
                                }
                            }

                            composable("main_app_root") {
                                val mainNavController = rememberNavController()
                                MainScreen(navController = mainNavController) {
                                    NavHost(
                                        navController = mainNavController,
                                        startDestination = ScreensRoute.Home.route
                                    ) {
                                        mainAppGraph(
                                            navController = mainNavController,
                                            rootNavController = rootNavController,
                                            assembly = assembly
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // While session is restoring, show a blank background matching the splash screen
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}


fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    showBackButton: Boolean,
    onLoginSuccess: () -> Unit,
    assembly: DependencyContainer
) {
    composable(ScreensRoute.LoginStepOne.route) {
        val vm = remember { assembly.makeLoginViewModel() }
        LoginStepOneView(
            showBackButton = showBackButton,
            onBackClick = { navController.popBackStack() },
            onContinueClicked = { navController.navigate(ScreensRoute.LoginStepTwo.route) },
            vm = vm
        )
    }

    composable(ScreensRoute.LoginStepTwo.route) {
        LoginStepTwoView(
            onLoginSuccess = onLoginSuccess,
            assembly = DependencyContainer.getInstance(LocalContext.current),
            onBackClicked = { navController.popBackStack() },
            onForgotPassword = { navController.navigate(ScreensRoute.ForgotPassword.route) })
    }

    composable(ScreensRoute.ForgotPassword.route) {
        val vm = remember { assembly.makeForgotPasswordViewModel() }
        ForgotPasswordScreen(
            vm = vm,
            onBackClick = { navController.popBackStack() },
            onCheckEmail = { navController.navigate(ScreensRoute.CheckEmail.createRoute(vm.email.value)) }
        )
    }

    composable(
        route = ScreensRoute.CheckEmail.route,
        arguments = listOf(navArgument("email") { type = NavType.StringType })
    ) { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email") ?: ""
        CheckEmailScreen(
            email = email,
            onBackToLogin = { navController.popBackStack(ScreensRoute.LoginStepTwo.route, inclusive = false) },
            onBackClick = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.mainAppGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    assembly: DependencyContainer
) {
    composable(ScreensRoute.Home.route) {
        DashboardScreen(assembly = assembly, onCourseClick = { courseId, courseName, progress ->
            navController.navigate(ScreensRoute.CourseDetail.createRoute(courseId, courseName, progress))
        }, onLeaderboardClick = {
        }, onNotificationClick = {
            navController.navigate(ScreensRoute.Notifications.route)
        })
    }

    composable(ScreensRoute.Tasks.route) {
        val vm = remember { assembly.makeTasksViewModel() }
        TasksScreen(vm = vm, onTaskClick = { assignmentId ->
            navController.navigate(ScreensRoute.AssignmentDetails.createRoute(0, assignmentId))
        })
    }

    composable(ScreensRoute.Grades.route) {
        val vm = remember { assembly.makeGradesViewModel() }
        GradesScreen(vm = vm)
    }

    composable(ScreensRoute.Notifications.route) {
        val vm = remember { assembly.makeNotificationsViewModel() }
        NotificationScreen(vm = vm, onBackClick = { navController.popBackStack() })
    }

    composable(ScreensRoute.Profile.route) {
        val vm = remember { assembly.makeProfileViewModel() }
        LaunchedEffect(Unit) {
            vm.loadAll()
        }
        val scope = rememberCoroutineScope()

        ProfileScreen(viewModel = vm, onLogOutClick = {
            scope.launch {
                assembly.sharedSession.logout()
                rootNavController.navigate("auth_root") {
                    popUpTo("main_app_root") { inclusive = true }
                }
            }
        }, onBackClick = {
            navController.popBackStack()
        })
    }

    composable(
        route = ScreensRoute.CourseDetail.route,
        arguments = listOf(
            navArgument("courseId") { type = NavType.IntType },
            navArgument("courseName") { type = NavType.StringType; defaultValue = "" },
            navArgument("progress") { type = NavType.IntType; defaultValue = 0 }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
        val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
        val progress = backStackEntry.arguments?.getInt("progress") ?: 0
        val vm = remember(courseId) { assembly.makeCourseDetailViewModel(courseId, courseName, progress) }

        val context = androidx.compose.ui.platform.LocalContext.current
        CourseOverviewScreen(
            vm = vm,
            onBackClick = { navController.popBackStack() },
            onReviewAssignmentClick = { assignmentId ->
                navController.navigate(ScreensRoute.AssignmentDetails.createRoute(courseId, assignmentId))
            },
            onActivityClick = { activity ->
                val kind = activity.modName.lowercase()
                when (kind) {
                    "assign", "assignment", "workshop" -> {
                        val assignmentId = if (activity.instance > 0) activity.instance else activity.id
                        navController.navigate(ScreensRoute.AssignmentDetails.createRoute(courseId, assignmentId))
                    }
                    "quiz", "exam" -> {
                        // Moodle native quiz APIs accept quiz.id, and on some PSA activities they accept cmid.
                        // iOS sends the quiz instance when available and keeps cmid as fallback.
                        val quizId = if (activity.instance > 0) activity.instance else activity.id
                        navController.navigate(ScreensRoute.QuizAttempt.createRoute(quizId, activity.id))
                    }
                    "feedback", "survey" -> {
                        // Feedback accepts feedback.id or cmid. Prefer instance, keep cmid fallback in the screen if needed.
                        val feedbackId = if (activity.instance > 0) activity.instance else activity.id
                        navController.navigate(ScreensRoute.FeedbackSurvey.createRoute(feedbackId))
                    }
                    "scorm", "lesson", "page", "url", "lti", "book" -> {
                        navController.navigate(ScreensRoute.LessonVideo.createRoute(activity.id))
                    }
                    "resource", "folder" -> {
                        navController.navigate(ScreensRoute.CourseResources.createRoute(courseId))
                    }
                    else -> {
                        navController.navigate(ScreensRoute.LessonVideo.createRoute(activity.id))
                    }
                }
            },
            onResourcesClick = { _ ->
                navController.navigate(ScreensRoute.CourseResources.createRoute(courseId))
            }
        )
    }

    composable(
        route = ScreensRoute.CourseResources.route,
        arguments = listOf(navArgument("courseId") { type = NavType.IntType })
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
        val vm = remember { assembly.makeCourseResourcesViewModel() }
        CourseResourcesScreen(courseId = courseId, vm = vm, onBackClick = { navController.popBackStack() })
    }

    composable(
        route = ScreensRoute.LessonVideo.route,
        arguments = listOf(navArgument("cmid") { type = NavType.IntType })
    ) { backStackEntry ->
        val cmid = backStackEntry.arguments?.getInt("cmid") ?: 0
        val vm = remember { assembly.makeLessonVideoViewModel() }
        LessonVideoScreen(cmid = cmid, vm = vm, onBackClick = { navController.popBackStack() })
    }


    composable(
        route = ScreensRoute.QuizAttempt.route,
        arguments = listOf(
            navArgument("quizId") { type = NavType.IntType },
            navArgument("cmid") { type = NavType.IntType; defaultValue = 0 }
        )
    ) { backStackEntry ->
        val quizId = backStackEntry.arguments?.getInt("quizId") ?: 0
        val cmid = backStackEntry.arguments?.getInt("cmid") ?: 0
        val vm = remember { assembly.makeQuizAttemptViewModel() }
        QuizAttemptScreen(quizId = quizId, cmid = cmid, vm = vm, onBackClick = { navController.popBackStack() })
    }

    composable(
        route = ScreensRoute.FeedbackSurvey.route,
        arguments = listOf(navArgument("feedbackId") { type = NavType.IntType })
    ) { backStackEntry ->
        val feedbackId = backStackEntry.arguments?.getInt("feedbackId") ?: 0
        val vm = remember { assembly.makeFeedbackSurveyViewModel() }
        FeedbackSurveyScreen(feedbackId = feedbackId, vm = vm, onBackClick = { navController.popBackStack() })
    }

    composable(
        route = ScreensRoute.AssignmentDetails.route,
        arguments = listOf(
            navArgument("courseId") { type = NavType.IntType },
            navArgument("assignmentId") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getInt("courseId") ?: return@composable
        val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: return@composable
        val vm = remember { assembly.makeAssignmentsViewModel() } // Ensure this exists in your DependencyContainer

        AssignmentDetailsScreen(
            courseId = courseId,
            assignmentId = assignmentId,
            viewModel = vm,
            onBackClick = { navController.popBackStack() },
            onStartAssignmentClick = { id ->
                navController.navigate(ScreensRoute.AssignmentSubmission.createRoute(courseId, id))
            }
        )
    }



    // Backward-compatible route for older calls that navigate with only assignmentId,
    // e.g. navController.navigate("assignment_details/301"). Without this, the app
    // crashes with "destination cannot be found".
    composable(
        route = "assignment_details/{assignmentId}",
        arguments = listOf(
            navArgument("assignmentId") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: return@composable
        val vm = remember { assembly.makeAssignmentsViewModel() }
        AssignmentDetailsScreen(
            courseId = 101,
            assignmentId = assignmentId,
            viewModel = vm,
            onBackClick = { navController.popBackStack() },
            onStartAssignmentClick = { id ->
                navController.navigate(ScreensRoute.AssignmentSubmission.createRoute(101, id))
            }
        )
    }

    composable(
        route = ScreensRoute.AssignmentSubmission.route,
        arguments = listOf(
            navArgument("courseId") { type = NavType.IntType },
            navArgument("assignmentId") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getInt("courseId") ?: return@composable
        val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: return@composable
        val vm = remember { assembly.makeAssignmentsViewModel() }

        AssignmentSubmissionScreen(
            courseId = courseId,
            assignmentId = assignmentId,
            viewModel = vm,
            onBackClick = { navController.popBackStack() },
            onSubmissionSuccess = { navController.popBackStack() }
        )
    }
}



@Composable
fun MoodleGovAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SpColors.NavyBlue,
            secondary = SpColors.Gold,
            background = SpColors.LightGray,
            surface = SpColors.White,
            error = SpColors.Error
        ), content = content
    )
}