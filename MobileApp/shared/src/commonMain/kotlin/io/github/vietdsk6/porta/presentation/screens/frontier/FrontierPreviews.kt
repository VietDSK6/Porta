package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel

@Preview
@Composable
internal fun FrontierScreenPreview() {
    AppTheme(isDarkMode = true) {
        FrontierScreen(
            uiState = previewState,
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=160")
@Composable
internal fun FrontierDesktopPreview() {
    AppTheme {
        FrontierScreen(
            uiState = previewState.copy(selectedActionId = "raise-ielts"),
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=1000dp,dpi=160")
@Composable
internal fun FrontierDesktopComparePreview() {
    AppTheme {
        FrontierScreen(
            uiState = previewState.copy(
                selectedActionId = "raise-ielts",
                comparedActionIds = listOf("raise-ielts", "verify-enrollment", "community-project"),
            ),
            onUiEvent = {},
            onPreviewScenario = {},
            onOpenActivePlan = {},
        )
    }
}

internal val previewState = FrontierUiState(
    isSampleMode = true,
    isSampleCatalog = true,
    catalogDoorCount = 5,
    actions = listOf(
        FrontierActionUiState(
            id = "raise-ielts",
            title = "Raise IELTS from 6.5 to 7.0",
            currentLabel = "6.5 band",
            targetLabel = "7.0 band",
            newlyOpenCount = 2,
            improvedCount = 4,
            savedDoorCount = 1,
            timingUnverifiedCount = 1,
            durationDays = 45,
            effort = EffortLevel.MEDIUM,
            cost = CostBand.LOW,
            durationLabel = "Est. 45 days",
            effortLabel = "Medium",
            costLabel = "Low",
            explanation = "Addresses a published requirement in four opportunities. Two would pass their current checks.",
            doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
            doorComparisons = listOf(
                FrontierDoorComparisonUiState(
                    id = "aurora",
                    title = "Aurora Scholars",
                    actualStateLabel = "Needs action",
                    previewStateLabel = "Assessed rules passed",
                    isNewlyOpen = true,
                    isSaved = true,
                    requiresDeadlineCheck = false,
                ),
                FrontierDoorComparisonUiState(
                    id = "erasmus",
                    title = "Erasmus Mundus Joint Masters",
                    actualStateLabel = "Needs action",
                    previewStateLabel = "Needs information",
                    isNewlyOpen = false,
                    isSaved = false,
                    requiresDeadlineCheck = true,
                ),
            ),
        ),
        FrontierActionUiState(
            id = "verify-enrollment",
            title = "Verify current enrollment",
            currentLabel = "Self-reported",
            targetLabel = "Evidence attached",
            newlyOpenCount = 1,
            improvedCount = 2,
            savedDoorCount = 2,
            durationDays = 7,
            effort = EffortLevel.LOW,
            cost = CostBand.FREE,
            durationLabel = "7 days",
            effortLabel = "Low",
            costLabel = "Free",
            explanation = "Addresses an enrollment-evidence check in two saved opportunities.",
            doorTitles = listOf("Kotlin Student Coding Competition", "Global Student Challenge"),
        ),
        FrontierActionUiState(
            id = "community-project",
            title = "Complete an evidenced community project",
            currentLabel = "Not set",
            targetLabel = "Completed",
            newlyOpenCount = 1,
            improvedCount = 3,
            savedDoorCount = 0,
            durationDays = 90,
            effort = EffortLevel.HIGH,
            cost = CostBand.MEDIUM,
            durationLabel = "90 days",
            effortLabel = "High",
            costLabel = "Medium",
            explanation = "Addresses a published project requirement in three opportunities.",
            doorTitles = listOf("Impact Fellows", "Civic Builders", "Student Changemakers"),
        ),
    ),
    exclusions = listOf(
        FrontierExcludedActionUiState(
            id = "internship-year",
            title = "Build 12 months of experience",
            reasons = listOf("Needs more time than your horizon"),
        ),
    ),
)
