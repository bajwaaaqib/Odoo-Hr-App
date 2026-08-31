package com.example.odoohr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.DeviceSession
import com.example.odoohr.data.model.UserProfile
import com.example.odoohr.ui.theme.ErrorRed
import com.example.odoohr.ui.theme.PrimaryBlue
import com.example.odoohr.ui.theme.PrimaryBlueContainer
import com.example.odoohr.ui.theme.PrimaryBlueDark
import com.example.odoohr.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    serverUrl: String,
    deviceSessions: List<DeviceSession>,
    biometricsEnabled: Boolean,
    darkModePreference: String = "SYSTEM",
    notificationsEnabled: Boolean = true,
    geofenceAlertsEnabled: Boolean = true,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeServer: () -> Unit,
    onRevokeDevice: (String) -> Unit,
    onToggleBiometrics: (Boolean) -> Unit,
    onSelectDarkMode: (String) -> Unit = {},
    onUpdateNotificationPreferences: (Boolean, Boolean) -> Unit = { _, _ -> },
    onSendTestNotification: (String) -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSwitchServerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showNotificationTestDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlueDark
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Header
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.avatarInitials,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 1: Appearance & Dark Mode Toggle
            // -------------------------------------------------------------
            SectionTitle(title = "Appearance & Theme")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (darkModePreference == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Dark Mode Icon",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Dark Mode",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = when (darkModePreference) {
                                        "DARK" -> "Dark theme enabled"
                                        "LIGHT" -> "Light theme active"
                                        else -> "Matches Android system appearance"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = darkModePreference == "DARK",
                            onCheckedChange = { checked ->
                                onSelectDarkMode(if (checked) "DARK" else "LIGHT")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            ),
                            modifier = Modifier.testTag("dark_mode_switch_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = "Theme Preference Mode",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "SYSTEM" to "System",
                            "LIGHT" to "Light",
                            "DARK" to "Dark"
                        ).forEach { (modeKey, modeLabel) ->
                            val isSelected = darkModePreference == modeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectDarkMode(modeKey) },
                                label = {
                                    Text(
                                        text = modeLabel,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    if (modeKey == "DARK") {
                                        Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    } else if (modeKey == "LIGHT") {
                                        Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_chip_$modeKey")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 2: Push Notifications & Alerts
            // -------------------------------------------------------------
            SectionTitle(title = "Push Notifications & Alerts")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Push Notifications",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Attendance Alerts",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Live check-in, check-out & shift status",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { checked ->
                                onUpdateNotificationPreferences(checked, geofenceAlertsEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            ),
                            modifier = Modifier.testTag("notifications_master_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Geofence Alerts",
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Geofence Boundary Proximity",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Alerts when entering verified office zone",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = geofenceAlertsEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = { checked ->
                                onUpdateNotificationPreferences(notificationsEnabled, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen
                            ),
                            modifier = Modifier.testTag("geofence_notifications_toggle")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ProfileMenuAction(
                        icon = Icons.Default.Notifications,
                        title = "Test Push Notification Alerts",
                        subtitle = "Send simulated system alerts to verify channels",
                        onClick = { showNotificationTestDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 3: Employment Details
            // -------------------------------------------------------------
            SectionTitle(title = "Employment Details")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileDetailItem(
                        icon = Icons.Default.Person,
                        title = "Employee ID",
                        value = userProfile.employeeId
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileDetailItem(
                        icon = Icons.Default.Business,
                        title = "Department",
                        value = userProfile.department
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileDetailItem(
                        icon = Icons.Default.Work,
                        title = "Position",
                        value = userProfile.position
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 4: Devices & Security
            // -------------------------------------------------------------
            SectionTitle(title = "Devices & Security")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileMenuAction(
                        icon = Icons.Default.Devices,
                        title = "Registered Devices",
                        subtitle = "${deviceSessions.size} active sessions",
                        onClick = { showDevicesDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileMenuAction(
                        icon = Icons.Default.Security,
                        title = "Security & Biometrics",
                        subtitle = "Biometric Lock, TLS 1.3 Keystore",
                        onClick = { showSecurityDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileMenuAction(
                        icon = Icons.Default.Business,
                        title = "Odoo Server Instance",
                        subtitle = if (serverUrl.isNotBlank()) serverUrl.removePrefix("https://").removePrefix("http://") else "Not configured",
                        onClick = { showSwitchServerDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileMenuAction(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Version 1.0.0 (GPS Geofence + Offline Room)",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ErrorRed)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = ErrorRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // -------------------------------------------------------------
    // Dialog: Push Notification Testing
    // -------------------------------------------------------------
    if (showNotificationTestDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationTestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Notification Triggers")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trigger real Android notification channel alerts to test push behaviors:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Button(
                        onClick = {
                            onSendTestNotification("CHECK_IN")
                            showNotificationTestDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Check-In Push")
                    }

                    Button(
                        onClick = {
                            onSendTestNotification("GEOFENCE")
                            showNotificationTestDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Geofence Enter Alert")
                    }

                    Button(
                        onClick = {
                            onSendTestNotification("SYNC")
                            showNotificationTestDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Offline Sync Alert")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationTestDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout from your Odoo account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSwitchServerDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchServerDialog = false },
            title = { Text("Odoo Server Instance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Connected Server:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Do you want to switch or configure a new Odoo domain link?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSwitchServerDialog = false
                        onChangeServer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Change Server", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchServerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GeoFence Attendance App",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enterprise attendance application featuring GPS geofencing, Room SQLite offline persistence, push notifications, and Odoo HR synchronization.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connected Server: $serverUrl",
                        style = MaterialTheme.typography.bodySmall.copy(color = PrimaryBlue)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = PrimaryBlue)
                }
            }
        )
    }

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Security Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Require fingerprint/face to check in",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = biometricsEnabled,
                            onCheckedChange = onToggleBiometrics,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "TLS",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TLS 1.3 Encryption Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Tokens securely stored in Android Keystore",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Done", color = PrimaryBlue)
                }
            }
        )
    }

    if (showDevicesDialog) {
        AlertDialog(
            onDismissRequest = { showDevicesDialog = false },
            title = { Text("Registered Devices") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    deviceSessions.forEach { device ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = device.deviceName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (device.isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(This device)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = PrimaryBlue,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${device.platform} • ${device.lastSeen}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (!device.isCurrent) {
                                    IconButton(onClick = { onRevokeDevice(device.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Revoke",
                                            tint = ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevicesDialog = false }) {
                    Text("Close", color = PrimaryBlue)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun ProfileDetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun ProfileMenuAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

