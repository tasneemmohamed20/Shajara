package com.example.moodlegovapp.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(val route: String, val label: String, val icon: ImageVector) {
    COURSES("main/courses", "Courses", Icons.Filled.MenuBook),
    TASKS("main/tasks", "Tasks", Icons.Filled.Assignment),
    GRADES("main/grades", "Grades", Icons.Filled.BarChart),
    PROFILE("main/profile", "Profile", Icons.Filled.Person)
}

//object NavGraphRoutes {
//    const val LOGIN = "login"
//    const val MAIN = "main"
//    const val COURSE_DETAIL = "course/{courseId}"
//    const val LESSON = "course/{courseId}/lesson/{activityId}"
//    const val NOTIFICATIONS = "notifications"
//    const val SETTINGS = "settings"
//
//    fun courseDetail(courseId: Int) = "course/$courseId"
//    fun lesson(courseId: Int, activityId: Int) = "course/$courseId/lesson/$activityId"
//}
