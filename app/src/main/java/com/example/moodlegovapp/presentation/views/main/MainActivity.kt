package com.example.moodlegovapp.presentation.views.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.presentation.utils.ScreensRoute
import com.example.moodlegovapp.presentation.viewmodels.LoginViewModel
import com.example.moodlegovapp.presentation.views.auth.LoginStepOneView
import com.example.moodlegovapp.presentation.views.coursedetails.CourseOverviewScreen
import com.example.moodlegovapp.presentation.views.dashboard.DashboardScreen
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography
import com.gov.moodleapp.presentation.auth.LoginStepTwoView

class MainActivity : AppCompatActivity() {

    private lateinit var assembly: DependencyContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        // Read persisted language and apply it before super.onCreate() to avoid layout flashing
        val dsm = com.example.moodlegovapp.data.service.DataStoreManager.getInstance(applicationContext)
        val savedLang = kotlinx.coroutines.runBlocking {
            dsm.get<String>(com.example.moodlegovapp.data.service.DataStoreManager.KEY_LANGUAGE)
        }
        if (savedLang != null) {
            val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            val currentLang = if (locales.isEmpty) "en" else locales[0]?.language ?: "en"
            if (savedLang != currentLang) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(savedLang)
                )
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        assembly = DependencyContainer.getInstance(this)

        setContent {
            MoodleGovAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
                    val session = remember { assembly.sharedSession }
                    val authToken by session.authToken.observeAsState()

                    val rootNavController = rememberNavController()

                    NavHost(
                        navController = rootNavController,
                        startDestination = ScreensRoute.Splash.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(ScreensRoute.Splash.route) {
                            com.example.moodlegovapp.presentation.views.splash.SplashScreen(
                                session = session,
                                onSplashFinished = { isAuthenticated ->
                                    val destination = if (isAuthenticated) "main_app_root" else "auth_root"
                                    rootNavController.navigate(destination) {
                                        popUpTo(ScreensRoute.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

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

                        // Authenticated Main Feature Context Wrapper
                        composable("main_app_root") {
                            val mainNavController = rememberNavController()
                            MainScreen(navController = mainNavController) {
                                NavHost(
                                    navController = mainNavController,
                                    startDestination = ScreensRoute.Home.route
                                ) {
                                    mainAppGraph(
                                        navController = mainNavController,
                                        assembly = assembly
                                    )
                                }
                            }
                        }
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
        Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile", style = SpTypography.headingL(), color = SpColors.DarkBrown)
            }
        }
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