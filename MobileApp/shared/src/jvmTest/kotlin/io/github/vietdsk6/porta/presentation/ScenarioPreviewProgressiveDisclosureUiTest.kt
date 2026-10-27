package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorOutcome
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioRuleChangeUiState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScenarioPreviewProgressiveDisclosureUiTest {
    @Test
    fun `what-if shows impact changes and remaining checks before evidence detail`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 360.dp, height = 760.dp)) {
                    ScenarioPreviewScreen(
                        uiState = scenarioState,
                        onUiEvent = {},
                        onNavigateBack = {},
                        onBuildProfile = {},
                    )
                }
            }
        }

        onNodeWithText("1 door would pass current checks").assertExists()
        onNodeWithTag("action-impact-diagram").assertExists()
        onNodeWithTag("action-impact-node-aurora").assertExists()
        onNodeWithTag("scenario-scope-notice").assertExists()
        onNodeWithText("What changes").assertExists()
        onNodeWithText("Requires IELTS 7.0 or above.").assertExists()
        onAllNodesWithText("6.5 band → 7.0 band").assertCountEquals(2)
        onNodeWithTag("scenario-preview-content").performScrollToIndex(3)
        onNodeWithText("What remains").assertExists()
        onNodeWithText("Check result").assertDoesNotExist()
        onNodeWithTag("scenario-details-aurora").performScrollTo().performClick()
        onNodeWithTag("scenario-evidence-aurora").assertExists()
        onNodeWithText("Check result").assertExists()
        onNodeWithText("Planning timing").assertExists()
    }

    private val scenarioState = ScenarioPreviewUiState(
        actionId = "raise-ielts",
        title = "Raise IELTS from 6.5 to 7.0",
        beforeLabel = "6.5 band",
        afterLabel = "7.0 band",
        scopeNotice = "Would satisfy this programme-specific check if confirmed.",
        openedDoors = listOf(
            ScenarioDoorUiState(
                id = "aurora",
                title = "Aurora Scholars",
                stateLabel = "Would pass current checks",
                actualStateLabel = "Needs action",
                simulatedStateLabel = "Current checks passed",
                changedRules = listOf(
                    ScenarioRuleChangeUiState(
                        id = "ielts",
                        explanation = "Requires IELTS 7.0 or above.",
                        beforeLabel = "6.5 band",
                        afterLabel = "7.0 band",
                        resultTransitionLabel = "Needs action → Check passed",
                    ),
                ),
                remainingBlockers = listOf("Confirm the published deadline."),
                timingLabel = "Deadline: TBA — verify before planning",
                outcome = ScenarioDoorOutcome.CURRENT_CHECKS_PASSED,
            ),
        ),
        isSampleProfile = false,
    )
}
