package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanCardUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreen
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanStatusUi
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanStepUiState
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ActivePlanUiTest {
    @Test
    fun `completed work keeps the explicit profile boundary visible`() = runComposeUiTest {
        var updateProfile = false
        setContent {
            AppTheme {
                ActivePlanScreen(
                    uiState = completedState,
                    onUiEvent = {},
                    onNavigateBack = {},
                    onBrowseActions = {},
                    onPreviewImpact = {},
                    onUpdateProfile = { updateProfile = true },
                )
            }
        }

        onNodeWithTag("active-plan-card").assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasText("Work complete ≠ profile updated"))
        onNodeWithText("Work complete ≠ profile updated").assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasTestTag("update-profile-button"))
        onNodeWithTag("update-profile-button").performClick()
        assertTrue(updateProfile)
    }

    @Test
    fun `empty plan points back to the frontier`() = runComposeUiTest {
        var browse = false
        setContent {
            AppTheme {
                ActivePlanScreen(
                    uiState = ActivePlanUiState(isLoading = false),
                    onUiEvent = {},
                    onNavigateBack = {},
                    onBrowseActions = { browse = true },
                    onPreviewImpact = {},
                    onUpdateProfile = {},
                )
            }
        }

        onNodeWithTag("browse-actions-button").performClick()
        assertTrue(browse)
    }

    private val completedState = ActivePlanUiState(
        isLoading = false,
        plan = ActivePlanCardUiState(
            id = "plan",
            actionId = "raise-ielts-aurora",
            title = "Raise IELTS to 7.0 band",
            targetLabel = "7.0 band",
            status = ActivePlanStatusUi.COMPLETED,
            statusLabel = "Work marked complete",
            durationLabel = "45-day working window",
            effortLabel = "Medium",
            costLabel = "Low",
            evidenceLabel = "Official score report",
            affectedDoorTitles = listOf("Aurora Scholars"),
            riskNote = "Synthetic pack; confirm real deadlines.",
            updatedLabel = "Updated 21.10.2026",
            progress = 1f,
            checklist = listOf(
                PlanStepUiState("Milestone saved locally", true),
                PlanStepUiState("Work started", true),
                PlanStepUiState("Milestone marked complete", true),
            ),
        ),
    )
}
