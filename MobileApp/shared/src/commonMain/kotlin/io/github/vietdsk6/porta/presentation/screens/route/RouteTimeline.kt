package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.route_check_change
import io.github.vietdsk6.porta.generated.resources.route_checks_improved
import io.github.vietdsk6.porta.generated.resources.route_may_close
import io.github.vietdsk6.porta.generated.resources.route_step_dates
import io.github.vietdsk6.porta.generated.resources.route_step_effort_cost
import io.github.vietdsk6.porta.generated.resources.route_would_open
import io.github.vietdsk6.porta.presentation.locale.toText
import io.github.vietdsk6.porta.presentation.theme.PortaColors
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.coroutineContext

@Composable
internal fun RouteTimeline(
    steps: List<RouteStepUiState>,
    modifier: Modifier = Modifier,
) {
    val pathProgress = remember(steps.map(RouteStepUiState::id)) { Animatable(0f) }
    LaunchedEffect(steps.map(RouteStepUiState::id)) {
        if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) {
            pathProgress.snapTo(1f)
        } else {
            pathProgress.animateTo(1f, animationSpec = tween(650))
        }
    }
    Column(
        modifier = modifier.fillMaxWidth().testTag("route-timeline"),
    ) {
        steps.forEachIndexed { index, step ->
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.width(32.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(AppTheme.colors.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", color = AppTheme.colors.onPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                    if (index < steps.lastIndex) {
                        val segmentProgress = (pathProgress.value * steps.lastIndex - index).coerceIn(0f, 1f)
                        val trackColor = AppTheme.colors.outline
                        val activeColor = AppTheme.colors.primary
                        Canvas(modifier = Modifier.width(2.dp).weight(1f)) {
                            drawRect(trackColor)
                            drawRect(activeColor, size = Size(size.width, size.height * segmentProgress))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Card(
                    modifier = Modifier.weight(1f)
                        .padding(bottom = if (index < steps.lastIndex) 12.dp else 0.dp)
                        .testTag("route-step-$index"),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            stringResource(
                                Res.string.route_step_dates,
                                step.startDateLabel,
                                step.endDateLabel,
                                routeDuration(step.durationDays),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppTheme.colors.text.secondary,
                        )
                        Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(
                                Res.string.route_step_effort_cost,
                                routeEffortLabel(step.effort),
                                routeCostLabel(step.cost),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = PortaColors.cost(step.cost),
                        )
                        StepDoors(step)
                    }
                }
            }
        }
    }
}

@Composable
internal fun StepDoors(step: RouteStepUiState) {
    if (step.doorsOpened.isNotEmpty()) {
        Text(stringResource(Res.string.route_would_open), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.status.success)
        step.doorsOpened.forEach { door -> Text("• $door", style = MaterialTheme.typography.bodySmall) }
    }
    val onlyImproved = step.doorsImproved.filterNot(step.doorsOpened::contains)
    if (onlyImproved.isNotEmpty()) {
        Text(stringResource(Res.string.route_checks_improved), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.text.secondary)
        onlyImproved.forEach { door ->
            Text(
                text = step.targetFactKey?.let { key -> "• " + stringResource(Res.string.route_check_change, door, key.toText()) }
                    ?: "• $door",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    if (step.doorsLost.isNotEmpty()) {
        Text(stringResource(Res.string.route_may_close), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.status.error)
        step.doorsLost.forEach { door -> Text("• $door", style = MaterialTheme.typography.bodySmall) }
    }
}
