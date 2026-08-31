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
    val isOnBreak by viewModel.isOnBreak.collectAsState()
    val breakStartTime by viewModel.breakStartTime.collectAsState()
    val shiftNotes by viewModel.shiftNotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Offline Sync, Charts & Simulation States
    val connectionState by viewModel.connectionState.collectAsState()
    val pendingPunches by viewModel.pendingPunches.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val dailyChartItems by viewModel.dailyChartItems.collectAsState()
    val chartSummary by viewModel.chartSummary.collectAsState()

    // Dark Mode, Notifications & Pull-to-Refresh States
    val darkModePreference by viewModel.darkModePreference.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val geofenceAlertsEnabled by viewModel.geofenceAlertsEnabled.collectAsState()
    val isRefreshingDashboard by viewModel.isRefreshingDashboard.collectAsState()
    val isRefreshingHistory by viewModel.isRefreshingHistory.collectAsState()
    val isRefreshingTimeOff by viewModel.isRefreshingTimeOff.collectAsState()

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
                biometricsEnabled = biometricsEnabled,
                onBack = {
                    navController.navigate(AppRoutes.SERVER_SETUP) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onLogin = { email, password, stayLoggedIn ->
                    viewModel.login(email, password, stayLoggedIn) {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.SERVER_SETUP) { inclusive = true }
                        }
                    }
                },
                onBiometricLogin = {
                    viewModel.biometricLogin {
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
                serverUrl = serverConfig.url,
                isCheckedIn = isCheckedIn,
                isOnBreak = isOnBreak,
                breakStartTime = breakStartTime,
                shiftNotes = shiftNotes,
                lastCheckInTime = lastCheckInTime,
                geofenceZone = geofenceZone,
                connectionState = connectionState,
                pendingPunches = pendingPunches,
                isSyncing = isSyncing,
                dailyChartItems = dailyChartItems,
                chartSummary = chartSummary,
                isRefreshing = isRefreshingDashboard,
                darkModePreference = darkModePreference,
                onToggleAttendance = { viewModel.toggleAttendance() },
                onToggleBreak = { viewModel.toggleBreak() },
                onAddShiftNote = { note -> viewModel.addShiftNote(note) },
                onRefreshLocation = { viewModel.refreshLocation() },
                onPullRefresh = { viewModel.refreshDashboard() },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onSendTestNotification = { type -> viewModel.sendTestNotification(type) },
                onSelectOfficePreset = { preset -> viewModel.selectOfficePreset(preset) },
                onSelectSimulation = { mockLoc -> viewModel.simulateLocation(mockLoc) },
                onSyncPendingPunches = { viewModel.syncPendingPunches() },
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
                darkModePreference = darkModePreference,
                notificationsEnabled = notificationsEnabled,
                geofenceAlertsEnabled = geofenceAlertsEnabled,
                onBack = { navController.popBackStack() },
                onLogout = {
                    viewModel.logout {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                        }
                    }
                },
                onChangeServer = {
                    viewModel.resetServer {
                        navController.navigate(AppRoutes.SERVER_SETUP) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                        }
                    }
                },
                onRevokeDevice = { id -> viewModel.revokeDevice(id) },
                onToggleBiometrics = { enabled -> viewModel.setBiometrics(enabled) },
                onSelectDarkMode = { mode -> viewModel.setDarkModePreference(mode) },
                onUpdateNotificationPreferences = { enabled, geofence ->
                    viewModel.setNotificationPreferences(enabled, geofence)
                },
                onSendTestNotification = { type -> viewModel.sendTestNotification(type) }
            )
        }
    }
}
