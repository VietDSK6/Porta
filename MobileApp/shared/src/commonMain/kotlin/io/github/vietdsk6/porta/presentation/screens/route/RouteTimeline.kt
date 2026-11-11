package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
internal fun RouteTimeline(
    steps: List<RouteStepUiState>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().testTag("route-timeline"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(32.dp).background(AppTheme.colors.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", color = AppTheme.colors.onPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                    if (index < steps.lastIndex) {
                        Spacer(
                            modifier = Modifier.width(2.dp).height(142.dp)
                                .background(AppTheme.colors.outline),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Card(modifier = Modifier.weight(1f).testTag("route-step-$index")) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "${step.startDateLabel} → ${step.endDateLabel} · ${step.durationDays} days",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppTheme.colors.text.secondary,
                        )
                        Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${step.effort.name.lowercase().replace('_', ' ')} effort · ${step.cost.name.lowercase()} cost",
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

@Composable
internal fun StepDoors(step: RouteStepUiState) {
    if (step.doorsOpened.isNotEmpty()) {
        Text("Would open", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.status.success)
        step.doorsOpened.forEach { door -> Text("• $door", style = MaterialTheme.typography.bodySmall) }
    }
    val onlyImproved = step.doorsImproved.filterNot(step.doorsOpened::contains)
    if (onlyImproved.isNotEmpty()) {
        Text("Checks improved", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.text.secondary)
        onlyImproved.forEach { door -> Text("• $door", style = MaterialTheme.typography.bodySmall) }
    }
    if (step.doorsLost.isNotEmpty()) {
        Text("May close before this step ends", style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.status.error)
        step.doorsLost.forEach { door -> Text("• $door", style = MaterialTheme.typography.bodySmall) }
    }
}
