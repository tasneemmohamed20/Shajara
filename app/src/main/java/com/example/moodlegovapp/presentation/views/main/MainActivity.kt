package com.example.moodlegovapp.presentation.views.main

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.moodlegovapp.presentation.views.auth.LoginStepOneView
import com.example.moodlegovapp.presentation.views.coursedetails.CourseOverviewScreen
import com.example.moodlegovapp.presentation.views.dashboard.DashboardScreen
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography
import com.gov.moodleapp.presentation.auth.LoginStepTwoView
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var assembly: DependencyContainer
    private var isKeepShowing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isKeepShowing }

        // Read persisted language and apply it before super.onCreate() to avoid layout flashing
        val dsm =
            DataStoreManager.getInstance(applicationContext)
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
                AppColors.Navy.toArgb(),
                AppColors.Navy.toArgb()
            )
        )
        assembly = DependencyContainer.getInstance(this)

        setContent {
            MoodleGovAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
                    val session = remember { assembly.sharedSession }

                    val isInitialized by session.isInitialized.observeAsState(initial = false)

                    if (isInitialized) {
                        val rootNavController = rememberNavController()
                        val startRoute = if (session.isAuthenticated) "main_app_root" else "auth_root"

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
            onBackClicked = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.mainAppGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    assembly: DependencyContainer
) {
    composable(ScreensRoute.Home.route) {
        DashboardScreen(
            assembly = assembly,
            onCourseClick = { courseId ->
                navController.navigate(ScreensRoute.CourseDetail.createRoute(courseId))
            },
            onLeaderboardClick = {
                // Navigate to full leaderboard screen
                // e.g., navController.navigate("leaderboard")
            }
        )
    }

    composable(ScreensRoute.Courses.route) {
        Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Courses", style = SpTypography.headingL(), color = SpColors.DarkBrown)
            }
        }
    }

    composable(ScreensRoute.Notifications.route) {
        Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Notifications", style = SpTypography.headingL(), color = SpColors.DarkBrown)
            }
        }
    }

    composable(ScreensRoute.Profile.route) {
        val vm = remember { assembly.makeProfileViewModel() }
        LaunchedEffect(Unit) {
            vm.loadAll()
        }
        val scope = rememberCoroutineScope()

        ProfileScreen(
            viewModel = vm,
            onLogOutClick = {
                scope.launch {
                    assembly.sharedSession.logout()
                    rootNavController.navigate("auth_root") {
                        popUpTo("main_app_root") { inclusive = true }
                    }
                }
            }
        )
    }

    composable(
        route = ScreensRoute.CourseDetail.route,
        arguments = listOf(navArgument("courseId") { type = NavType.IntType })
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
        val vm = remember(courseId) { assembly.makeCourseDetailViewModel(courseId) }

        CourseOverviewScreen(
            vm = vm,
            onBackClick = { navController.popBackStack() },
            onReviewAssignmentClick = { },
            onActivityClick = { },
            onResourcesClick = { }
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
        ),
        content = content
    )
}