package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

private val stationWidth = 260.dp

@Composable
internal fun RouteMetroMap(
    route: RouteOptionUiState,
    modifier: Modifier = Modifier,
) {
    val trackColor = AppTheme.colors.primary
    val mutedColor = AppTheme.colors.outline
    val lostColor = AppTheme.colors.status.error
    val progress = remember(route.id) { Animatable(0f) }
    LaunchedEffect(route.id) { progress.animateTo(1f, animationSpec = tween(650)) }

    Box(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .testTag("route-metro-map"),
    ) {
        Box(modifier = Modifier.width(stationWidth * route.steps.size.coerceAtLeast(1))) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stationPx = stationWidth.toPx()
                val trackY = 28.dp.toPx()
                val branchEndY = 72.dp.toPx()
                val trackEndX = stationPx * route.steps.size * progress.value
                drawLine(
                    color = mutedColor,
                    start = Offset(0f, trackY),
                    end = Offset(stationPx * route.steps.size, trackY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = trackColor,
                    start = Offset(0f, trackY),
                    end = Offset(trackEndX, trackY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                route.steps.forEachIndexed { index, step ->
                    val x = stationPx * (index + 0.5f)
                    val lit = progress.value * route.steps.size >= index + 0.5f
                    drawLine(
                        color = if (lit) trackColor else mutedColor,
                        start = Offset(x, trackY),
                        end = Offset(x, branchEndY),
                        strokeWidth = 2.dp.toPx(),
                    )
                    drawCircle(
                        color = if (lit) trackColor else mutedColor,
                        radius = 11.dp.toPx(),
                        center = Offset(x, trackY),
                    )
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(x, trackY),
                    )
                    if (step.doorsLost.isNotEmpty()) {
                        drawLine(
                            color = lostColor,
                            start = Offset(x + 16.dp.toPx(), trackY + 3.dp.toPx()),
                            end = Offset(x + 16.dp.toPx(), branchEndY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx())),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 76.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                route.steps.forEachIndexed { index, step ->
                    Column(modifier = Modifier.width(stationWidth).padding(horizontal = 10.dp)) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().testTag("route-station-$index"),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "STATION ${index + 1} · ${step.endDateLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = trackColor,
                                )
                                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${step.durationDays} days · ${step.cost.name.lowercase()} cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.text.secondary,
                                )
                                StepDoors(step)
                            }
                        }
                    }
                }
            }
        }
    }
}
