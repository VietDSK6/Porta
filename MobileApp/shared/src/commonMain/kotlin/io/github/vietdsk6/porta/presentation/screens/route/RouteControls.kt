package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.route_budget_note
import io.github.vietdsk6.porta.generated.resources.route_controls_help
import io.github.vietdsk6.porta.generated.resources.route_controls_title
import io.github.vietdsk6.porta.generated.resources.route_horizon
import io.github.vietdsk6.porta.generated.resources.route_max_cost_per_step
import io.github.vietdsk6.porta.generated.resources.route_max_effort
import io.github.vietdsk6.porta.generated.resources.route_recalculating
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun RouteControls(
    uiState: RouteUiState,
    onUiEvent: (RouteUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().testTag("route-controls")) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.route_controls_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.route_controls_help), style = MaterialTheme.typography.bodySmall)
            if (uiState.isRecalculating) {
                Text(stringResource(Res.string.route_recalculating), style = MaterialTheme.typography.labelMedium)
            }
            Text(
                stringResource(Res.string.route_horizon, routeDuration(uiState.horizonDays)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = uiState.horizonDays.toFloat(),
                onValueChange = { onUiEvent(RouteUiEvent.HorizonChanged(it.roundToInt())) },
                valueRange = 30f..365f,
                enabled = !uiState.isSaving,
                modifier = Modifier.testTag("route-horizon-slider"),
            )
            Text(stringResource(Res.string.route_max_cost_per_step), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CostBand.entries.forEach { cost ->
                    FilterChip(
                        selected = uiState.maxCostPerStep == cost,
                        onClick = { onUiEvent(RouteUiEvent.MaxCostChanged(cost)) },
                        enabled = !uiState.isSaving,
                        label = { Text(routeCostLabel(cost)) },
                        modifier = Modifier.testTag("route-cost-${cost.name.lowercase()}"),
                    )
                }
            }
            Text(stringResource(Res.string.route_max_effort), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                EffortLevel.entries.forEach { effort ->
                    FilterChip(
                        selected = uiState.maxEffort == effort,
                        onClick = { onUiEvent(RouteUiEvent.MaxEffortChanged(effort)) },
                        enabled = !uiState.isSaving,
                        label = { Text(routeEffortLabel(effort)) },
                        modifier = Modifier.testTag("route-effort-${effort.name.lowercase()}"),
                    )
                }
            }
            Text(
                stringResource(Res.string.route_budget_note),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
