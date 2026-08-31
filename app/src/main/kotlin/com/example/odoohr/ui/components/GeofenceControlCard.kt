package com.example.odoohr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.data.model.GeofenceLocationPreset
import com.example.odoohr.data.model.GeofenceZone
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeofenceControlCard(
    geofenceZone: GeofenceZone,
    onRefreshLocation: () -> Unit,
    onSelectPreset: (GeofenceLocationPreset) -> Unit = {},
    onSelectSimulation: (GeofenceCalculator.MockLocationPoint) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showTestingControls by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geofence_control_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
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
                            .background(
                                if (geofenceZone.isInside) SuccessGreenLight else WarningOrangeLight,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (geofenceZone.isInside) Icons.Default.GpsFixed else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (geofenceZone.isInside) SuccessGreen else WarningOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Geofence Perimeter Logic",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = geofenceZone.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier.testTag("geofence_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh GPS",
                        tint = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Radar Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                RadarCanvas(
                    isInside = geofenceZone.isInside,
                    distanceMeters = geofenceZone.distanceMeters,
                    radiusMeters = geofenceZone.radiusMeters,
                    pulseFraction = pulseRadiusFraction,
                    pulseAlpha = pulseAlpha
                )

                // Status Overlay Badge inside Radar
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (geofenceZone.isInside) Color(0xCC10B981) else Color(0xCCF59E0B),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            text = if (geofenceZone.isInside) "INSIDE GEOFENCE" else "OUTSIDE BOUNDARY",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Coordinates chip at bottom of radar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x99000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "GPS: ${String.format(java.util.Locale.US, "%.4f", geofenceZone.userLatitude)}, ${String.format(java.util.Locale.US, "%.4f", geofenceZone.userLongitude)} (±${geofenceZone.accuracyMeters.toInt()}m)",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Row (Distance, Allowed Radius, Verification)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GeofenceMetricBadge(
                    label = "Beacon Distance",
                    value = GeofenceCalculator.formatDistance(geofenceZone.distanceMeters),
                    isPositive = geofenceZone.isInside,
                    modifier = Modifier.weight(1f)
                )

                GeofenceMetricBadge(
                    label = "Max Radius",
                    value = "${geofenceZone.radiusMeters.toInt()}m Limit",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )

                GeofenceMetricBadge(
                    label = "Punch Status",
                    value = if (geofenceZone.isInside) "Allowed" else "Warning",
                    isPositive = geofenceZone.isInside,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle for Geofence Presets & Testing Simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Office Locations & Testing Controls",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                )

                TextButton(
                    onClick = { showTestingControls = !showTestingControls },
                    modifier = Modifier.testTag("geofence_toggle_controls_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showTestingControls) "Hide" else "Customize / Test",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(visible = showTestingControls) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "SELECT OFFICE PRESET:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(GeofenceCalculator.DEFAULT_OFFICE_PRESETS) { preset ->
                            val isSelected = geofenceZone.name == preset.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectPreset(preset) },
                                label = { Text(preset.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlueContainer,
                                    selectedLabelColor = PrimaryBlueDark
                                ),
                                modifier = Modifier.testTag("preset_chip_${preset.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TEST SIMULATION SCENARIO:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(GeofenceCalculator.SIMULATION_SCENARIOS) { scenario ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (scenario.isInsideExpected) SuccessGreenLight else WarningOrangeLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (scenario.isInsideExpected) SuccessGreen.copy(alpha = 0.3f) else WarningOrange.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .clickable { onSelectSimulation(scenario) }
                                    .testTag("simulation_${scenario.name.replace(" ", "_")}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = scenario.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scenario.isInsideExpected) SuccessGreen else WarningOrange
                                    )
                                    Text(
                                        text = scenario.description,
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarCanvas(
    isInside: Boolean,
    distanceMeters: Double,
    radiusMeters: Double,
    pulseFraction: Float,
    pulseAlpha: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val maxCanvasRadius = (size.height / 2f) - 16.dp.toPx()

        // Concentric radar grid rings
        listOf(0.33f, 0.66f, 1.0f).forEach { fraction ->
            drawCircle(
                color = Color(0xFF334155),
                radius = maxCanvasRadius * fraction,
                center = centerOffset,
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
        }

        // Crosshairs
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(centerOffset.x, 10.dp.toPx()),
            end = Offset(centerOffset.x, size.height - 10.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(20.dp.toPx(), centerOffset.y),
            end = Offset(size.width - 20.dp.toPx(), centerOffset.y),
            strokeWidth = 1.dp.toPx()
        )

        // Pulsing radar sweep ring
        drawCircle(
            color = if (isInside) SuccessGreen.copy(alpha = pulseAlpha) else WarningOrange.copy(alpha = pulseAlpha),
            radius = maxCanvasRadius * pulseFraction,
            center = centerOffset,
            style = Stroke(width = 2.dp.toPx())
        )

        // Geofence Boundary Circle
        val boundaryRadius = maxCanvasRadius * 0.70f
        drawCircle(
            color = if (isInside) SuccessGreen.copy(alpha = 0.4f) else WarningOrange.copy(alpha = 0.4f),
            radius = boundaryRadius,
            center = centerOffset,
            style = Stroke(width = 2.dp.toPx())
        )

        // Office Beacon (Center Point)
        drawCircle(
            color = PrimaryBlueLight,
            radius = 6.dp.toPx(),
            center = centerOffset
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = centerOffset
        )

        // User Position Marker
        // Calculate proportional offset based on distance / radius
        val normalizedDistance = (distanceMeters / radiusMeters).toFloat().coerceIn(0.1f, 1.35f)
        val userRadiusOnCanvas = boundaryRadius * normalizedDistance
        val angleRad = Math.toRadians(45.0) // 45 degree angle for clear visualization

        val userX = centerOffset.x + (userRadiusOnCanvas * cos(angleRad)).toFloat()
        val userY = centerOffset.y - (userRadiusOnCanvas * sin(angleRad)).toFloat()
        val userOffset = Offset(userX, userY)

        // User marker glow
        drawCircle(
            color = if (isInside) SuccessGreen.copy(alpha = 0.35f) else WarningOrange.copy(alpha = 0.35f),
            radius = 12.dp.toPx(),
            center = userOffset
        )
        // User marker core
        drawCircle(
            color = if (isInside) SuccessGreen else WarningOrange,
            radius = 6.dp.toPx(),
            center = userOffset
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = userOffset
        )
    }
}

@Composable
fun GeofenceMetricBadge(
    label: String,
    value: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) PrimaryBlueDark else WarningOrange
            )
        }
    }
}
