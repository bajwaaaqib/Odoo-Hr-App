package com.example.odoohr.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.AttendanceChartSummary
import com.example.odoohr.data.model.DailyAttendanceChartItem
import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.OdooConnectionState
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.ui.components.AttendanceDataChart
import com.example.odoohr.ui.components.GeofenceControlCard
import com.example.odoohr.ui.components.OfflineSyncBanner
import com.example.odoohr.ui.components.PullRefreshLayout
import com.example.odoohr.ui.theme.BackgroundLight
import com.example.odoohr.ui.theme.BorderLight
import com.example.odoohr.ui.theme.ErrorRed
import com.example.odoohr.ui.theme.ErrorRedLight
import com.example.odoohr.ui.theme.InfoBlue
import com.example.odoohr.ui.theme.PrimaryBlue
import com.example.odoohr.ui.theme.PrimaryBlueContainer
import com.example.odoohr.ui.theme.PrimaryBlueDark
import com.example.odoohr.ui.theme.PrimaryBlueLight
import com.example.odoohr.ui.theme.SuccessGreen
import com.example.odoohr.ui.theme.SuccessGreenLight
import com.example.odoohr.ui.theme.TextMuted
import com.example.odoohr.ui.theme.TextPrimary
import com.example.odoohr.ui.theme.TextSecondary
import com.example.odoohr.ui.theme.WarningOrange
import com.example.odoohr.ui.theme.WarningOrangeLight
import com.example.odoohr.util.GeofenceCalculator

enum class DashboardNavTab {
    DASHBOARD,
    HISTORY,
    TIME_OFF,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
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
    currentTab: DashboardNavTab = DashboardNavTab.DASHBOARD,
    isRefreshing: Boolean = false,
    darkModePreference: String = "SYSTEM",
    onTabSelected: (DashboardNavTab) -> Unit,
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
    var showBreakDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("") }
    var showConfirmationToast by remember { mutableStateOf(false) }
    var confirmationMessage by remember { mutableStateOf("") }

    // Pulsing animation for active shift button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "GeoFence Attendance",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (connectionState == OdooConnectionState.ONLINE) SuccessGreen else WarningOrange,
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = if (serverUrl.isNotBlank()) {
                                        serverUrl.removePrefix("https://").removePrefix("http://").substringBefore("/")
                                    } else "Odoo Enterprise",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.testTag("dashboard_dark_mode_toggle_action")
                        ) {
                            Icon(
                                imageVector = if (darkModePreference == "DARK") Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { showNotificationDialog = true },
                            modifier = Modifier.testTag("dashboard_notifications_test_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Test Push Notifications",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = onRefreshLocation,
                            modifier = Modifier.testTag("dashboard_refresh_gps_top")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync GPS Location",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.testTag("dashboard_profile_action")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.avatarInitials,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlueDark
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("dashboard_bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == DashboardNavTab.DASHBOARD,
                            onClick = { onTabSelected(DashboardNavTab.DASHBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard"
                                )
                            },
                            label = { Text("Dashboard", fontWeight = if (currentTab == DashboardNavTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )

                        NavigationBarItem(
                            selected = currentTab == DashboardNavTab.HISTORY,
                            onClick = {
                                onTabSelected(DashboardNavTab.HISTORY)
                                onNavigateToHistory()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                                    contentDescription = "Attendance Logs"
                                )
                            },
                            label = { Text("History", fontWeight = if (currentTab == DashboardNavTab.HISTORY) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_history")
                        )

                        NavigationBarItem(
                            selected = currentTab == DashboardNavTab.TIME_OFF,
                            onClick = {
                                onTabSelected(DashboardNavTab.TIME_OFF)
                                onNavigateToTimeOff()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.TIME_OFF) Icons.Default.BeachAccess else Icons.Outlined.BeachAccess,
                                    contentDescription = "Time Off"
                                )
                            },
                            label = { Text("Time Off", fontWeight = if (currentTab == DashboardNavTab.TIME_OFF) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_time_off")
                        )

                        NavigationBarItem(
                            selected = currentTab == DashboardNavTab.PROFILE,
                            onClick = {
                                onTabSelected(DashboardNavTab.PROFILE)
                                onNavigateToProfile()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.PROFILE) Icons.Default.Person else Icons.Outlined.PersonOutline,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile", fontWeight = if (currentTab == DashboardNavTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_profile")
                        )
                    }
                }
            },
            containerColor = BackgroundLight
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation Rail on Wide Screens (Tablets / Foldables)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("dashboard_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationRailItem(
                            selected = currentTab == DashboardNavTab.DASHBOARD,
                            onClick = { onTabSelected(DashboardNavTab.DASHBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard"
                                )
                            },
                            label = { Text("Dashboard") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_dashboard")
                        )

                        NavigationRailItem(
                            selected = currentTab == DashboardNavTab.HISTORY,
                            onClick = {
                                onTabSelected(DashboardNavTab.HISTORY)
                                onNavigateToHistory()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                                    contentDescription = "History"
                                )
                            },
                            label = { Text("History") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_history")
                        )

                        NavigationRailItem(
                            selected = currentTab == DashboardNavTab.TIME_OFF,
                            onClick = {
                                onTabSelected(DashboardNavTab.TIME_OFF)
                                onNavigateToTimeOff()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.TIME_OFF) Icons.Default.BeachAccess else Icons.Outlined.BeachAccess,
                                    contentDescription = "Time Off"
                                )
                            },
                            label = { Text("Time Off") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_time_off")
                        )

                        NavigationRailItem(
                            selected = currentTab == DashboardNavTab.PROFILE,
                            onClick = {
                                onTabSelected(DashboardNavTab.PROFILE)
                                onNavigateToProfile()
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == DashboardNavTab.PROFILE) Icons.Default.Person else Icons.Outlined.PersonOutline,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlueContainer
                            ),
                            modifier = Modifier.testTag("nav_rail_profile")
                        )
                    }
                }

                // Pull-to-Refresh Container wrapping the entire dashboard scroll area
                PullRefreshLayout(
                    isRefreshing = isRefreshing,
                    onRefresh = onPullRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    val scrollState = rememberScrollState()

                    if (isWideScreen) {
                        // Two-Column Responsive Layout for Tablets
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Left Column
                            Column(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OfflineSyncBanner(
                                    connectionState = connectionState,
                                    pendingPunches = pendingPunches,
                                    isSyncing = isSyncing,
                                    onSyncNow = onSyncPendingPunches
                                )

                                EmployeeProfileSummaryCard(
                                    userProfile = userProfile,
                                    isCheckedIn = isCheckedIn,
                                    isOnBreak = isOnBreak,
                                    onNavigateToProfile = onNavigateToProfile
                                )

                                AttendanceHeroCard(
                                    isCheckedIn = isCheckedIn,
                                    isOnBreak = isOnBreak,
                                    lastCheckInTime = lastCheckInTime,
                                    breakStartTime = breakStartTime,
                                    pulseScale = pulseScale,
                                    onToggleAttendance = {
                                        onToggleAttendance()
                                        confirmationMessage = if (!isCheckedIn) "Checked In Successfully!" else "Checked Out. Shift recorded."
                                        showConfirmationToast = true
                                    }
                                )

                                TodayMetricsCard(
                                    isCheckedIn = isCheckedIn,
                                    lastCheckInTime = lastCheckInTime,
                                    isOnBreak = isOnBreak
                                )
                            }

                            // Right Column
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                GeofenceControlCard(
                                    geofenceZone = geofenceZone,
                                    onRefreshLocation = onRefreshLocation,
                                    onSelectPreset = onSelectOfficePreset,
                                    onSelectSimulation = onSelectSimulation
                                )

                                AttendanceDataChart(
                                    dailyItems = dailyChartItems,
                                    summary = chartSummary
                                )

                                QuickActionsGrid(
                                    isCheckedIn = isCheckedIn,
                                    isOnBreak = isOnBreak,
                                    onToggleAttendance = {
                                        onToggleAttendance()
                                        confirmationMessage = if (!isCheckedIn) "Checked In Successfully!" else "Checked Out."
                                        showConfirmationToast = true
                                    },
                                    onOpenBreakDialog = { showBreakDialog = true },
                                    onRefreshLocation = onRefreshLocation,
                                    onNavigateToTimeOff = onNavigateToTimeOff,
                                    onNavigateToHistory = onNavigateToHistory,
                                    onOpenNoteDialog = { showNoteDialog = true }
                                )

                                if (shiftNotes.isNotEmpty()) {
                                    ShiftNotesCard(
                                        notes = shiftNotes,
                                        onAddNote = { showNoteDialog = true }
                                    )
                                }
                            }
                        }
                    } else {
                        // Mobile Vertical Layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Offline Sync Status Banner (if offline or pending)
                            OfflineSyncBanner(
                                connectionState = connectionState,
                                pendingPunches = pendingPunches,
                                isSyncing = isSyncing,
                                onSyncNow = onSyncPendingPunches
                            )

                            // 2. Employee Profile Summary Card
                            EmployeeProfileSummaryCard(
                                userProfile = userProfile,
                                isCheckedIn = isCheckedIn,
                                isOnBreak = isOnBreak,
                                onNavigateToProfile = onNavigateToProfile
                            )

                            // 3. Attendance Status Hero Card
                            AttendanceHeroCard(
                                isCheckedIn = isCheckedIn,
                                isOnBreak = isOnBreak,
                                lastCheckInTime = lastCheckInTime,
                                breakStartTime = breakStartTime,
                                pulseScale = pulseScale,
                                onToggleAttendance = {
                                    onToggleAttendance()
                                    confirmationMessage = if (!isCheckedIn) "Checked In Successfully!" else "Checked Out. Shift recorded."
                                    showConfirmationToast = true
                                }
                            )

                            // 4. Geofence & GPS Location Radar Card with Presets & Simulation
                            GeofenceControlCard(
                                geofenceZone = geofenceZone,
                                onRefreshLocation = onRefreshLocation,
                                onSelectPreset = onSelectOfficePreset,
                                onSelectSimulation = onSelectSimulation
                            )

                            // 5. Attendance Analytics & Weekly Chart
                            AttendanceDataChart(
                                dailyItems = dailyChartItems,
                                summary = chartSummary
                            )

                            // 6. Quick Action Buttons Grid
                            QuickActionsGrid(
                                isCheckedIn = isCheckedIn,
                                isOnBreak = isOnBreak,
                                onToggleAttendance = {
                                    onToggleAttendance()
                                    confirmationMessage = if (!isCheckedIn) "Checked In Successfully!" else "Checked Out."
                                    showConfirmationToast = true
                                },
                                onOpenBreakDialog = { showBreakDialog = true },
                                onRefreshLocation = onRefreshLocation,
                                onNavigateToTimeOff = onNavigateToTimeOff,
                                onNavigateToHistory = onNavigateToHistory,
                                onOpenNoteDialog = { showNoteDialog = true }
                            )

                            // 7. Today's Shift Metrics Summary
                            TodayMetricsCard(
                                isCheckedIn = isCheckedIn,
                                lastCheckInTime = lastCheckInTime,
                                isOnBreak = isOnBreak
                            )

                            // 8. Shift Remarks & Notes
                            if (shiftNotes.isNotEmpty()) {
                                ShiftNotesCard(
                                    notes = shiftNotes,
                                    onAddNote = { showNoteDialog = true }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // Push Notification Trigger Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Test Push Notifications", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trigger real Android notification channel alerts to test push behaviors:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            onSendTestNotification("CHECK_IN")
                            showNotificationDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Check-In Push")
                    }
                    Button(
                        onClick = {
                            onSendTestNotification("GEOFENCE")
                            showNotificationDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Geofence Enter Alert")
                    }
                    Button(
                        onClick = {
                            onSendTestNotification("SYNC")
                            showNotificationDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Offline Sync Alert")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Break Dialog
    if (showBreakDialog) {
        AlertDialog(
            onDismissRequest = { showBreakDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = "Break Icon",
                    tint = WarningOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isOnBreak) "End Current Break" else "Log a Shift Break",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isOnBreak) {
                            "You are currently on break since ${breakStartTime ?: "earlier"}. Ready to resume your active shift?"
                        } else {
                            "Select your break type or pause your shift for meal/rest:"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    if (!isOnBreak) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarningOrangeLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "☕ 15m Coffee",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningOrange,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarningOrangeLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🍱 30m Lunch",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningOrange,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBreak()
                        showBreakDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnBreak) SuccessGreen else WarningOrange
                    )
                ) {
                    Text(
                        text = if (isOnBreak) "Resume Shift" else "Start Break",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Shift Note Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                    contentDescription = "Note Icon",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = "Add Shift Note",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Record a quick handover note, completed task, or remark for your attendance log:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        label = { Text("Note content") },
                        placeholder = { Text("e.g. Completed client meeting, shift handover done.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInputText.isNotBlank()) {
                            onAddShiftNote(noteInputText.trim())
                            noteInputText = ""
                        }
                        showNoteDialog = false
                    },
                    enabled = noteInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Note", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Component: Employee Profile Summary Card
// -------------------------------------------------------------
@Composable
fun EmployeeProfileSummaryCard(
    userProfile: UserProfile,
    isCheckedIn: Boolean,
    isOnBreak: Boolean,
    onNavigateToProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToProfile() }
            .testTag("dashboard_profile_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlueLight, PrimaryBlueDark)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.avatarInitials.ifEmpty { "U" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = when {
                                isOnBreak -> WarningOrange
                                isCheckedIn -> SuccessGreen
                                else -> TextMuted
                            },
                            shape = CircleShape
                        )
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userProfile.name.ifEmpty { "Alex Morgan" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = userProfile.employeeId.ifEmpty { "EMP-042" },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${userProfile.position} • ${userProfile.department}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isOnBreak -> WarningOrangeLight
                        isCheckedIn -> SuccessGreenLight
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = when {
                                        isOnBreak -> WarningOrange
                                        isCheckedIn -> SuccessGreen
                                        else -> TextMuted
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when {
                                isOnBreak -> "ON BREAK"
                                isCheckedIn -> "ON DUTY (Checked In)"
                                else -> "OFF DUTY (Checked Out)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = when {
                                    isOnBreak -> WarningOrange
                                    isCheckedIn -> SuccessGreen
                                    else -> TextSecondary
                                }
                            )
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Profile",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Component: Attendance Status Hero Card
// -------------------------------------------------------------
@Composable
fun AttendanceHeroCard(
    isCheckedIn: Boolean,
    isOnBreak: Boolean,
    lastCheckInTime: String?,
    breakStartTime: String?,
    pulseScale: Float,
    onToggleAttendance: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_attendance_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isOnBreak -> WarningOrangeLight
                        isCheckedIn -> SuccessGreenLight
                        else -> Color(0xFFF1F5F9)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isCheckedIn) SuccessGreen else if (isOnBreak) WarningOrange else TextMuted,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isOnBreak -> "BREAK IN PROGRESS"
                                isCheckedIn -> "ACTIVE SHIFT"
                                else -> "NOT CHECKED IN"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isOnBreak -> WarningOrange
                                    isCheckedIn -> SuccessGreen
                                    else -> TextSecondary
                                }
                            )
                        )
                    }
                }

                Text(
                    text = "Shift: 08:30 - 17:30",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Pulsing Action Button
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(if (isCheckedIn && !isOnBreak) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        brush = when {
                            isOnBreak -> Brush.radialGradient(listOf(WarningOrangeLight, WarningOrange))
                            isCheckedIn -> Brush.radialGradient(listOf(ErrorRedLight, ErrorRed))
                            else -> Brush.radialGradient(listOf(SuccessGreenLight, SuccessGreen))
                        }
                    )
                    .border(
                        width = 4.dp,
                        color = when {
                            isOnBreak -> WarningOrange.copy(alpha = 0.5f)
                            isCheckedIn -> ErrorRed.copy(alpha = 0.5f)
                            else -> SuccessGreen.copy(alpha = 0.5f)
                        },
                        shape = CircleShape
                    )
                    .clickable { onToggleAttendance() }
                    .testTag("dashboard_main_punch_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCheckedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                        contentDescription = if (isCheckedIn) "Check Out" else "Check In",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isCheckedIn) "CHECK OUT" else "CHECK IN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = if (isCheckedIn) "Tap to finish" else "Tap to start",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isCheckedIn) {
                        "Checked in at ${lastCheckInTime ?: "08:45"} • Shift duration running"
                    } else {
                        "Ready for next punch • Geofence ready"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Quick Actions Grid
// -------------------------------------------------------------
@Composable
fun QuickActionsGrid(
    isCheckedIn: Boolean,
    isOnBreak: Boolean,
    onToggleAttendance: () -> Unit,
    onOpenBreakDialog: () -> Unit,
    onRefreshLocation: () -> Unit,
    onNavigateToTimeOff: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenNoteDialog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_quick_actions_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButtonItem(
                    title = if (isCheckedIn) "Check Out" else "Check In",
                    subtitle = if (isCheckedIn) "End Shift" else "Start Shift",
                    icon = if (isCheckedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                    iconBgColor = if (isCheckedIn) ErrorRedLight else SuccessGreenLight,
                    iconTint = if (isCheckedIn) ErrorRed else SuccessGreen,
                    testTag = "quick_action_check_in_out",
                    modifier = Modifier.weight(1f),
                    onClick = onToggleAttendance
                )

                QuickActionButtonItem(
                    title = if (isOnBreak) "Resume" else "Take Break",
                    subtitle = if (isOnBreak) "End Break" else "Coffee/Meal",
                    icon = Icons.Default.Coffee,
                    iconBgColor = WarningOrangeLight,
                    iconTint = WarningOrange,
                    testTag = "quick_action_break",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBreakDialog
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButtonItem(
                    title = "Verify GPS",
                    subtitle = "Location check",
                    icon = Icons.Default.LocationOn,
                    iconBgColor = PrimaryBlue.copy(alpha = 0.12f),
                    iconTint = PrimaryBlue,
                    testTag = "quick_action_location",
                    modifier = Modifier.weight(1f),
                    onClick = onRefreshLocation
                )

                QuickActionButtonItem(
                    title = "Time Off",
                    subtitle = "Request leave",
                    icon = Icons.Default.BeachAccess,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF7C3AED),
                    testTag = "quick_action_time_off",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTimeOff
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButtonItem(
                    title = "Shift Logs",
                    subtitle = "Past records",
                    icon = Icons.Default.History,
                    iconBgColor = Color(0xFFE0F2FE),
                    iconTint = InfoBlue,
                    testTag = "quick_action_history",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory
                )

                QuickActionButtonItem(
                    title = "Shift Note",
                    subtitle = "Add memo",
                    icon = Icons.AutoMirrored.Filled.NoteAdd,
                    iconBgColor = Color(0xFFF3F4F6),
                    iconTint = TextPrimary,
                    testTag = "quick_action_note",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenNoteDialog
                )
            }
        }
    }
}

@Composable
fun QuickActionButtonItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Today's Shift Metrics Summary Card
// -------------------------------------------------------------
@Composable
fun TodayMetricsCard(
    isCheckedIn: Boolean,
    lastCheckInTime: String?,
    isOnBreak: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_today_metrics_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Work Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Standard Shift",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Check In",
                    value = lastCheckInTime ?: "--:--",
                    color = PrimaryBlue
                )
                MetricItem(
                    label = "Check Out",
                    value = if (isCheckedIn) "17:45 (Est)" else "--:--",
                    color = TextPrimary
                )
                MetricItem(
                    label = "Worked Hours",
                    value = if (isCheckedIn) "8h 12m" else "--:--",
                    color = SuccessGreen
                )
                MetricItem(
                    label = "Break Time",
                    value = if (isOnBreak) "In Progress" else "0h 30m",
                    color = WarningOrange
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 13.sp
            )
        )
    }
}

// -------------------------------------------------------------
// Component: Shift Notes Card
// -------------------------------------------------------------
@Composable
fun ShiftNotesCard(
    notes: List<String>,
    onAddNote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_shift_notes_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shift Remarks & Notes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                TextButton(
                    onClick = onAddNote,
                    modifier = Modifier.testTag("dashboard_add_note_button")
                ) {
                    Text("+ Add", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            notes.take(3).forEach { note ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
