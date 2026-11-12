package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_adjust
import io.github.vietdsk6.porta.generated.resources.frontier_ceiling_any
import io.github.vietdsk6.porta.generated.resources.frontier_cost_description
import io.github.vietdsk6.porta.generated.resources.frontier_cost_free
import io.github.vietdsk6.porta.generated.resources.frontier_cost_high
import io.github.vietdsk6.porta.generated.resources.frontier_cost_low
import io.github.vietdsk6.porta.generated.resources.frontier_cost_medium
import io.github.vietdsk6.porta.generated.resources.frontier_days
import io.github.vietdsk6.porta.generated.resources.frontier_done
import io.github.vietdsk6.porta.generated.resources.frontier_effort_description
import io.github.vietdsk6.porta.generated.resources.frontier_effort_high
import io.github.vietdsk6.porta.generated.resources.frontier_effort_low
import io.github.vietdsk6.porta.generated.resources.frontier_effort_medium
import io.github.vietdsk6.porta.generated.resources.frontier_effort_very_low
import io.github.vietdsk6.porta.generated.resources.frontier_horizon_description
import io.github.vietdsk6.porta.generated.resources.frontier_limits_adjust
import io.github.vietdsk6.porta.generated.resources.frontier_limits_collapse
import io.github.vietdsk6.porta.generated.resources.frontier_limits_summary
import io.github.vietdsk6.porta.generated.resources.frontier_max_cost
import io.github.vietdsk6.porta.generated.resources.frontier_max_effort
import io.github.vietdsk6.porta.generated.resources.frontier_one_year
import io.github.vietdsk6.porta.generated.resources.frontier_prioritize_saved
import io.github.vietdsk6.porta.generated.resources.frontier_saved_priority_help
import io.github.vietdsk6.porta.generated.resources.frontier_time_available
import io.github.vietdsk6.porta.generated.resources.frontier_your_limits
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FrontierControls(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    collapsible: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(!collapsible) }
    Card(
        modifier = modifier.fillMaxWidth().testTag("frontier-planning-limits"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (collapsible) {
                            Modifier.clickable(
                                role = Role.Button,
                                onClickLabel = if (expanded) stringResource(Res.string.frontier_limits_collapse) else stringResource(Res.string.frontier_limits_adjust),
                            ) { expanded = !expanded }
                        } else {
                            Modifier
                        },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(Res.string.frontier_your_limits), style = AppTheme.typography.h6)
                    Text(
                        text = stringResource(Res.string.frontier_limits_summary, uiState.horizonDays, uiState.maxEffort.ceilingLabel(), uiState.maxCost.ceilingLabel()),
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                if (collapsible) {
                    Text(
                        text = if (expanded) stringResource(Res.string.frontier_done) else stringResource(Res.string.frontier_adjust),
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (expanded) {
                ConstraintLabel(
                    title = stringResource(Res.string.frontier_time_available),
                    description = stringResource(Res.string.frontier_horizon_description),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ALLOWED_HORIZONS.forEach { days ->
                        FilterChip(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("frontier-horizon-$days"),
                            selected = uiState.horizonDays == days,
                            onClick = { onUiEvent(FrontierUiEvent.HorizonChanged(days)) },
                            label = { Text(if (days == 365) stringResource(Res.string.frontier_one_year) else pluralStringResource(Res.plurals.frontier_days, days, days)) },
                        )
                    }
                }

                ConstraintLabel(
                    title = stringResource(Res.string.frontier_max_effort),
                    description = stringResource(Res.string.frontier_effort_description),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    EffortLevel.entries.forEach { effort ->
                        FilterChip(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("frontier-effort-${effort.name}"),
                            selected = uiState.maxEffort == effort,
                            onClick = { onUiEvent(FrontierUiEvent.MaxEffortChanged(effort)) },
                            label = { Text(effort.ceilingLabel()) },
                        )
                    }
                }

                ConstraintLabel(
                    title = stringResource(Res.string.frontier_max_cost),
                    description = stringResource(Res.string.frontier_cost_description),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CostBand.entries.forEach { cost ->
                        FilterChip(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("frontier-cost-${cost.name}"),
                            selected = uiState.maxCost == cost,
                            onClick = { onUiEvent(FrontierUiEvent.MaxCostChanged(cost)) },
                            label = { Text(cost.ceilingLabel()) },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("frontier-prioritize-saved")
                        .toggleable(
                            value = uiState.prioritizeSaved,
                            role = Role.Switch,
                            onValueChange = {
                                onUiEvent(FrontierUiEvent.PrioritizeSavedChanged(it))
                            },
                        )
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(Res.string.frontier_prioritize_saved), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = stringResource(Res.string.frontier_saved_priority_help),
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                    Switch(checked = uiState.prioritizeSaved, onCheckedChange = null)
                }
            }
        }
    }
}

@Composable
private fun ConstraintLabel(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, style = AppTheme.typography.bodyMedium)
        Text(
            text = description,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun EffortLevel.ceilingLabel(): String = stringResource(
    when (this) {
        EffortLevel.VERY_LOW -> Res.string.frontier_effort_very_low
        EffortLevel.LOW -> Res.string.frontier_effort_low
        EffortLevel.MEDIUM -> Res.string.frontier_effort_medium
        EffortLevel.HIGH -> Res.string.frontier_effort_high
        EffortLevel.VERY_HIGH -> Res.string.frontier_ceiling_any
    },
)

@Composable
private fun CostBand.ceilingLabel(): String = stringResource(
    when (this) {
        CostBand.FREE -> Res.string.frontier_cost_free
        CostBand.LOW -> Res.string.frontier_cost_low
        CostBand.MEDIUM -> Res.string.frontier_cost_medium
        CostBand.HIGH -> Res.string.frontier_cost_high
        CostBand.UNKNOWN -> Res.string.frontier_ceiling_any
    },
)

private val ALLOWED_HORIZONS = listOf(30, 90, 180, 365)
