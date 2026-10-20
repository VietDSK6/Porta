package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.presentation.screens.doors.DoorCardUiState
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiEvent
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.RuleReceiptUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PortaVerticalSliceUiTest {
    @Test
    fun `door card opens its source backed receipt`() = runComposeUiTest {
        var openedId: String? = null
        setContent {
            AppTheme {
                DoorsScreen(
                    uiState = doorsState,
                    onUiEvent = {},
                    onOpenOpportunity = { openedId = it },
                )
            }
        }

        onNodeWithText("SAMPLE PROFILE · SYNTHETIC PACK").assertExists()
        onNodeWithTag("door-card-aurora").performClick()
        assertEquals("aurora", openedId)
    }

    @Test
    fun `frontier horizon control emits an explicit constraint change`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                FrontierScreen(
                    uiState = frontierState,
                    onUiEvent = { events += it },
                    onPreviewScenario = {},
                )
            }
        }

        onNodeWithText("30 days").performClick()
        assertTrue(events.contains(FrontierUiEvent.HorizonChanged(30)))
    }

    @Test
    fun `scenario keeps what-if label visible and saves only on explicit action`() = runComposeUiTest {
        var saved = false
        setContent {
            AppTheme {
                ScenarioPreviewScreen(
                    uiState = scenarioState,
                    onUiEvent = {},
                    onNavigateBack = {},
                    onSavePlan = { saved = true },
                )
            }
        }

        onNodeWithText("WHAT-IF · SAMPLE").assertExists()
        onNodeWithText("4 doors open").assertExists()
        onNodeWithText("Add milestone to plan").performClick()
        assertTrue(saved)
    }

    @Test
    fun `receipt exposes actual required and source limits`() = runComposeUiTest {
        setContent {
            AppTheme {
                OpportunityDetailScreen(
                    uiState = receiptState,
                    onUiEvent = {},
                    onNavigateBack = {},
                    onPreviewAction = {},
                )
            }
        }

        onNodeWithText("6.5 band").assertExists()
        onNodeWithText("7.0 band").assertExists()
        onNodeWithText("Source & limits").assertExists()
        onNodeWithText("Sample data only.").assertExists()
    }

    private val doorsState = DoorsUiState(
        doors = listOf(
            DoorCardUiState(
                id = "aurora",
                title = "Aurora Scholars",
                provider = "Northstar Foundation",
                state = EligibilityState.HAS_GAPS,
                deadlineLabel = "Due 2027-06-01",
                checkedLabel = "Checked 2026-10-20",
                isSaved = true,
                primaryReason = "The synthetic sample requires IELTS 7.0 or above.",
            ),
        ),
        openCount = 1,
        gapCount = 4,
    )

    private val frontierState = FrontierUiState(
        actions = listOf(
            FrontierActionUiState(
                id = "raise-ielts",
                title = "Raise IELTS from 6.5 to 7.0",
                targetLabel = "7.0 band",
                newlyOpenCount = 4,
                savedDoorCount = 2,
                durationLabel = "45 days",
                effortLabel = "Medium",
                costLabel = "Low",
                explanation = "Opens four synthetic doors before their deadlines.",
                doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
            ),
        ),
    )

    private val scenarioState = ScenarioPreviewUiState(
        actionId = "raise-ielts",
        title = "Raise IELTS from 6.5 to 7.0",
        beforeLabel = "6.5 band",
        afterLabel = "7.0 band",
        openedDoors = listOf(
            ScenarioDoorUiState("aurora", "Aurora Scholars", "Opens"),
            ScenarioDoorUiState("forge", "Forge Student Lab", "Opens"),
            ScenarioDoorUiState("horizon", "Horizon Exchange", "Opens"),
            ScenarioDoorUiState("spark", "Spark Fellowship", "Opens"),
        ),
    )

    private val receiptState = OpportunityDetailUiState(
        id = "aurora",
        title = "Aurora Scholars",
        provider = "Northstar Foundation",
        stateLabel = "HAS GAPS",
        deadlineLabel = "Deadline 2027-06-01",
        checkedLabel = "Checked 2026-10-20",
        sourceUrl = "https://example.invalid/porta-sample/aurora",
        disclaimer = "Sample data only.",
        rules = listOf(
            RuleReceiptUiState(
                id = "ielts",
                result = RuleResult.FAIL,
                explanation = "The synthetic sample requires IELTS 7.0 or above.",
                actualLabel = "6.5 band",
                expectedLabel = "7.0 band",
                sourceNote = "Synthetic sample rule; not a real requirement.",
            ),
        ),
    )
}
