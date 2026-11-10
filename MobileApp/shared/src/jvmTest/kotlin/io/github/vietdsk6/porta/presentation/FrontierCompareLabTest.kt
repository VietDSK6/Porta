@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierDoorComparisonUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiEvent
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.toggleComparedAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FrontierCompareLabUiTest {
    @Test
    fun `desktop compares independent scenarios against the same actual profile`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 1280.dp, height = 1000.dp)) {
                    FrontierScreen(
                        uiState = compareState.copy(comparedActionIds = listOf("ielts", "project")),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-scenario-compare-lab").assertExists()
        onNodeWithTag("frontier-compare-independence-note").assertExists()
        onNodeWithTag("frontier-compare-scenario-ielts").assertExists()
        onNodeWithTag("frontier-compare-scenario-project").assertExists()
        onNodeWithTag("frontier-compare-door-aurora").assertExists()
    }

    @Test
    fun `desktop selector emits transient comparison events`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 1280.dp, height = 900.dp)) {
                    FrontierScreen(
                        uiState = compareState,
                        onUiEvent = { events += it },
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-compare-toggle-ielts").performClick()
        assertTrue(events.contains(FrontierUiEvent.ComparisonToggled("ielts")))
    }

    @Test
    fun `phone remains a single scenario preview`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 900.dp)) {
                    FrontierScreen(
                        uiState = compareState.copy(comparedActionIds = listOf("ielts", "project")),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-compare-selector").assertDoesNotExist()
        onNodeWithTag("frontier-scenario-compare-lab").assertDoesNotExist()
    }

    @Test
    fun `desktop compare stacks at two hundred percent text`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                AppTheme {
                    Box(Modifier.size(width = 1280.dp, height = 1400.dp)) {
                        FrontierScreen(
                            uiState = compareState.copy(comparedActionIds = listOf("ielts", "project")),
                            onUiEvent = {},
                            onPreviewScenario = {},
                            onOpenActivePlan = {},
                        )
                    }
                }
            }
        }

        onNodeWithTag("frontier-desktop-workspace").assertDoesNotExist()
        onNodeWithTag("frontier-compare-stacked").assertExists()
    }

    private val ieltsAction = action(
        id = "ielts",
        title = "IELTS 7.0",
        current = "6.5 band",
        target = "7.0 band",
        newlyOpenCount = 1,
        timingUnverifiedCount = 0,
        comparison = FrontierDoorComparisonUiState(
            id = "aurora",
            title = "Aurora Scholars",
            actualStateLabel = "Needs action",
            previewStateLabel = "Assessed rules passed",
            isNewlyOpen = true,
            isSaved = true,
            requiresDeadlineCheck = false,
        ),
    )
    private val projectAction = action(
        id = "project",
        title = "Project milestone",
        current = "Not set",
        target = "Completed",
        newlyOpenCount = 0,
        timingUnverifiedCount = 1,
        comparison = FrontierDoorComparisonUiState(
            id = "forge",
            title = "Forge Fellows",
            actualStateLabel = "Needs action",
            previewStateLabel = "Needs information",
            isNewlyOpen = false,
            needsVerification = true,
            isSaved = false,
            requiresDeadlineCheck = true,
        ),
    )
    private val compareState = FrontierUiState(
        actions = listOf(ieltsAction, projectAction),
        selectedActionId = ieltsAction.id,
    )

    private fun action(
        id: String,
        title: String,
        current: String,
        target: String,
        newlyOpenCount: Int,
        timingUnverifiedCount: Int,
        comparison: FrontierDoorComparisonUiState,
    ) = FrontierActionUiState(
        id = id,
        title = title,
        currentLabel = current,
        targetLabel = target,
        newlyOpenCount = newlyOpenCount,
        improvedCount = 1,
        savedDoorCount = if (comparison.isSaved) 1 else 0,
        timingUnverifiedCount = timingUnverifiedCount,
        durationDays = 30,
        effort = EffortLevel.MEDIUM,
        cost = CostBand.LOW,
        durationLabel = "Est. 30 days",
        effortLabel = "Medium",
        costLabel = "Low",
        explanation = "Independent encoded rule simulation.",
        doorTitles = listOf(comparison.title),
        doorComparisons = listOf(comparison),
    )
}

class FrontierCompareLabSelectionTest {
    @Test
    fun `comparison selection accepts available actions removes selections and caps at three`() {
        val availableIds = setOf("ielts", "project", "experience", "gpa")
        val selected = listOf("ielts", "project", "experience")

        assertEquals(
            selected,
            toggleComparedAction(selected, actionId = "gpa", availableActionIds = availableIds),
        )
        assertEquals(
            listOf("ielts", "experience"),
            toggleComparedAction(selected, actionId = "project", availableActionIds = availableIds),
        )
        assertEquals(
            selected,
            toggleComparedAction(selected, actionId = "unknown", availableActionIds = availableIds),
        )
    }
}
