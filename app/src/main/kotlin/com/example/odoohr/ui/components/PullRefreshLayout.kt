package com.example.odoohr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odoohr.ui.theme.PrimaryBlue
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modern Jetpack Compose Material 3 Pull-To-Refresh Layout wrapper.
 * Provides intuitive gesture dragging, release-to-refresh badge, and smooth spin animations.
 */
@Composable
fun PullRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshThresholdDp: Float = 85f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { refreshThresholdDp.dp.toPx() }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var isTriggered by remember { mutableStateOf(false) }

    // Reset offset when refreshing finishes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            dragOffsetPx = 0f
            isTriggered = false
        }
    }

    val animatedOffsetPx by animateFloatAsState(
        targetValue = if (isRefreshing) thresholdPx * 0.75f else dragOffsetPx,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "pull_offset"
    )

    val progress = min(1f, dragOffsetPx / thresholdPx)
    val rotationDegrees = progress * 180f

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (!isRefreshing) {
                        val newOffset = (dragOffsetPx + delta * 0.55f).coerceAtLeast(0f)
                        dragOffsetPx = min(newOffset, thresholdPx * 1.5f)
                        isTriggered = dragOffsetPx >= thresholdPx
                    }
                },
                onDragStopped = {
                    if (isTriggered && !isRefreshing) {
                        onRefresh()
                    } else {
                        dragOffsetPx = 0f
                        isTriggered = false
                    }
                }
            )
            .testTag("pull_to_refresh_container")
    ) {
        // Main Screen Content
        content()

        // Floating Refresh Badge Indicator
        if (animatedOffsetPx > 6f || isRefreshing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, animatedOffsetPx.roundToInt()) }
                    .padding(top = 8.dp)
                    .testTag("pull_refresh_indicator_badge")
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Syncing with Odoo...",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        } else {
                            Icon(
                                imageVector = if (isTriggered) Icons.Default.Refresh else Icons.Default.ArrowDownward,
                                contentDescription = "Pull to refresh",
                                tint = if (isTriggered) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (isTriggered) 180f else rotationDegrees)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTriggered) "Release to refresh" else "Pull down to sync",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isTriggered) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
