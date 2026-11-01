package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorOutcome
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioRuleChangeUiState
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScenarioPreviewScalabilityUiTest {
    @Test
    fun `zero affected checks has a clear no change state`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(360.dp, 760.dp)) {
                    ScenarioPreviewScreen(emptyState, onUiEvent = {}, onNavigateBack = {}, onBuildProfile = {})
                }
            }
        }

        onNodeWithText("No assessed checks change").assertExists()
        onNodeWithTag("scenario-ripple-toggle").assertDoesNotExist()
        onNodeWithText("No assessed check changes").assertExists()
    }

    @Test
    fun `five results show exact counts and only one expanded detail`() = runComposeUiTest {
        val state = emptyState.copy(affectedDoors = (0 until 5).map { door(it, warning = it < 2) })
        setContent {
            AppTheme {
                Box(Modifier.size(360.dp, 760.dp)) {
                    ScenarioPreviewScreen(state, onUiEvent = {}, onNavigateBack = {}, onBuildProfile = {})
                }
            }
        }

        onNodeWithText("5 opportunities affected").assertExists()
        onNodeWithText("5 checks could change · 2 timing concerns").assertExists()
        onNodeWithTag("action-impact-diagram").assertDoesNotExist()
        onNodeWithTag("scenario-preview-content").performScrollToIndex(5)
        onNodeWithTag("scenario-details-door-0").performClick()
        onNodeWithTag("scenario-selected-door-0").assertExists()
        onNodeWithTag("scenario-preview-content").performScrollToIndex(6)
        onNodeWithTag("scenario-details-door-1").performClick()
        onNodeWithTag("scenario-selected-door-0").assertDoesNotExist()
        onNodeWithTag("scenario-selected-door-1").assertExists()
    }

    @Test
    fun `twelve results keep ripple optional and all rows reachable at large font`() = runComposeUiTest {
        val state = emptyState.copy(affectedDoors = (0 until 12).map { door(it) })
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                AppTheme {
                    Box(Modifier.size(320.dp, 760.dp)) {
                        ScenarioPreviewScreen(state, onUiEvent = {}, onNavigateBack = {}, onBuildProfile = {})
                    }
                }
            }
        }

        onNodeWithText("12 opportunities affected").assertExists()
        onNodeWithTag("scenario-ripple-toggle").performScrollTo().performClick()
        onNodeWithTag("scenario-preview-content").performScrollToIndex(4)
        onNodeWithTag("action-impact-diagram").assertExists()
        onNodeWithText("+7 more in Results").assertExists()
        onNodeWithTag("scenario-ripple-toggle").performScrollTo().performClick()
        onNodeWithTag("scenario-preview-content").performScrollToIndex(16)
        onNodeWithTag("scenario-result-door-11").assertExists()
    }

    @Test
    fun `desktop keeps selected details and official source alongside results`() = runComposeUiTest {
        var opened: String? = null
        val state = emptyState.copy(affectedDoors = listOf(door(0)))
        setContent {
            AppTheme {
                Box(Modifier.size(1200.dp, 800.dp)) {
                    ScenarioPreviewScreen(
                        state,
                        onUiEvent = {},
                        onNavigateBack = {},
                        onBuildProfile = {},
                        onOpenSource = { opened = it },
                    )
                }
            }
        }

        onNodeWithTag("scenario-desktop-workspace").assertExists()
        onNodeWithTag("scenario-desktop-selected-detail").assertExists()
        onNodeWithTag("scenario-selected-door-0").assertExists()
        onNodeWithTag("scenario-evidence-toggle-door-0").performClick()
        onNodeWithTag("scenario-source-door-0").performScrollTo().performClick()
        runOnIdle { assertEquals("https://example.org/door-0", opened) }
    }

    private val emptyState = ScenarioPreviewUiState(
        actionId = "raise-ielts",
        title = "Raise IELTS score",
        beforeLabel = "6.5 band",
        afterLabel = "7.0 band",
        isSampleProfile = false,
    )

    private fun door(index: Int, warning: Boolean = false): ScenarioDoorUiState = ScenarioDoorUiState(
        id = "door-$index",
        title = "Opportunity $index with a readable long name",
        stateLabel = "Would address a checked requirement",
        outcome = ScenarioDoorOutcome.CHECKED_PROGRESS,
        actualStateLabel = "Needs action",
        simulatedStateLabel = "Needs information",
        changedRules = listOf(
            ScenarioRuleChangeUiState(
                id = "rule-$index",
                explanation = "IELTS threshold in the published rule.",
                beforeLabel = "6.5 band",
                afterLabel = "7.0 band",
                resultTransitionLabel = "Needs action → Check passed",
                sourceNote = "Official eligibility section",
            ),
        ),
        remainingBlockers = listOf("Still needs source review: confirm enrollment."),
        timingLabel = "Published deadline: 2026-12-07",
        timingWarning = if (warning) "The Porta estimate may exceed the published deadline." else null,
        sourceUrl = "https://example.org/door-$index",
        sourceSection = "Eligibility",
        lastCheckedLabel = "Source checked 2026-10-28",
    )
}
