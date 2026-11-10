package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanCardUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreen
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanStatusUi
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanUiEvent
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanOpportunityImpactUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanStepState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanStepUiState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanTimingStatus
import kotlin.test.Test
import kotlin.test.assertEquals
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
        onNodeWithTag("active-plan-list").performScrollToNode(hasText("Suggested evidence"))
        onNodeWithText("Suggested evidence").assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasText("Work complete · actual result pending"))
        onNodeWithText("Work complete · actual result pending").assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasTestTag("update-profile-button"))
        onNodeWithTag("update-profile-button").performClick()
        assertTrue(updateProfile)
    }

    @Test
    fun `planned milestone shows next work step and start action without a percent bar`() = runComposeUiTest {
        val events = mutableListOf<ActivePlanUiEvent>()
        setContent {
            AppTheme {
                ActivePlanScreen(
                    uiState = plannedState,
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onBrowseActions = {},
                    onPreviewImpact = {},
                    onUpdateProfile = {},
                )
            }
        }

        onNodeWithTag("active-plan-list").performScrollToNode(hasTestTag("plan-status-timeline"))
        onNodeWithText("Plan status").assertExists()
        onNodeWithText("Start the work").assertExists()
        onNodeWithText("Next").assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasTestTag("start-plan-button"))
        onNodeWithTag("start-plan-button").performClick()
        assertEquals(listOf<ActivePlanUiEvent>(ActivePlanUiEvent.Start), events)
    }

    @Test
    fun `removing a plan requires confirmation`() = runComposeUiTest {
        val events = mutableListOf<ActivePlanUiEvent>()
        setContent {
            AppTheme {
                ActivePlanScreen(
                    uiState = plannedState,
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onBrowseActions = {},
                    onPreviewImpact = {},
                    onUpdateProfile = {},
                )
            }
        }

        onNodeWithTag("active-plan-list").performScrollToNode(hasText("Remove local plan"))
        onNodeWithText("Remove local plan").performClick()
        assertTrue(events.isEmpty())
        onNodeWithText("Keep plan").performClick()
        assertTrue(events.isEmpty())
        onNodeWithText("Remove local plan").performClick()
        onNodeWithText("Remove plan").performClick()
        assertEquals(listOf<ActivePlanUiEvent>(ActivePlanUiEvent.Remove), events)
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

    @Test
    fun `plan separates five candidate checks from four not ruled out by known deadlines`() = runComposeUiTest {
        val impacts = (0 until 5).map { index ->
            PlanOpportunityImpactUiState(
                opportunityId = "door-$index",
                title = "Opportunity $index",
                timing = if (index == 4) PlanTimingStatus.MISSES_KNOWN_DEADLINE else PlanTimingStatus.WITHIN_KNOWN_DEADLINE,
            )
        }
        val state = plannedState.copy(plan = plannedState.plan!!.copy(candidateCheckChanges = impacts))
        setContent {
            AppTheme {
                ActivePlanScreen(
                    uiState = state,
                    onUiEvent = {},
                    onNavigateBack = {},
                    onBrowseActions = {},
                    onPreviewImpact = {},
                    onUpdateProfile = {},
                )
            }
        }

        onNodeWithText("Deadline concern", substring = true).assertExists()
        onNodeWithTag("active-plan-list").performScrollToNode(hasTestTag("plan-candidate-impact-count"))
        onNodeWithText("5 opportunities have a potential check change").assertExists()
        onNodeWithText("4 not ruled out by a known deadline").assertExists()
        onNodeWithTag("plan-impact-expand").performScrollTo().performClick()
        onNodeWithTag("plan-impact-door-4").assertExists()
        onNodeWithText("Estimate may miss the published deadline").assertExists()
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
            durationLabel = "Time estimate · 45 days (Porta assumption)",
            effortLabel = "Effort · Medium",
            costLabel = "Cost · Low",
            evidenceLabel = "Official score report",
            candidateCheckChanges = listOf(
                PlanOpportunityImpactUiState("aurora", "Aurora Scholars", PlanTimingStatus.WITHIN_KNOWN_DEADLINE),
            ),
            riskNote = "Synthetic pack; confirm real deadlines.",
            updatedLabel = "Updated 21.10.2026",
            steps = listOf(
                PlanStepUiState("Plan saved", PlanStepState.DONE),
                PlanStepUiState("Work marked complete", PlanStepState.DONE),
                PlanStepUiState("Confirm actual result", PlanStepState.NEXT),
            ),
        ),
    )

    private val plannedState = completedState.copy(
        plan = completedState.plan!!.copy(
            status = ActivePlanStatusUi.PLANNED,
            statusLabel = "Planned",
            steps = listOf(
                PlanStepUiState("Plan saved", PlanStepState.DONE),
                PlanStepUiState("Start the work", PlanStepState.NEXT),
                PlanStepUiState("Confirm actual result", PlanStepState.LATER),
            ),
        ),
    )
}
