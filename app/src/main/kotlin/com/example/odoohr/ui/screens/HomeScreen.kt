package com.example.odoohr.ui.screens

import androidx.compose.runtime.Composable
import com.example.odoohr.data.model.AttendanceChartSummary
import com.example.odoohr.data.model.DailyAttendanceChartItem
import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.OdooConnectionState
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.util.GeofenceCalculator

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    serverUrl: String = "",
    isCheckedIn: Boolean,
    isOnBreak: Boolean = false,
    breakStartTime: String? = null,
    shiftNotes: List<String> = emptyList(),
    lastCheckInTime: String?,
    geofenceZone: GeofenceZone,
    connectionState: OdooConnectionState = OdooConnectionState.ONLINE,
    pendingPunches: List<OfflinePunchRecord> = emptyList(),
    isSyncing: Boolean = false,
    dailyChartItems: List<DailyAttendanceChartItem> = emptyList(),
    chartSummary: AttendanceChartSummary = AttendanceChartSummary(),
    isRefreshing: Boolean = false,
    darkModePreference: String = "SYSTEM",
    onToggleAttendance: () -> Unit,
    onToggleBreak: () -> Unit = {},
    onAddShiftNote: (String) -> Unit = {},
    onRefreshLocation: () -> Unit,
    onPullRefresh: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onSendTestNotification: (String) -> Unit = {},
    onSelectOfficePreset: (GeofenceLocationPreset) -> Unit = {},
    onSelectSimulation: (GeofenceCalculator.MockLocationPoint) -> Unit = {},
    onSyncPendingPunches: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToTimeOff: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    DashboardScreen(
        userProfile = userProfile,
        serverUrl = serverUrl,
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
        currentTab = DashboardNavTab.DASHBOARD,
        isRefreshing = isRefreshing,
        darkModePreference = darkModePreference,
        onTabSelected = { tab ->
            when (tab) {
                DashboardNavTab.DASHBOARD -> { /* Already on dashboard */ }
                DashboardNavTab.HISTORY -> onNavigateToHistory()
                DashboardNavTab.TIME_OFF -> onNavigateToTimeOff()
                DashboardNavTab.PROFILE -> onNavigateToProfile()
            }
        },
        onToggleAttendance = onToggleAttendance,
        onToggleBreak = onToggleBreak,
        onAddShiftNote = onAddShiftNote,
        onRefreshLocation = onRefreshLocation,
        onPullRefresh = onPullRefresh,
        onToggleDarkMode = onToggleDarkMode,
        onSendTestNotification = onSendTestNotification,
        onSelectOfficePreset = onSelectOfficePreset,
        onSelectSimulation = onSelectSimulation,
        onSyncPendingPunches = onSyncPendingPunches,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToTimeOff = onNavigateToTimeOff,
        onNavigateToHistory = onNavigateToHistory
    )
}
