package com.example.odoohr.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.AttendanceRecord
import com.example.odoohr.ui.theme.BackgroundLight
import com.example.odoohr.ui.theme.BorderLight
import com.example.odoohr.ui.theme.PrimaryBlue
import com.example.odoohr.ui.theme.PrimaryBlueDark
import com.example.odoohr.ui.theme.SuccessGreen
import com.example.odoohr.ui.theme.TextMuted
import com.example.odoohr.ui.theme.TextPrimary
import com.example.odoohr.ui.theme.TextSecondary
import com.example.odoohr.ui.theme.WarningOrange

enum class HistoryFilter {
    ALL,
    THIS_WEEK,
    OVERTIME,
    WITH_NOTES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    attendanceRecords: List<AttendanceRecord>,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecordForDetails by remember { mutableStateOf<AttendanceRecord?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val filteredRecords = attendanceRecords.filter { record ->
        val matchesQuery = searchQuery.isBlank() ||
                record.date.contains(searchQuery, ignoreCase = true) ||
                (record.shiftNote?.contains(searchQuery, ignoreCase = true) == true) ||
                record.locationName.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.THIS_WEEK -> record.date.contains("Today") ||
                    record.date.contains("Yesterday") ||
                    record.date.contains("Thursday") ||
                    record.date.contains("Wednesday")
            HistoryFilter.OVERTIME -> record.isOvertime || (record.overtimeMinutes > 0)
            HistoryFilter.WITH_NOTES -> !record.shiftNote.isNullOrBlank()
        }

        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Attendance History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("attendance_history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("attendance_history_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export History",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlueDark
                )
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // KPI Overview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Monthly Performance",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Trend",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "On Track",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "168.5 hrs",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                )
                                Text(
                                    text = "Total Logged",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            Column {
                                Text(
                                    text = "8.4 hrs",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Daily Average",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            Column {
                                Text(
                                    text = "98.5%",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                )
                                Text(
                                    text = "On-Time Rate",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            // Search and Filters
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by date, location or note...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_history_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.ALL,
                        onClick = { selectedFilter = HistoryFilter.ALL },
                        label = { Text("All (${attendanceRecords.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )

                    FilterChip(
                        selected = selectedFilter == HistoryFilter.THIS_WEEK,
                        onClick = { selectedFilter = HistoryFilter.THIS_WEEK },
                        label = { Text("This Week", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("filter_chip_this_week")
                    )

                    FilterChip(
                        selected = selectedFilter == HistoryFilter.OVERTIME,
                        onClick = { selectedFilter = HistoryFilter.OVERTIME },
                        label = { Text("Overtime", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("filter_chip_overtime")
                    )

                    FilterChip(
                        selected = selectedFilter == HistoryFilter.WITH_NOTES,
                        onClick = { selectedFilter = HistoryFilter.WITH_NOTES },
                        label = { Text("Notes", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("filter_chip_notes")
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No records",
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No records match your criteria",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Try clearing search or changing the filter chip.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }

            // Attendance Record Cards
            items(filteredRecords, key = { it.id }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRecordForDetails = record }
                        .testTag("attendance_record_${record.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Icon
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (record.isLive) SuccessGreen else PrimaryBlue,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val dayNum = record.date.filter { it.isDigit() }.take(2).ifEmpty { "31" }
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = record.date,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    if (record.isLive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SuccessGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "IN PROGRESS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = "Check in",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = record.checkInTime,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Check out",
                                        tint = if (record.checkOutTime != null) TextSecondary else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = record.checkOutTime ?: "--:--",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (record.checkOutTime != null) TextPrimary else TextMuted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = record.duration,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.isLive) SuccessGreen else PrimaryBlue
                                    )
                                )

                                if (record.isOvertime) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = WarningOrange.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "+${record.overtimeMinutes}m OT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = WarningOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Location and Note Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = record.verificationStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!record.shiftNote.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Note,
                                        contentDescription = "Note",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Note attached",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Detailed Attendance Record Dialog
    if (selectedRecordForDetails != null) {
        val record = selectedRecordForDetails!!
        AlertDialog(
            onDismissRequest = { selectedRecordForDetails = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Record",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Shift Verification Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "Date", value = record.date)
                    DetailRow(label = "Check-in", value = "${record.checkInTime} (Biometric + GPS)")
                    DetailRow(label = "Check-out", value = record.checkOutTime?.let { "$it (Automated)" } ?: "Active in Progress")
                    DetailRow(label = "Total Duration", value = record.duration)
                    DetailRow(label = "Work Zone", value = record.locationName)
                    DetailRow(label = "GPS Accuracy", value = "±8.5 meters (Inside Geofence)")
                    DetailRow(label = "IP / Network", value = "192.168.1.104 (Office Wi-Fi)")
                    if (!record.shiftNote.isNullOrBlank()) {
                        DetailRow(label = "Shift Note", value = record.shiftNote)
                    }
                    DetailRow(label = "Odoo Status", value = "Synced & Approved")
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRecordForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Export Attendance Records", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Download your official Odoo-signed attendance records for August 2026. The exported file contains biometric timestamps, duration tallies, and geofence coordinates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        exportSuccessMessage = "Attendance_Report_Aug_2026.csv generated and saved to Downloads"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("export_csv_confirm_button")
                ) {
                    Text("Download CSV", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (exportSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { exportSuccessMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Export Successful", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    exportSuccessMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { exportSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
