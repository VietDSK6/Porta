package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

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
