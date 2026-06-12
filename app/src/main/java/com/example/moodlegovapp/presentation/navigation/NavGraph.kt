//package com.example.moodlegovapp.core.presentation.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.example.moodlegovapp.core.AppAssembly
//import com.example.moodlegovapp.core.data.session.AppSession
//import com.example.moodlegovapp.core.presentation.views.dashboard.DashboardScreen
//import com.example.moodlegovapp.core.presentation.viewmodels.DashboardViewModel
//import com.example.moodlegovapp.core.presentation.viewmodels.LoginViewModel
//
//object NavGraphRoutes {
//    const val LOGIN = "login"
//    const val DASHBOARD = "dashboard"
//}
//
//@Composable
//fun AppNavigation(
//    navController: NavHostController,
//    appAssembly: AppAssembly,
//    session: AppSession
//) {
//    // Observe authentication state
//    val authToken by session.authToken.observeAsState(initial = null)
//    val currentUser by session.currentUser.observeAsState(initial = null)
//
//    // Determine starting destination based on authentication state
//    val startDestination = if (authToken != null) {
//        NavGraphRoutes.DASHBOARD
//    } else {
//        NavGraphRoutes.LOGIN
//    }
//
//    NavHost(
//        navController = navController,
//        startDestination = startDestination
//    ) {
//        composable(NavGraphRoutes.LOGIN) {
//            val loginViewModel: LoginViewModel = viewModel(
//                factory = object : ViewModelProvider.Factory {
//                    @Suppress("UNCHECKED_CAST")
//                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                        return appAssembly.makeLoginViewModel() as T
//                    }
//                }
//            )
//
//            AuthLoginScreen(viewModel = loginViewModel)
//
//            // Navigate to dashboard once user is authenticated
//            LaunchedEffect(authToken) {
//                if (authToken != null) {
//                    navController.navigate(NavGraphRoutes.DASHBOARD) {
//                        popUpTo(NavGraphRoutes.LOGIN) { inclusive = true }
//                    }
//                }
//            }
//        }
//
//        composable(NavGraphRoutes.DASHBOARD) {
//            val dashboardViewModel: DashboardViewModel = viewModel(
//                factory = object : ViewModelProvider.Factory {
//                    @Suppress("UNCHECKED_CAST")
//                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                        return appAssembly.makeDashboardViewModel() as T
//                    }
//                }
//            )
//
//            DashboardScreen(
//                viewModel = dashboardViewModel,
//                onLogout = {
//                    session.logout()
//                    navController.navigate(NavGraphRoutes.LOGIN) {
//                        popUpTo(NavGraphRoutes.DASHBOARD) { inclusive = true }
//                    }
//                }
//            )
//
//            // Navigate back to login if logged out
//            LaunchedEffect(authToken) {
//                if (authToken == null) {
//                    navController.navigate(NavGraphRoutes.LOGIN) {
//                        popUpTo(NavGraphRoutes.DASHBOARD) { inclusive = true }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//
