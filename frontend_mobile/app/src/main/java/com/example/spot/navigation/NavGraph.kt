package com.example.spot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spot.ui.theme.screens.*

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP_STUDENT = "sign_up_student"
    const val SIGN_UP_INSTRUCTOR = "sign_up_instructor"
    const val DASHBOARD = "dashboard"
    const val CLASSES = "classes"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val ATTENDANCE_LOG = "attendance_log"
    const val QR_SCANNER = "qr_scanner"
    const val CLASS_VIEW = "class_view/{classCode}"
    const val WELCOME = "welcome"
}

@Composable
fun AppNavGraph(
    navController: NavHostController, 
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, 
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.WELCOME) {
            WelcomeScreen(navController)
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(navController)
        }
        composable(Routes.CLASSES) {
            ClassesScreen(navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController)
        }
        composable(Routes.QR_SCANNER) {
            QrScannerScreen(navController)
        }
        composable(Routes.ATTENDANCE_LOG) {
            AttendanceLogScreen(navController)
        }
        composable(Routes.CLASS_VIEW) { backStackEntry ->
            val classCode = backStackEntry.arguments?.getString("classCode") ?: ""
            ClassViewScreen(navController, classCode)
        }
    }
}
