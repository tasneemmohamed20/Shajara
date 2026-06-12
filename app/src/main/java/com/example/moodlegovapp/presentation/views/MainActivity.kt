package com.example.moodlegovapp.presentation.views

import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.ui.theme.SpColors
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.moodlegovapp.R
import com.example.moodlegovapp.presentation.views.dashboard.DashboardScreen
import com.example.moodlegovapp.ui.theme.SpTypography
import com.gov.moodleapp.presentation.auth.LoginScreen

class MainActivity : ComponentActivity() {

    private lateinit var assembly: DependencyContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        assembly = DependencyContainer.getInstance(this)

        setContent {
            MoodleGovAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
                    AppRoot(assembly = assembly)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// APP ROOT — mirrors iOS RootView
// decides Login vs MainTabView
// ─────────────────────────────────────────────

@Composable
fun AppRoot(assembly: DependencyContainer) {
    val session = remember { assembly.sharedSession }
    val authToken by session.authToken.observeAsState()

    if (authToken != null) {
        MainTabView(assembly = assembly)
    } else {
        LoginScreen(
            onLoginSuccess = { /* StateFlow update triggers recompose */ },
            assembly       = assembly
        )
    }
}

// ─────────────────────────────────────────────
// MAIN TAB VIEW
// mirrors iOS MainTabView — 4 tabs
// ─────────────────────────────────────────────

sealed class Tab(val route: String, val labelRes: Int, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home          : Tab("home",          R.string.tab_home,          Icons.Default.Home,         Icons.Default.Home)
    object Courses       : Tab("courses",       R.string.tab_courses,       Icons.Default.Book,         Icons.Default.Book)
    object Notifications : Tab("notifications", R.string.tab_notifications, Icons.Default.Notifications,Icons.Default.Notifications)
    object Profile       : Tab("profile",       R.string.tab_profile,       Icons.Default.Person,       Icons.Default.Person)

    companion object {
        val all = listOf(Home, Courses, Notifications, Profile)
    }
}

@Composable
fun MainTabView(assembly: DependencyContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Tab.all.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected  = isSelected,
                        onClick   = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon      = { Icon(if (isSelected) tab.selectedIcon else tab.icon, contentDescription = null) },
                        label     = { Text(stringResource(tab.labelRes), style = SpTypography.small()) },
                        colors    = NavigationBarItemDefaults.colors(
                            selectedIconColor   = SpColors.NavyBlue,
                            selectedTextColor   = SpColors.NavyBlue,
                            unselectedIconColor = SpColors.DarkGray,
                            unselectedTextColor = SpColors.DarkGray,
                            indicatorColor      = SpColors.NavyBlue.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Tab.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Tab.Home.route) {
                DashboardScreen(
                    assembly      = assembly,
                    onCourseClick = { courseId ->
                        navController.navigate("course_detail/$courseId")
                    }
                )
            }
            composable(Tab.Courses.route) {
                // CoursesScreen — placeholder
                PlaceholderScreen(label = "Courses")
            }
            composable(Tab.Notifications.route) {
                PlaceholderScreen(label = "Notifications")
            }
            composable(Tab.Profile.route) {
                PlaceholderScreen(label = "Profile")
            }
            composable("course_detail/{courseId}") { backStack ->
                val courseId = backStack.arguments?.getString("courseId")?.toIntOrNull() ?: 0
                PlaceholderScreen(label = "Course Detail #$courseId")
            }
        }
    }
}

private val Int.dp_value get() = this

@Composable
private fun PlaceholderScreen(label: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = SpColors.LightGray) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(label, style = SpTypography.headingL(), color = SpColors.DarkBrown)
        }
    }
}

// ─────────────────────────────────────────────
// APP THEME
// ─────────────────────────────────────────────

@Composable
fun MoodleGovAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary    = SpColors.NavyBlue,
            secondary  = SpColors.Gold,
            background = SpColors.LightGray,
            surface    = SpColors.White,
            error      = SpColors.Error
        ),
        content = content
    )
}