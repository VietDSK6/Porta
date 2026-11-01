package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.presentation.screens.frontier.ActivePlanSummaryUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiState
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FrontierPriorityUiTest {
    @Test
    fun `completed plan leads suggestions and opens confirmation path`() = runComposeUiTest {
        var openedPlan = false
        setContent {
            AppTheme {
                Box(Modifier.size(390.dp, 1200.dp)) {
                    FrontierScreen(
                        uiState = state.copy(activePlan = ActivePlanSummaryUiState("Raise IELTS", PlanStatus.COMPLETED, "Work marked complete")),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = { openedPlan = true },
                    )
                }
            }
        }

        val planTop = onNodeWithTag("frontier-active-plan").fetchSemanticsNode().boundsInRoot.top
        val suggestionTop = onNodeWithTag("frontier-action-raise-ielts").fetchSemanticsNode().boundsInRoot.top
        assertTrue(planTop < suggestionTop)
        onNodeWithText("RESULT TO CONFIRM").assertExists()
        onNodeWithTag("frontier-open-active-plan").performClick()
        runOnIdle { assertTrue(openedPlan) }
    }

    @Test
    fun `planned milestone follows the suggestion`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(390.dp, 1200.dp)) {
                    FrontierScreen(
                        uiState = state.copy(activePlan = ActivePlanSummaryUiState("Raise IELTS", PlanStatus.PLANNED, "Planned")),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        val planTop = onNodeWithTag("frontier-active-plan").fetchSemanticsNode().boundsInRoot.top
        val suggestionTop = onNodeWithTag("frontier-action-raise-ielts").fetchSemanticsNode().boundsInRoot.top
        assertTrue(suggestionTop < planTop)
        onNodeWithText("Open plan").assertExists()
    }

    @Test
    fun `recommendation names opportunities and checks with estimate provenance`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(390.dp, 900.dp)) {
                    FrontierScreen(state, onUiEvent = {}, onPreviewScenario = {}, onOpenActivePlan = {})
                }
            }
        }

        onNodeWithText("1 opportunity · 2 assessed checks could change").assertExists()
        onNodeWithText("Time · Planner assumes 14 days").assertExists()
        onNodeWithText("Effort · Medium").assertExists()
        onNodeWithText("Cost · Unknown").assertExists()
        onNodeWithText("Why this move?").performClick()
        onNodeWithText("Time estimate · basis not stated").assertExists()
    }

    private val action = FrontierActionUiState(
        id = "raise-ielts",
        title = "Raise IELTS to 7.0",
        targetLabel = "7.0 band",
        newlyOpenCount = 0,
        improvedCount = 1,
        savedDoorCount = 0,
        durationDays = 14,
        effort = EffortLevel.MEDIUM,
        cost = CostBand.UNKNOWN,
        durationLabel = "Planner assumes 14 days",
        effortLabel = "Medium",
        costLabel = "Unknown",
        explanation = "One encoded requirement could improve; other checks remain.",
        doorTitles = listOf("Aurora Scholars"),
        changedCheckCount = 2,
    )

    private val state = FrontierUiState(actions = listOf(action))
}
