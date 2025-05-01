package com.example.spot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.spot.model.Section
import com.example.spot.model.Student
import com.example.spot.ui.theme.screens.*
import com.example.spot.viewmodel.StudentState
import com.google.gson.Gson

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP_STUDENT = "sign_up_student"
    const val SIGN_UP_INSTRUCTOR = "sign_up_instructor"
    const val DASHBOARD = "dashboard"
    const val CLASSES = "classes"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"
    const val ATTENDANCE_HISTORY = "attendance_history"
    const val ATTENDANCE_LOG = "attendance_log/{sectionId}"
    const val ATTENDANCE_CALENDAR = "attendance_calendar/{sectionId}"
    const val QR_SCANNER = "qr_scanner"
    const val CLASS_VIEW = "class_view/{classCode}"
    const val SEAT_PLAN = "seat_plan/{sectionId}"
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
        composable(Routes.EDIT_PROFILE) {
            // The EditProfileScreen will get the student data from the current StudentViewModel
            // which is passed to it via viewModel = viewModel()
            EditProfileScreen(navController = navController)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController)
        }
        composable(Routes.QR_SCANNER) {
            QrScannerScreen(navController)
        }
        composable(Routes.ATTENDANCE_HISTORY) {
            AttendanceHistoryScreen(navController)
        }
        composable(
            route = Routes.ATTENDANCE_LOG,
            arguments = listOf(
                navArgument("sectionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            AttendanceLogScreen(
                navController = navController,
                sectionId = sectionId
            )
        }
        composable(
            route = Routes.ATTENDANCE_CALENDAR,
            arguments = listOf(
                navArgument("sectionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            AttendanceCalendarScreen(
                navController = navController,
                sectionId = sectionId
            )
        }
        composable(Routes.CLASS_VIEW) { backStackEntry ->
            val classCode = backStackEntry.arguments?.getString("classCode") ?: ""
            ClassViewScreen(navController, classCode)
        }
        composable(
            route = Routes.SEAT_PLAN,
            arguments = listOf(
                navArgument("sectionId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getLong("sectionId") ?: 0L
            SeatPlanScreen(
                navController = navController,
                sectionId = sectionId
            )
        }
    }
}
