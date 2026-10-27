package io.github.vietdsk6.porta.presentation.screens.activeplan

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun ActivePlanScreen(
    viewModel: ActivePlanViewModel,
    onNavigateBack: () -> Unit,
    onBrowseActions: () -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActivePlanScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onBrowseActions = onBrowseActions,
        onPreviewImpact = onPreviewImpact,
        onUpdateProfile = onUpdateProfile,
    )
}

@Composable
fun ActivePlanScreen(
    uiState: ActivePlanUiState,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onBrowseActions: () -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("active-plan-screen"),
        isScrollableContent = false,
        title = "Active plan",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        when {
            uiState.isLoading -> LoadingState()

            uiState.plan == null -> EmptyPlanState(onBrowseActions)

            else -> ActivePlanContent(
                uiState = uiState,
                onUiEvent = onUiEvent,
                onPreviewImpact = onPreviewImpact,
                onUpdateProfile = onUpdateProfile,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            "Loading your local plan…",
            modifier = Modifier.padding(top = 12.dp),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun EmptyPlanState(onBrowseActions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("No active milestone", style = AppTheme.typography.h5)
                Text(
                    "Preview a controllable action first. Saving it creates one calm, local plan—not a streak.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyLarge,
                )
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("browse-actions-button"),
                    onClick = onBrowseActions,
                ) {
                    Text("Browse Action Frontier")
                }
            }
        }
    }
}

@Composable
private fun ActivePlanContent(
    uiState: ActivePlanUiState,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
) {
    val plan = checkNotNull(uiState.plan)
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("active-plan-list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (uiState.isSampleCatalog) {
                        "LOCAL PLAN · SYNTHETIC PACK"
                    } else {
                        "LOCAL PLAN · SOURCED PACK"
                    },
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Progress stays on this device. Checklist changes never alter your actual profile.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        }
        item { GoalCard(plan) }
        item { ChecklistCard(plan) }
        item { EvidenceAndImpactCard(plan) }
        item { RiskCard(plan.riskNote) }
        if (plan.status == ActivePlanStatusUi.COMPLETED) {
            item { CompletionBoundaryCard() }
        }
        uiState.error?.let { error ->
            item { Text(error, color = AppTheme.colors.status.error, style = AppTheme.typography.bodyMedium) }
        }
        item {
            PlanActions(
                plan = plan,
                isWorking = uiState.isWorking,
                onUiEvent = onUiEvent,
                onPreviewImpact = onPreviewImpact,
                onUpdateProfile = onUpdateProfile,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun GoalCard(plan: ActivePlanCardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("active-plan-card"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.72f)),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 40.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(plan.statusLabel, color = AppTheme.colors.status.success, fontWeight = FontWeight.Bold)
                Text(plan.updatedLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
            }
            Text(plan.title, style = AppTheme.typography.h5)
            Text(
                "Target: ${plan.targetLabel}",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.h6,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(plan.durationLabel, Modifier.weight(1.4f))
                MetricPill(plan.effortLabel, Modifier.weight(0.8f))
                MetricPill(plan.costLabel, Modifier.weight(0.8f))
            }
            LinearProgressIndicator(
                progress = { plan.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = AppTheme.colors.status.success,
                trackColor = AppTheme.colors.outline.copy(alpha = 0.35f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
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
private fun ChecklistCard(plan: ActivePlanCardUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Progress checklist", style = AppTheme.typography.h6)
            plan.checklist.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = step.isComplete, onCheckedChange = null)
                    Text(step.title, style = AppTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun EvidenceAndImpactCard(plan: ActivePlanCardUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Completion evidence", style = AppTheme.typography.h6)
            Text(plan.evidenceLabel, color = AppTheme.colors.text.secondary)
            Text("Affected doors", style = AppTheme.typography.h6)
            Text(
                plan.affectedDoorTitles.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
                    ?: "No assessed door-state change is shown for this profile.",
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun RiskCard(riskNote: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.status.warningContainer),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Risk note", color = AppTheme.colors.status.warning, fontWeight = FontWeight.Bold)
            Text(riskNote, color = AppTheme.colors.text.primary, style = AppTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CompletionBoundaryCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Work complete ≠ profile updated", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
            Text(
                "Review the result and enter the achieved value explicitly. Porta will not treat a checked box as evidence.",
                color = AppTheme.colors.onAlternative,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PlanActions(
    plan: ActivePlanCardUiState,
    isWorking: Boolean,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (plan.status) {
            ActivePlanStatusUi.PLANNED -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("start-plan-button"),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Start) },
                ) {
                    Text("Start milestone")
                }
            }

            ActivePlanStatusUi.IN_PROGRESS -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("complete-plan-button"),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Complete) },
                ) {
                    Text("Mark work complete")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Pause) },
                ) {
                    Text("Pause plan")
                }
            }

            ActivePlanStatusUi.PAUSED -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Resume) },
                ) {
                    Text("Resume plan")
                }
            }

            ActivePlanStatusUi.COMPLETED -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("update-profile-button"),
                    onClick = onUpdateProfile,
                ) {
                    Text("Review actual profile update")
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onPreviewImpact(plan.actionId) },
        ) {
            Text("Preview impact again")
        }
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isWorking,
            onClick = { onUiEvent(ActivePlanUiEvent.Remove) },
        ) {
            Text("Remove local plan", color = AppTheme.colors.status.error)
        }
    }
}

@Preview
@Composable
private fun ActivePlanScreenPreview() {
    AppTheme(isDarkMode = true) {
        ActivePlanScreen(
            uiState = ActivePlanUiState(
                isLoading = false,
                plan = ActivePlanCardUiState(
                    id = "default:raise-ielts",
                    actionId = "raise-ielts-aurora",
                    title = "Raise IELTS to 7.0 band",
                    targetLabel = "7.0 band",
                    status = ActivePlanStatusUi.IN_PROGRESS,
                    statusLabel = "In progress",
                    durationLabel = "45-day working window",
                    effortLabel = "Medium",
                    costLabel = "Low",
                    evidenceLabel = "Official score report",
                    affectedDoorTitles = listOf("Aurora Scholars", "Forge Student Lab", "Horizon Exchange"),
                    riskNote = "This milestone uses a synthetic opportunity pack. Confirm real deadlines first.",
                    updatedLabel = "Updated 21.10.2026",
                    progress = 2f / 3f,
                    checklist = listOf(
                        PlanStepUiState("Milestone saved locally", true),
                        PlanStepUiState("Work started", true),
                        PlanStepUiState("Milestone marked complete", false),
                    ),
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onBrowseActions = {},
            onPreviewImpact = {},
            onUpdateProfile = {},
        )
    }
}
