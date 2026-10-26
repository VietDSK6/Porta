package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.presentation.screens.doors.DoorCardUiState
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierDoorComparisonUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierExcludedActionUiState
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierMoveKind
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiEvent
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.InformationTaskDestination
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.InformationTaskUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailUiState
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.RuleReceiptUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioDoorUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiEvent
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiState
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioRuleChangeUiState
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

        onNodeWithText("15 source-linked opportunities").assertExists()
        onNodeWithText("Your saved profile is compared with checks encoded from published sources.").assertExists()
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

        onNodeWithText("Adjust").performScrollTo().performClick()
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

        onNodeWithText("Adjust").performScrollTo().performClick()
        onNodeWithTag("frontier-effort-MEDIUM").performScrollTo().performClick()
        onNodeWithTag("frontier-cost-LOW").performScrollTo().performClick()
        onNodeWithTag("frontier-prioritize-saved").performScrollTo().performClick()
        onNodeWithText("Saved-door priority can change which moves reach the frontier", substring = true)
            .assertExists()

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

        onNodeWithText("SUGGESTED IMPROVEMENT").assertExists()
        onNodeWithTag("frontier-trade-offs-toggle").assertDoesNotExist()
        onNodeWithTag("frontier-impact-map").assertDoesNotExist()
    }

    @Test
    fun `frontier groups scoped verification moves without merging their targets`() = runComposeUiTest {
        val firstVerification = frontierState.actions[1].copy(
            id = "verify-oecd",
            title = "Confirm availability for OECD Internship Programme",
            kind = FrontierMoveKind.VERIFY,
            familyId = "verify-availability",
            familyLabel = "Confirm programme availability",
            scopeLabel = "OECD Internship Programme · programme-specific",
        )
        val secondVerification = firstVerification.copy(
            id = "verify-cern",
            title = "Confirm availability for CERN Technical Studentship",
            scopeLabel = "CERN Technical Studentship · programme-specific",
        )
        setContent {
            AppTheme {
                Box(Modifier.size(width = 360.dp, height = 760.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(
                            actions = listOf(frontierState.actions.first(), firstVerification, secondVerification),
                        ),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-compact-content").performScrollToIndex(4)
        onNodeWithText("VERIFY · Confirm programme availability").assertExists()
        onNodeWithText("2 programme-specific checks share this planning pattern.").assertExists()
        onNodeWithTag("frontier-action-verify-oecd").assertExists()
        onNodeWithTag("frontier-action-verify-cern").assertExists()
    }

    @Test
    fun `frontier treats verification as a suggestion and does not present an estimate as fact`() = runComposeUiTest {
        val verification = frontierState.actions[1]
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 760.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(actions = listOf(verification)),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithText("SUGGESTED VERIFICATION").assertExists()
        onNodeWithText("Timing and effort depend on programme confirmation.").assertExists()
        onNodeWithText("7 days").assertDoesNotExist()
    }

    @Test
    fun `frontier reveals longer term growth moves without changing limits silently`() = runComposeUiTest {
        val events = mutableListOf<FrontierUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 760.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(suggestedGrowthHorizonDays = 180),
                        onUiEvent = { events += it },
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-growth-horizon-prompt").assertExists()
        onNodeWithTag("frontier-use-180-days").performScrollTo().performClick()
        assertTrue(events.contains(FrontierUiEvent.HorizonChanged(180)))
    }

    @Test
    fun `frontier hides a plot that has no real trade-off`() = runComposeUiTest {
        val sameVectorAction = frontierState.actions[1].copy(
            durationDays = frontierState.actions.first().durationDays,
            improvedCount = frontierState.actions.first().improvedCount,
            cost = frontierState.actions.first().cost,
        )
        setContent {
            AppTheme {
                Box(Modifier.size(width = 360.dp, height = 760.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(
                            actions = listOf(frontierState.actions.first(), sameVectorAction),
                        ),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-trade-offs-toggle").assertDoesNotExist()
        onNodeWithTag("frontier-impact-map").assertDoesNotExist()
    }

    @Test
    fun `frontier plot explains when multiple moves share a point`() = runComposeUiTest {
        val sameVectorAction = frontierState.actions[1].copy(
            durationDays = frontierState.actions.first().durationDays,
            improvedCount = frontierState.actions.first().improvedCount,
            cost = frontierState.actions.first().cost,
        )
        setContent {
            AppTheme {
                Box(Modifier.size(width = 1200.dp, height = 800.dp)) {
                    FrontierScreen(
                        uiState = frontierState.copy(
                            actions = listOf(
                                frontierState.actions.first(),
                                sameVectorAction,
                                frontierState.actions.last(),
                            ),
                            selectedActionId = frontierState.actions.first().id,
                        ),
                        onUiEvent = {},
                        onPreviewScenario = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("frontier-impact-map").assertExists()
        onNodeWithText("1. 2 moves share this trade-off").assertExists()
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
    fun `large text keeps next moves in a single column at desktop width`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 1200.dp, height = 800.dp)) {
                    AppTheme {
                        FrontierScreen(
                            uiState = frontierState,
                            onUiEvent = {},
                            onPreviewScenario = {},
                            onOpenActivePlan = {},
                        )
                    }
                }
            }
        }

        onNodeWithTag("frontier-compact-content").assertExists()
        onNodeWithTag("frontier-desktop-workspace").assertDoesNotExist()
    }

    @Test
    fun `scenario keeps what-if label visible and saves only on explicit action`() = runComposeUiTest {
        val events = mutableListOf<ScenarioPreviewUiEvent>()
        val changedDoor = scenarioState.openedDoors.first().copy(
            changedRules = listOf(
                ScenarioRuleChangeUiState(
                    id = "ielts",
                    explanation = "Requires IELTS 7.0 or above.",
                    beforeLabel = "6.5 band",
                    afterLabel = "7.0 band",
                    resultTransitionLabel = "Needs action → Check passed",
                ),
            ),
            remainingBlockers = listOf("Still needs source review: Confirm the programme deadline."),
            timingLabel = "Deadline: TBA — verify before planning",
        )
        setContent {
            AppTheme {
                ScenarioPreviewScreen(
                    uiState = scenarioState.copy(openedDoors = listOf(changedDoor)),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onBuildProfile = {},
                )
            }
        }

        onNodeWithText("Simulation — your saved profile is unchanged.").assertExists()
        onNodeWithText("1 door is affected by this simulation.", substring = true).assertExists()
        onNodeWithTag("scenario-outcome-summary").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.LiveRegion,
                LiveRegionMode.Polite,
            ),
        )
        onNodeWithTag("scenario-details-aurora").performScrollTo().performClick()
        onNodeWithText("Changed check").assertExists()
        onNodeWithText("Needs action → Check passed").assertExists()
        onNodeWithText("Still unresolved").assertExists()
        onNodeWithTag("scenario-preview-content").performScrollToIndex(4)
        onNodeWithText("Add milestone to plan").performClick()
        assertTrue(events.contains(ScenarioPreviewUiEvent.SavePlan))
    }

    @Test
    fun `reduced motion keeps the scenario outcome and changed door visible`() = runComposeUiTest(
        effectContext = ReducedMotionScale,
    ) {
        setContent {
            AppTheme {
                ScenarioPreviewScreen(
                    uiState = scenarioState,
                    onUiEvent = {},
                    onNavigateBack = {},
                    onBuildProfile = {},
                )
            }
        }

        onNodeWithTag("scenario-outcome-summary").assertExists()
        onNodeWithText("4 doors are affected by this simulation.", substring = true).assertExists()
        onNodeWithTag("scenario-door-title-aurora").assertExists()
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
        onNodeWithTag("opportunity-detail-content").performScrollToIndex(7)
        onNodeWithText("Source and last review").assertExists()
        onNodeWithText("Sample data only.").assertExists()
    }

    @Test
    fun `wide opportunity detail uses separate decision and evidence panes`() = runComposeUiTest {
        setContent {
            Box(Modifier.size(width = 1200.dp, height = 800.dp)) {
                AppTheme {
                    OpportunityDetailScreen(
                        uiState = receiptState,
                        onUiEvent = {},
                        onNavigateBack = {},
                        onPreviewAction = {},
                    )
                }
            }
        }

        onNodeWithTag("opportunity-detail-desktop-workspace").assertExists()
        onNodeWithTag("opportunity-detail-primary-pane").assertExists()
        onNodeWithTag("opportunity-detail-evidence-pane").assertExists()
    }

    @Test
    fun `large text returns opportunity detail to one readable column`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 1200.dp, height = 800.dp)) {
                    AppTheme {
                        OpportunityDetailScreen(
                            uiState = receiptState,
                            onUiEvent = {},
                            onNavigateBack = {},
                            onPreviewAction = {},
                        )
                    }
                }
            }
        }

        onNodeWithTag("opportunity-detail-content").assertExists()
        onNodeWithTag("opportunity-detail-desktop-workspace").assertDoesNotExist()
    }

    @Test
    fun `missing fact task opens profile without claiming progress`() = runComposeUiTest {
        var requestedFact: FactKey? = null
        var requestContext = ""
        setContent {
            AppTheme {
                Box(Modifier.size(width = 390.dp, height = 700.dp)) {
                    OpportunityDetailScreen(
                        uiState = receiptState.copy(
                            nextStepGuidance = "Adding information is not progress by itself.",
                            informationTasks = listOf(
                                InformationTaskUiState(
                                    id = "degree-status",
                                    factKey = FactKey.DEGREE_COMPLETED,
                                    title = "Add your degree completion status",
                                    question = "Applicants must hold a bachelor's degree or equivalent.",
                                    sourceNote = "Official eligibility section, reviewed 2026-10-25.",
                                    destination = InformationTaskDestination.ACADEMIC_PROFILE,
                                ),
                            ),
                        ),
                        onUiEvent = {},
                        onNavigateBack = {},
                        onPreviewAction = {},
                        onEditProfile = { factKey, context ->
                            requestedFact = factKey
                            requestContext = context
                        },
                    )
                }
            }
        }

        onNodeWithTag("opportunity-detail-content").performScrollToIndex(4)
        onNodeWithTag("information-task-degree-status").assertIsDisplayed()
        onNodeWithText("Update academic profile").performClick()
        runOnIdle {
            assertEquals(FactKey.DEGREE_COMPLETED, requestedFact)
            assertTrue(requestContext.contains("bachelor's degree"))
        }
        onNodeWithText("These tasks only fill missing information.", substring = true).assertExists()
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
                kind = FrontierMoveKind.IMPROVE,
                familyId = "improve-ielts",
                familyLabel = "Improve English score",
                scopeLabel = "Profile-wide · 4 doors",
                estimateSourceLabel = "Time estimate · synthetic demo",
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
                kind = FrontierMoveKind.VERIFY,
                familyId = "verify-enrollment",
                familyLabel = "Confirm enrollment",
                scopeLabel = "Kotlin Student Coding Competition · programme-specific",
                estimateSourceLabel = "Time estimate · Porta assumption",
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
                kind = FrontierMoveKind.PREPARE,
                familyId = "prepare-community-project",
                familyLabel = "Build community evidence",
                estimateSourceLabel = "Time estimate · Porta assumption",
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

private object ReducedMotionScale : MotionDurationScale {
    override val scaleFactor: Float = 0f
}
