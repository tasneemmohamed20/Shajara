package com.example.moodlegovapp.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moodlegovapp.R
import java.io.Serializable

sealed class ScreensRoute(val route: String) : Serializable {
    object Home : ScreensRoute("home")
    object Courses : ScreensRoute("courses")
    object Notifications : ScreensRoute("notifications")
    object Profile : ScreensRoute("profile")
    object LoginStepOne : ScreensRoute("loginOne")
    object LoginStepTwo : ScreensRoute("loginTwo")

    object CourseDetail : ScreensRoute("course_detail/{courseId}?courseName={courseName}&progress={progress}") {
        fun createRoute(courseId: Int, courseName: String, progress: Int): String {
            val encodedName = android.net.Uri.encode(courseName)
            return "course_detail/$courseId?courseName=$encodedName&progress=$progress"
        }
    }

    // --- NEW ROUTES ---
    object AssignmentDetails : ScreensRoute("assignment_details/{assignmentId}") {
        fun createRoute(assignmentId: Int): String = "assignment_details/$assignmentId"
    }

    object AssignmentSubmission : ScreensRoute("assignmentSubmission/{assignmentId}") {
        fun createRoute(assignmentId: Int) = "assignmentSubmission/$assignmentId"
    }
}

sealed class Tab(
    val screenRoute: ScreensRoute,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : Tab(ScreensRoute.Home, R.string.tab_home, Icons.Default.Home, Icons.Default.Home)
    object Courses : Tab(ScreensRoute.Courses, R.string.tab_courses, Icons.Default.Book, Icons.Default.Book)
    object Notifications : Tab(
        ScreensRoute.Notifications,
        R.string.tab_notifications,
        Icons.Default.Notifications,
        Icons.Default.Notifications
    )
    object Profile : Tab(ScreensRoute.Profile, R.string.tab_profile, Icons.Default.Person, Icons.Default.Person)

    val route: String get() = screenRoute.route

    companion object {
        val all = listOf(Home, Courses, Notifications, Profile)
    }
}