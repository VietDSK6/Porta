package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.presentation.screens.doors.DoorCardUiState
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierDoorComparisonUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierExcludedActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiEvent
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.RuleReceiptUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiEvent
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
    fun `real doors use compact source context instead of a banner`() = runComposeUiTest {
        setContent {
            AppTheme {
                DoorsScreen(
                    uiState = doorsState.copy(
                        isSampleMode = false,
                        isSampleCatalog = false,
                        catalogDoorCount = 15,
                    ),
                    onUiEvent = {},
                    onOpenOpportunity = {},
                )
            }
        }

        onNodeWithText("15 source-linked opportunities · checked on this device").assertExists()
        onAllNodesWithText(
            "Your local facts are evaluated against published rules checked in the active offline pack.",
        ).assertCountEquals(0)
    }

    @Test
    fun `door filters wrap without clipping on a 320 dp screen`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 320.dp, height = 760.dp)) {
                    DoorsScreen(
                        uiState = doorsState.copy(
                            notAvailableCount = 1,
                            closedCount = 1,
                        ),
                        onUiEvent = {},
                        onOpenOpportunity = {},
                    )
                }
            }
        }

        onNodeWithTag("door-filter-ALL").assertIsDisplayed()
        onNodeWithTag("door-filter-SAVED").assertIsDisplayed()
    }

    @Test
    fun `frontier horizon control emits an explicit constraint change`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 700.dp, height = 600.dp)) {
                    FrontierScreen(
                        uiState = frontierState,
                        onUiEvent = { events += it },
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithText("Adjust").performClick()
        onNodeWithTag("frontier-horizon-30").performScrollTo().performClick()
        assertTrue(events.contains(FrontierUiEvent.HorizonChanged(30)))
    }

    @Test
    fun `frontier emits effort cost and saved priority constraints`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 700.dp, height = 600.dp)) {
                    FrontierScreen(
                        uiState = frontierState,
                        onUiEvent = { events += it },
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithText("Adjust").performClick()
        onNodeWithTag("frontier-effort-MEDIUM").performScrollTo().performClick()
        onNodeWithTag("frontier-cost-LOW").performScrollTo().performClick()
        onNodeWithTag("frontier-prioritize-saved").performScrollTo().performClick()

        assertTrue(events.contains(FrontierUiEvent.MaxEffortChanged(EffortLevel.MEDIUM)))
        assertTrue(
            events.contains(FrontierUiEvent.MaxCostChanged(CostBand.LOW)),
            "Expected LOW cost event, received $events",
        )
        assertTrue(events.contains(FrontierUiEvent.PrioritizeSavedChanged(false)))
    }

    @Test
    fun `frontier explains excluded actions and exposes an accessible impact map`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 700.dp, height = 900.dp)) {
                    FrontierScreen(
                        uiState = frontierState,
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-compact-content").performScrollToIndex(6)
        onNodeWithTag("frontier-trade-offs-toggle").performClick()
        onNodeWithTag("frontier-impact-map").assertExists()
        onNodeWithTag("frontier-compact-content").performScrollToIndex(7)
        onNodeWithTag("frontier-exclusions").assertExists()
        onNodeWithText("Above your cost limit", substring = true).assertExists()
    }

    @Test
    fun `frontier omits trade-off graph when only one action is available`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 360.dp, height = 760.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(actions = frontierState.actions.take(1)),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithText("RECOMMENDED NEXT MOVE").assertExists()
        onNodeWithTag("frontier-trade-offs-toggle").assertDoesNotExist()
        onNodeWithTag("frontier-impact-map").assertDoesNotExist()
    }

    @Test
    fun `wide frontier shows actual versus preview workspace and selects milestones`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 1200.dp, height = 800.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(selectedActionId = "raise-ielts"),
                        onUiEvent = { events += it },
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-desktop-workspace").assertExists()
        onNodeWithText("SIMULATION PREVIEW").assertExists()
        onNodeWithText("6.5 band").assertExists()
        onNodeWithText("7.0 band").assertExists()
        onNodeWithText("Aurora Scholars").assertExists()
        onNodeWithTag("frontier-impact-map").assertExists()
        onNodeWithTag("frontier-plot-point-raise-ielts").performClick()
        assertTrue(events.contains(FrontierUiEvent.ActionSelected("raise-ielts")))
    }

    @Test
    fun `scenario keeps what-if label visible and saves only on explicit action`() = runComposeUiTest {
        val events = mutableListOf<ScenarioPreviewUiEvent>()
        setContent {
            AppTheme {
                ScenarioPreviewScreen(
                    uiState = scenarioState.copy(openedDoors = scenarioState.openedDoors.take(1)),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onBuildProfile = {},
                )
            }
        }

        onNodeWithText("Simulation — your saved profile is unchanged.").assertExists()
        onNodeWithText("1 door is affected by this simulation.").assertExists()
        onNodeWithText("Add milestone to plan").performClick()
        assertTrue(events.contains(ScenarioPreviewUiEvent.SavePlan))
    }

    @Test
    fun `scenario keeps long opportunity titles readable`() = runComposeUiTest {
        setContent {
            AppTheme {
                ScenarioPreviewScreen(
                    uiState = scenarioState.copy(
                        openedDoors = emptyList(),
                        progressedDoors = listOf(
                            ScenarioDoorUiState(
                                id = "erasmus-mundus",
                                title = "Erasmus Mundus Joint Masters",
                                stateLabel = "Still needs information or source review",
                                actualStateLabel = "Needs action",
                                simulatedStateLabel = "Needs information",
                            ),
                        ),
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                    onBuildProfile = {},
                )
            }
        }

        onNodeWithTag("scenario-door-title-erasmus-mundus").assertWidthIsAtLeast(200.dp)
    }

    @Test
    fun `receipt exposes actual required and source limits`() = runComposeUiTest {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 700.dp, height = 700.dp)) {
                    OpportunityDetailScreen(
                        uiState = receiptState,
                        onUiEvent = {},
                        onNavigateBack = {},
                        onPreviewAction = {},
                    )
                }
            }
        }

        onNodeWithText("6.5 band").assertExists()
        onNodeWithText("7.0 band").assertExists()
        onNodeWithTag("opportunity-detail-content").performScrollToIndex(6)
        onNodeWithText("Source and last review").assertExists()
        onNodeWithText("Sample data only.").assertExists()
    }

    private val doorsState = DoorsUiState(
        doors = listOf(
            DoorCardUiState(
                id = "aurora",
                title = "Aurora Scholars",
                provider = "Northstar Foundation",
                state = EligibilityState.HAS_GAPS,
                stateLabel = "NEEDS ACTION",
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
                currentLabel = "6.5 band",
                targetLabel = "7.0 band",
                newlyOpenCount = 4,
                savedDoorCount = 2,
                durationDays = 45,
                effort = EffortLevel.MEDIUM,
                cost = CostBand.LOW,
                durationLabel = "45 days",
                effortLabel = "Medium",
                costLabel = "Low",
                explanation = "Opens four synthetic doors before their deadlines.",
                doorTitles = listOf("Aurora", "Forge", "Horizon", "Spark"),
                doorComparisons = listOf(
                    FrontierDoorComparisonUiState(
                        id = "aurora",
                        title = "Aurora Scholars",
                        actualStateLabel = "Has gaps",
                        previewStateLabel = "Open",
                        isNewlyOpen = true,
                        isSaved = true,
                        requiresDeadlineCheck = false,
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
                savedDoorCount = 1,
                durationDays = 7,
                effort = EffortLevel.LOW,
                cost = CostBand.FREE,
                durationLabel = "7 days",
                effortLabel = "Low",
                costLabel = "Free",
                explanation = "Moves two doors forward.",
                doorTitles = listOf("Kotlin Student Coding Competition", "Aurora"),
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
                explanation = "Moves three doors forward.",
                doorTitles = listOf("Impact", "Civic", "Change"),
            ),
        ),
        exclusions = listOf(
            FrontierExcludedActionUiState(
                id = "costly-certificate",
                title = "Complete an advanced certificate",
                reasons = listOf("Above your cost limit"),
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
        isSampleProfile = false,
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
