package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
                                onClickLabel = if (expanded) "Collapse planning limits" else "Adjust planning limits",
                            ) { expanded = !expanded }
                        } else {
                            Modifier
                        },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Planning limits", style = AppTheme.typography.h6)
                    Text(
                        text = "${uiState.horizonDays} days · ${uiState.maxEffort.ceilingLabel()} effort · " +
                            "${uiState.maxCost.ceilingLabel()} cost",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                if (collapsible) {
                    Text(
                        text = if (expanded) "Done" else "Adjust",
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (expanded) {
                ConstraintLabel(
                    title = "Time available",
                    description = "Milestones longer than this stay visible below with a reason.",
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ALLOWED_HORIZONS) { days ->
                        FilterChip(
                            modifier = Modifier.testTag("frontier-horizon-$days"),
                            selected = uiState.horizonDays == days,
                            onClick = { onUiEvent(FrontierUiEvent.HorizonChanged(days)) },
                            label = { Text(if (days == 365) "1 year" else "$days days") },
                        )
                    }
                }

                ConstraintLabel(
                    title = "Maximum effort",
                    description = "The most demanding milestone you can realistically take on.",
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EffortLevel.entries) { effort ->
                        FilterChip(
                            modifier = Modifier.testTag("frontier-effort-${effort.name}"),
                            selected = uiState.maxEffort == effort,
                            onClick = { onUiEvent(FrontierUiEvent.MaxEffortChanged(effort)) },
                            label = { Text(effort.ceilingLabel()) },
                        )
                    }
                }

                ConstraintLabel(
                    title = "Maximum cost",
                    description = "Choose Any to keep actions whose price still needs verification.",
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CostBand.entries) { cost ->
                        FilterChip(
                            modifier = Modifier.testTag("frontier-cost-${cost.name}"),
                            selected = uiState.maxCost == cost,
                            onClick = { onUiEvent(FrontierUiEvent.MaxCostChanged(cost)) },
                            label = { Text(cost.ceilingLabel()) },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        Text("Prioritize saved doors", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Keep milestones for saved doors visible and rank them first.",
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

private fun EffortLevel.ceilingLabel(): String = if (this == EffortLevel.VERY_HIGH) {
    "Any"
} else {
    name.readableEnumName()
}

private fun CostBand.ceilingLabel(): String = if (this == CostBand.UNKNOWN) {
    "Any"
} else {
    name.readableEnumName()
}

private fun String.readableEnumName(): String = lowercase()
    .replace('_', ' ')
    .replaceFirstChar(Char::uppercaseChar)

private val ALLOWED_HORIZONS = listOf(30, 90, 180, 365)
