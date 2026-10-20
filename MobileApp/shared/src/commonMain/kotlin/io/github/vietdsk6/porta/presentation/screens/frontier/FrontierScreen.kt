package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun FrontierScreen(
    viewModel: FrontierViewModel,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FrontierScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onPreviewScenario = onPreviewScenario,
        onOpenActivePlan = onOpenActivePlan,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun FrontierScreen(
    uiState: FrontierUiState,
    onUiEvent: (FrontierUiEvent) -> Unit,
    onPreviewScenario: (String) -> Unit,
    onOpenActivePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("frontier-screen"),
        isScrollableContent = false,
        title = "Action Frontier",
        includeBottomInsets = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (uiState.isSampleMode) {
                            "SAMPLE PROFILE · SYNTHETIC PACK"
                        } else {
                            "REAL PROFILE · SYNTHETIC PACK"
                        },
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Only non-dominated, controllable milestones appear here.",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyLarge,
                    )
                    ConstraintCard(uiState.horizonDays, onUiEvent)
                }
            }
            uiState.activePlan?.let { plan ->
                item { ActivePlanPinnedCard(plan = plan, onOpen = onOpenActivePlan) }
            }
            if (uiState.actions.isEmpty()) {
                item { NoFeasibleActionCard() }
            } else {
                items(uiState.actions, key = FrontierActionUiState::id) { action ->
                    FrontierCard(action = action, onPreview = { onPreviewScenario(action.id) })
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ActivePlanPinnedCard(plan: ActivePlanSummaryUiState, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-active-plan"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        border = BorderStroke(1.dp, PORTA_TEAL),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("ACTIVE PLAN · ${plan.statusLabel.uppercase()}", color = PORTA_TEAL, fontWeight = FontWeight.Bold)
            Text(plan.title, color = AppTheme.colors.onAlternative, style = AppTheme.typography.h6)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onOpen) { Text("Open plan") }
        }
    }
}

@Composable
private fun ConstraintCard(horizonDays: Int, onUiEvent: (FrontierUiEvent) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Planning horizon", style = AppTheme.typography.h6)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(30, 90, 180, 365)) { days ->
                    FilterChip(
                        selected = horizonDays == days,
                        onClick = { onUiEvent(FrontierUiEvent.HorizonChanged(days)) },
                        label = { Text(if (days < 365) "$days days" else "1 year") },
                    )
                }
            }
            Text(
                "Sample constraints: up to very-high effort and unknown cost.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FrontierCard(action: FrontierActionUiState, onPreview: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("frontier-action-${action.id}"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(topStart = 42.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 42.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("PARETO FRONTIER", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
            Text(action.title, style = AppTheme.typography.h5)
            Text(
                "${action.newlyOpenCount} doors open · ${action.savedDoorCount} saved",
                color = PORTA_TEAL,
                style = AppTheme.typography.h6,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(action.durationLabel, Modifier.weight(1f))
                MetricPill(action.effortLabel, Modifier.weight(1f))
                MetricPill(action.costLabel, Modifier.weight(1f))
            }
            Text(action.explanation, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
            Text(
                action.doorTitles.joinToString(prefix = "Opens: ", separator = " · "),
                style = AppTheme.typography.bodyMedium,
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = onPreview) {
                Text("Preview what changes")
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            color = AppTheme.colors.onAlternative,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NoFeasibleActionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No feasible milestone in this horizon", style = AppTheme.typography.h6)
            Text(
                "Try a longer horizon. Porta will not pretend a 45-day milestone fits into 30 days.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

private val PORTA_TEAL = Color(0xFF2DD4BF)

@Preview
@Composable
private fun FrontierScreenPreview() {
    AppTheme(isDarkMode = true) {
        FrontierScreen(
            uiState = FrontierUiState(
                actions = listOf(
                    FrontierActionUiState(
                        id = "raise-ielts",
                        title = "Raise IELTS from 6.5 to 7.0",
                        targetLabel = "7.0 band",
                        newlyOpenCount = 4,
                        savedDoorCount = 2,
                        durationLabel = "45 days",
                        effortLabel = "Medium",
                        costLabel = "Low",
                        explanation = "Opens four synthetic sample opportunities before their deadlines.",
                        doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
                    ),
                ),
            ),
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}
