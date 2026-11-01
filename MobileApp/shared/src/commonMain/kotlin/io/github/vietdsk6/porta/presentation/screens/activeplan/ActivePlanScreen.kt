package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    "This plan stays on this device. Changing its status never alters your actual profile.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        }
        item { GoalCard(plan) }
        item { PlanTimeline(plan) }
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
            Text(
                plan.durationLabel,
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
            if (plan.deadlineMisses.isNotEmpty()) {
                Text(
                    "Deadline concern · ${plan.deadlineMisses.size} ${if (plan.deadlineMisses.size == 1) "opportunity" else "opportunities"} may miss a published deadline with this estimate.",
                    modifier = Modifier.testTag("plan-deadline-warning"),
                    color = AppTheme.colors.status.warning,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(plan.effortLabel, Modifier.weight(1f))
                MetricPill(plan.costLabel, Modifier.weight(1f))
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
private fun PlanTimeline(plan: ActivePlanCardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("plan-status-timeline"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Plan status", style = AppTheme.typography.h6)
            plan.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("plan-step-$index"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = if (step.state == PlanStepState.DONE) {
                            AppTheme.colors.status.success
                        } else {
                            AppTheme.colors.alternative
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (step.state == PlanStepState.DONE) "✓" else "${index + 1}",
                                color = if (step.state == PlanStepState.DONE) {
                                    AppTheme.colors.background
                                } else {
                                    AppTheme.colors.onAlternative
                                },
                                style = AppTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Column {
                        Text(step.title, style = AppTheme.typography.bodyLarge)
                        Text(
                            step.state.label(),
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun PlanStepState.label(): String = when (this) {
    PlanStepState.DONE -> "Done"
    PlanStepState.IN_PROGRESS -> "In progress"
    PlanStepState.PAUSED -> "Paused"
    PlanStepState.NEXT -> "Next"
    PlanStepState.LATER -> "Later"
}

@Composable
private fun EvidenceAndImpactCard(plan: ActivePlanCardUiState) {
    var showAll by remember(plan.id) { mutableStateOf(false) }
    val candidateCount = plan.candidateOpportunityIds.size
    val notRuledOutCount = plan.notRuledOutOpportunityIds.size
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
            Text("Potential check changes", style = AppTheme.typography.h6)
            if (candidateCount == 0) {
                Text(
                    "No assessed requirement changes for your current profile.",
                    color = AppTheme.colors.text.secondary,
                )
            } else {
                Text(
                    "$candidateCount ${if (candidateCount == 1) "opportunity has" else "opportunities have"} a potential check change",
                    modifier = Modifier.testTag("plan-candidate-impact-count"),
                    color = AppTheme.colors.text.primary,
                )
                Text(
                    "$notRuledOutCount not ruled out by a known deadline",
                    modifier = Modifier.testTag("plan-in-plan-impact-count"),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
                if (plan.timingUnverified.isNotEmpty()) {
                    val knownCount = plan.knownDeadlineFeasibleOpportunityIds.size
                    val unverifiedCount = plan.timingUnverified.size
                    Text(
                        "$knownCount ${if (knownCount == 1) "estimate fits a published deadline" else "estimates fit published deadlines"} · " +
                            "$unverifiedCount ${if (unverifiedCount == 1) "needs" else "need"} timing verification",
                        color = AppTheme.colors.status.warning,
                        style = AppTheme.typography.bodySmall,
                    )
                }
                plan.candidateCheckChanges.take(if (showAll) candidateCount else 3).forEach { impact ->
                    Column(Modifier.fillMaxWidth().testTag("plan-impact-${impact.opportunityId}")) {
                        Text(impact.title, style = AppTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            impact.timing.label(),
                            color = if (impact.timing == PlanTimingStatus.MISSES_KNOWN_DEADLINE) {
                                AppTheme.colors.status.warning
                            } else {
                                AppTheme.colors.text.secondary
                            },
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
                if (candidateCount > 3) {
                    TextButton(
                        modifier = Modifier.testTag("plan-impact-expand"),
                        onClick = { showAll = !showAll },
                    ) { Text(if (showAll) "Show fewer opportunities" else "See all $candidateCount opportunities") }
                }
            }
        }
    }
}

private fun PlanTimingStatus.label(): String = when (this) {
    PlanTimingStatus.WITHIN_KNOWN_DEADLINE -> "Estimate fits the published deadline"
    PlanTimingStatus.TIMING_UNVERIFIED -> "Timing needs verification"
    PlanTimingStatus.MISSES_KNOWN_DEADLINE -> "Estimate may miss the published deadline"
    PlanTimingStatus.OUTSIDE_CURRENT_CYCLE -> "Not available in the current cycle"
}

@Composable
private fun RiskCard(riskNote: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(modifier = Modifier.testTag("plan-risk-expand"), onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide planning assumptions" else "Planning assumptions & source checks")
            }
            if (expanded) Text(riskNote, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
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
            Text("Work complete · actual result pending", color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
            Text(
                "Confirm what you achieved to update your profile. Marking work complete does not change facts.",
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
    var showRemoveConfirmation by remember(plan.id) { mutableStateOf(false) }
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
                    enabled = !isWorking,
                    onClick = onUpdateProfile,
                ) {
                    Text("Confirm actual result")
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
            onClick = { showRemoveConfirmation = true },
        ) {
            Text("Remove local plan", color = AppTheme.colors.status.error)
        }
    }
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text("Remove this plan?") },
            text = { Text("The milestone and its status will be removed. Your actual profile stays unchanged.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirmation = false
                        onUiEvent(ActivePlanUiEvent.Remove)
                    },
                ) { Text("Remove plan") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) { Text("Keep plan") }
            },
        )
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
                    durationLabel = "Time estimate · 45 days (Porta assumption)",
                    effortLabel = "Effort · Medium",
                    costLabel = "Cost · Low",
                    evidenceLabel = "Official score report",
                    candidateCheckChanges = listOf(
                        PlanOpportunityImpactUiState("aurora", "Aurora Scholars", PlanTimingStatus.WITHIN_KNOWN_DEADLINE),
                        PlanOpportunityImpactUiState("forge", "Forge Student Lab", PlanTimingStatus.WITHIN_KNOWN_DEADLINE),
                        PlanOpportunityImpactUiState("horizon", "Horizon Exchange", PlanTimingStatus.MISSES_KNOWN_DEADLINE),
                    ),
                    riskNote = "This milestone uses a synthetic opportunity pack. Confirm real deadlines first.",
                    updatedLabel = "Updated 21.10.2026",
                    steps = listOf(
                        PlanStepUiState("Plan saved", PlanStepState.DONE),
                        PlanStepUiState("Work in progress", PlanStepState.IN_PROGRESS),
                        PlanStepUiState("Confirm actual result", PlanStepState.LATER),
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

@Preview(name = "Planned milestone")
@Composable
private fun ActivePlanPlannedPreview() {
    AppTheme {
        ActivePlanScreen(
            uiState = ActivePlanUiState(
                isLoading = false,
                plan = ActivePlanCardUiState(
                    id = "default:raise-ielts",
                    actionId = "raise-ielts-aurora",
                    title = "Raise IELTS to 7.0 band",
                    targetLabel = "7.0 band",
                    status = ActivePlanStatusUi.PLANNED,
                    statusLabel = "Planned",
                    durationLabel = "Time estimate · 45 days (Porta assumption)",
                    effortLabel = "Effort · Medium",
                    costLabel = "Cost · Low",
                    evidenceLabel = "Official score report",
                    candidateCheckChanges = listOf(
                        PlanOpportunityImpactUiState("aurora", "Aurora Scholars", PlanTimingStatus.WITHIN_KNOWN_DEADLINE),
                        PlanOpportunityImpactUiState("forge", "Forge Student Lab", PlanTimingStatus.TIMING_UNVERIFIED),
                    ),
                    riskNote = "This is a planning estimate. Confirm the official deadline before acting.",
                    updatedLabel = "Updated 21.10.2026",
                    steps = listOf(
                        PlanStepUiState("Plan saved", PlanStepState.DONE),
                        PlanStepUiState("Start the work", PlanStepState.NEXT),
                        PlanStepUiState("Confirm actual result", PlanStepState.LATER),
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
