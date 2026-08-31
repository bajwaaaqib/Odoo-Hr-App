package com.example.odoohr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.OfflinePunchRecord
import com.example.odoohr.data.model.OdooConnectionState
import com.example.odoohr.data.model.SyncStatus
import com.example.odoohr.ui.theme.BorderLight
import com.example.odoohr.ui.theme.ErrorRed
import com.example.odoohr.ui.theme.ErrorRedLight
import com.example.odoohr.ui.theme.InfoBlue
import com.example.odoohr.ui.theme.PrimaryBlue
import com.example.odoohr.ui.theme.PrimaryBlueDark
import com.example.odoohr.ui.theme.SuccessGreen
import com.example.odoohr.ui.theme.SuccessGreenLight
import com.example.odoohr.ui.theme.TextMuted
import com.example.odoohr.ui.theme.TextPrimary
import com.example.odoohr.ui.theme.TextSecondary
import com.example.odoohr.ui.theme.WarningOrange
import com.example.odoohr.ui.theme.WarningOrangeLight

@Composable
fun OfflineSyncBanner(
    connectionState: OdooConnectionState,
    pendingPunches: List<OfflinePunchRecord>,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQueueDetailsDialog by remember { mutableStateOf(false) }

    val hasPending = pendingPunches.isNotEmpty()
    val isOfflineMode = connectionState == OdooConnectionState.OFFLINE_CACHE || connectionState == OdooConnectionState.ERROR

    if (hasPending || isOfflineMode) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable { showQueueDetailsDialog = true }
                .testTag("offline_sync_banner"),
            shape = RoundedCornerShape(14.dp),
            color = if (hasPending) WarningOrangeLight else Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hasPending) WarningOrange.copy(alpha = 0.4f) else PrimaryBlue.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (hasPending) WarningOrange else PrimaryBlue,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (hasPending) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (hasPending) {
                                "${pendingPunches.size} Offline ${if (pendingPunches.size == 1) "Punch" else "Punches"} Queued"
                            } else {
                                "Offline Cache Active"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasPending) WarningOrange else PrimaryBlueDark
                            )
                        )
                        Text(
                            text = if (hasPending) {
                                "Stored locally with GPS coords. Tap to inspect."
                            } else {
                                "Working with cached attendance records."
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (hasPending) {
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("offline_sync_now_button")
                    ) {
                        Text(
                            text = if (isSyncing) "Syncing..." else "Sync Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Inspection Dialog for Queued Punches
    if (showQueueDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showQueueDetailsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Offline Punch Queue & Cache",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "When offline, punches are timestamped, encrypted, and saved locally. They automatically push to Odoo when reconnected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pendingPunches.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SuccessGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = SuccessGreen
                                )
                                Text(
                                    text = "All punches are fully synchronized with Odoo ERP!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(pendingPunches) { punch ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${punch.type.replace("_", " ")} (${punch.timeFormatted})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = PrimaryBlueDark
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = WarningOrangeLight
                                            ) {
                                                Text(
                                                    text = punch.syncStatus.name,
                                                    color = WarningOrange,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Zone: ${punch.zoneName} • Dist: ${punch.distanceMeters.toInt()}m",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "GPS: ${String.format(java.util.Locale.US, "%.4f", punch.latitude)}, ${String.format(java.util.Locale.US, "%.4f", punch.longitude)}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (pendingPunches.isNotEmpty()) {
                    Button(
                        onClick = {
                            onSyncNow()
                            showQueueDetailsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Sync All Now", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { showQueueDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (pendingPunches.isNotEmpty()) {
                    TextButton(onClick = { showQueueDetailsDialog = false }) {
                        Text("Dismiss")
                    }
                }
            }
        )
    }
}
