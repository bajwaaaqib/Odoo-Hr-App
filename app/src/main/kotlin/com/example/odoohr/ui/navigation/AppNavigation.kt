package com.example.odoohr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.odoohr.ui.screens.AttendanceHistoryScreen
import com.example.odoohr.ui.screens.HomeScreen
import com.example.odoohr.ui.screens.LoginScreen
import com.example.odoohr.ui.screens.ProfileScreen
import com.example.odoohr.ui.screens.ServerSetupScreen
import com.example.odoohr.ui.screens.SplashScreen
import com.example.odoohr.ui.screens.TimeOffScreen
import com.example.odoohr.ui.viewmodel.AttendanceViewModel

object AppRoutes {
    const val SPLASH = "splash"
    const val SERVER_SETUP = "server_setup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val ATTENDANCE_HISTORY = "attendance_history"
    const val TIME_OFF = "time_off"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: AttendanceViewModel = viewModel()
) {
    val serverConfig by viewModel.serverConfig.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isCheckedIn by viewModel.isCheckedIn.collectAsState()
    val lastCheckInTime by viewModel.lastCheckInTime.collectAsState()
    val geofenceZone by viewModel.geofenceZone.collectAsState()
    val attendanceHistory by viewModel.attendanceHistory.collectAsState()
    val timeOffBalance by viewModel.timeOffBalance.collectAsState()
    val timeOffRecords by viewModel.timeOffRecords.collectAsState()
    val deviceSessions by viewModel.deviceSessions.collectAsState()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                isLoggedIn = isLoggedIn,
                isServerConfigured = serverConfig.isConfigured,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.SERVER_SETUP) {
            ServerSetupScreen(
                currentUrl = serverConfig.url,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onValidateAndContinue = { url ->
                    viewModel.configureServer(url) {
                        navController.navigate(AppRoutes.LOGIN)
                    }
                }
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                serverUrl = serverConfig.url,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onBack = {
                    navController.popBackStack()
                },
                onLogin = { email, password ->
                    viewModel.login(email, password) {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.SERVER_SETUP) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                userProfile = userProfile,
                isCheckedIn = isCheckedIn,
                lastCheckInTime = lastCheckInTime,
                geofenceZone = geofenceZone,
                onToggleAttendance = { viewModel.toggleAttendance() },
                onRefreshLocation = { viewModel.refreshLocation() },
                onNavigateToProfile = { navController.navigate(AppRoutes.PROFILE) },
                onNavigateToTimeOff = { navController.navigate(AppRoutes.TIME_OFF) },
                onNavigateToHistory = { navController.navigate(AppRoutes.ATTENDANCE_HISTORY) }
            )
        }

        composable(AppRoutes.ATTENDANCE_HISTORY) {
            AttendanceHistoryScreen(
                attendanceRecords = attendanceHistory,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.TIME_OFF) {
            TimeOffScreen(
                timeOffBalance = timeOffBalance,
                timeOffRecords = timeOffRecords,
                onBack = { navController.popBackStack() },
                onSubmitRequest = { type, start, end, days, reason ->
                    viewModel.submitTimeOff(type, start, end, days, reason) {}
                }
            )
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                userProfile = userProfile,
                serverUrl = serverConfig.url,
                deviceSessions = deviceSessions,
                biometricsEnabled = biometricsEnabled,
                onBack = { navController.popBackStack() },
                onLogout = {
                    viewModel.logout {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                        }
                    }
                },
                onRevokeDevice = { id -> viewModel.revokeDevice(id) },
                onToggleBiometrics = { enabled -> viewModel.setBiometrics(enabled) }
            )
        }
    }
}
