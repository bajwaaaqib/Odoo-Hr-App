package com.example.odoohr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.AttendanceChartSummary
import com.example.odoohr.data.model.DailyAttendanceChartItem
import com.example.odoohr.ui.theme.BackgroundLight
import com.example.odoohr.ui.theme.BorderLight
import com.example.odoohr.ui.theme.ErrorRed
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

@Composable
fun AttendanceDataChart(
    dailyItems: List<DailyAttendanceChartItem>,
    summary: AttendanceChartSummary = AttendanceChartSummary(),
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(dailyItems.indexOfFirst { it.isToday }.coerceAtLeast(0)) }
    var chartViewMode by remember { mutableStateOf(0) } // 0: Weekly Velocity, 1: Monthly Trend

    val selectedItem = dailyItems.getOrNull(selectedIndex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_data_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title and View Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryBlueContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Attendance Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Weekly Hours & Target Velocity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Metric Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SuccessGreenLight
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${summary.weeklyTotalHours}h / ${summary.weeklyTargetHours}h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bar Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Background Target Guideline for 8 Hours
                TargetGuideline(targetHours = 8.0f, maxHours = 10.5f)

                // Bar Columns
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyItems.forEachIndexed { index, item ->
                        BarColumnItem(
                            item = item,
                            isSelected = selectedIndex == index,
                            maxHours = 10.5f,
                            onClick = { selectedIndex = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Inspection Tooltip Card
            AnimatedVisibility(
                visible = selectedItem != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedItem?.let { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chart_selected_day_tooltip")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.fullDate,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    if (item.isToday) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryBlue
                                        ) {
                                            Text(
                                                text = "TODAY",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${item.totalHours} hrs",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.overtimeHours > 0) WarningOrange else PrimaryBlue
                                        )
                                    )
                                    if (item.overtimeHours > 0) {
                                        Text(
                                            text = "(+${item.overtimeHours}h OT)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = WarningOrange,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (item.checkInTime != null) {
                                            "${item.checkInTime} → ${item.checkOutTime ?: "In Progress"}"
                                        } else {
                                            "Weekend / Off-day"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = item.locationName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(
                    title = "Weekly Total",
                    value = "${summary.weeklyTotalHours} hrs",
                    subtitle = "${((summary.weeklyTotalHours / summary.weeklyTargetHours) * 100).toInt()}% Target",
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsKpiCard(
                    title = "Overtime Logged",
                    value = "+${summary.weeklyOvertimeHours} hrs",
                    subtitle = "Approved OT",
                    color = WarningOrange,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsKpiCard(
                    title = "On-Time Rate",
                    value = "${summary.onTimePercentage}%",
                    subtitle = "14 Day Streak",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BarColumnItem(
    item: DailyAttendanceChartItem,
    isSelected: Boolean,
    maxHours: Float,
    onClick: () -> Unit
) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = (item.totalHours / maxHours).coerceIn(0.05f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "barHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("chart_bar_${item.dayLabel.lowercase()}")
    ) {
        // Value above bar
        Text(
            text = if (item.totalHours > 0) "${item.totalHours}h" else "-",
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryBlueDark else TextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bar container
        Box(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight(0.72f),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background bar track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            )

            // Active bar fill with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeightFraction)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(
                        brush = when {
                            item.isWeekend -> Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                            item.overtimeHours > 0 -> Brush.verticalGradient(listOf(WarningOrange, PrimaryBlue))
                            isSelected -> Brush.verticalGradient(listOf(PrimaryBlueLight, PrimaryBlueDark))
                            else -> Brush.verticalGradient(listOf(PrimaryBlue, PrimaryBlueDark))
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) PrimaryBlueDark else Color.Transparent,
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day Label
        Text(
            text = item.dayLabel,
            fontSize = 11.sp,
            fontWeight = if (isSelected || item.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected || item.isToday) PrimaryBlue else TextSecondary
        )
    }
}

@Composable
fun TargetGuideline(targetHours: Float, maxHours: Float) {
    val targetFraction = 1f - (targetHours / maxHours).coerceIn(0.1f, 0.9f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val yPos = size.height * 0.72f * targetFraction + 16.dp.toPx()

        drawLine(
            color = WarningOrange.copy(alpha = 0.6f),
            start = Offset(0f, yPos),
            end = Offset(size.width, yPos),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
    }
}

@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 9.sp
                )
            )
        }
    }
}
