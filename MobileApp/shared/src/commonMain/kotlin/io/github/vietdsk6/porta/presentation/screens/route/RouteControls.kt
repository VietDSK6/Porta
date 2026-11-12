package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import io.github.vietdsk6.porta.generated.resources.route_controls_title
import io.github.vietdsk6.porta.generated.resources.route_horizon
import io.github.vietdsk6.porta.generated.resources.route_max_effort
import io.github.vietdsk6.porta.generated.resources.route_total_budget
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
            Text(
                stringResource(Res.string.route_horizon, routeDuration(uiState.horizonDays)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = uiState.horizonDays.toFloat(),
                onValueChange = { onUiEvent(RouteUiEvent.HorizonChanged(it.roundToInt())) },
                valueRange = 30f..365f,
                modifier = Modifier.testTag("route-horizon-slider"),
            )
            Text(stringResource(Res.string.route_total_budget), style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CostBand.entries.forEach { cost ->
                    FilterChip(
                        selected = uiState.maxTotalCost == cost,
                        onClick = { onUiEvent(RouteUiEvent.MaxCostChanged(cost)) },
                        label = { Text(routeCostLabel(cost)) },
                        modifier = Modifier.testTag("route-cost-${cost.name.lowercase()}"),
                    )
                }
            }
            Text(stringResource(Res.string.route_max_effort), style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EffortLevel.entries.forEach { effort ->
                    FilterChip(
                        selected = uiState.maxEffort == effort,
                        onClick = { onUiEvent(RouteUiEvent.MaxEffortChanged(effort)) },
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
