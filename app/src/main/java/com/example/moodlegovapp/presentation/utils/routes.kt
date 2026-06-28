package com.example.moodlegovapp.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moodlegovapp.R
import java.io.Serializable

sealed class ScreensRoute(val route: String) : Serializable {
    object Home : ScreensRoute("home")
    object Courses : ScreensRoute("courses")
    object Tasks : ScreensRoute("tasks")
    object Grades : ScreensRoute("grades")
    object Notifications : ScreensRoute("notifications")
    object Profile : ScreensRoute("profile")
    object LoginStepOne : ScreensRoute("loginOne")
    object LoginStepTwo : ScreensRoute("loginTwo")

    object ForgotPassword : ScreensRoute("forgot_password")
    object CheckEmail : ScreensRoute("check_email/{email}") {
        fun createRoute(email: String): String = "check_email/${android.net.Uri.encode(email)}"
    }
    object CourseResources : ScreensRoute("course_resources/{courseId}") {
        fun createRoute(courseId: Int): String = "course_resources/$courseId"
    }
    object LessonVideo : ScreensRoute("lesson_video/{cmid}") {
        fun createRoute(cmid: Int): String = "lesson_video/$cmid"
    }

    object QuizAttempt : ScreensRoute("quiz_attempt/{quizId}?cmid={cmid}") {
        fun createRoute(quizId: Int, cmid: Int = quizId): String = "quiz_attempt/$quizId?cmid=$cmid"
    }

    object FeedbackSurvey : ScreensRoute("feedback_survey/{feedbackId}") {
        fun createRoute(feedbackId: Int): String = "feedback_survey/$feedbackId"
    }

    object CourseDetail : ScreensRoute("course_detail/{courseId}?courseName={courseName}&progress={progress}") {
        fun createRoute(courseId: Int, courseName: String, progress: Int): String {
            val encodedName = android.net.Uri.encode(courseName)
            return "course_detail/$courseId?courseName=$encodedName&progress=$progress"
        }
    }

    // --- NEW ROUTES ---
    object AssignmentDetails : ScreensRoute("assignment_details/{courseId}/{assignmentId}") {
        fun createRoute(courseId: Int, assignmentId: Int): String = "assignment_details/$courseId/$assignmentId"
    }

    object AssignmentSubmission : ScreensRoute("assignmentSubmission/{courseId}/{assignmentId}") {
        fun createRoute(courseId: Int, assignmentId: Int) = "assignmentSubmission/$courseId/$assignmentId"
    }
}

sealed class Tab(
    val screenRoute: ScreensRoute,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : Tab(ScreensRoute.Home, R.string.tab_home, Icons.Default.Home, Icons.Default.Home)
    object Tasks : Tab(ScreensRoute.Tasks, R.string.tab_tasks, Icons.Default.Assignment, Icons.Default.Assignment)
    object Grades : Tab(ScreensRoute.Grades, R.string.tab_grades, Icons.Default.BarChart, Icons.Default.BarChart)
    object Profile : Tab(ScreensRoute.Profile, R.string.tab_profile, Icons.Default.Person, Icons.Default.Person)

    val route: String get() = screenRoute.route

    companion object {
        val all = listOf(Home, Tasks, Grades, Profile)
    }
}