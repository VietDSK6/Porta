package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.presentation.screens.route.RouteNotice
import io.github.vietdsk6.porta.presentation.screens.route.RouteOptionUiState
import io.github.vietdsk6.porta.presentation.screens.route.RouteReason
import io.github.vietdsk6.porta.presentation.screens.route.RouteReasonKind
import io.github.vietdsk6.porta.presentation.screens.route.RouteScreen
import io.github.vietdsk6.porta.presentation.screens.route.RouteStepUiState
import io.github.vietdsk6.porta.presentation.screens.route.RouteUiEvent
import io.github.vietdsk6.porta.presentation.screens.route.RouteUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RouteUiTest {
    @Test
    fun `route progress excludes a door lost before the route ends`() {
        val route = routeOption("zero", 0, "Door A")
        assertEquals(1, route.improvedDoorCount)
        assertEquals(
            0,
            route.copy(steps = route.steps.map { it.copy(doorsLost = listOf("Door A")) }).improvedDoorCount,
        )
    }

    @Test
    fun `selecting another route changes the announced outcome`() = runComposeUiTest {
        var state by mutableStateOf(routeState)
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = state,
                    onUiEvent = { event ->
                        if (event is RouteUiEvent.RouteSelected) state = state.copy(selectedRouteId = event.id)
                    },
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-choice-1"))
        onNodeWithTag("route-choice-1").performClick()
        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-why"))
        onNodeWithTag("route-why").performClick()
        onNodeWithTag("route-summary").assertExists()
        assertEquals("second", state.selectedRouteId)
        assertEquals(1, state.selectedRoute?.openedTotal)
    }

    @Test
    fun `save route is explicit and goes through the event callback`() = runComposeUiTest {
        val events = mutableListOf<RouteUiEvent>()
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = routeState,
                    onUiEvent = events::add,
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-save"))
        onNodeWithTag("route-save").performClick()
        assertEquals(listOf<RouteUiEvent>(RouteUiEvent.SaveRoute), events)
    }

    @Test
    fun `first move opens the selected route action`() = runComposeUiTest {
        var openedAction: String? = null
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = routeState.copy(selectedRouteId = "second"),
                    onUiEvent = {},
                    onOpenNextMoves = { openedAction = it },
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-first-move"))
        onNodeWithTag("route-first-move").performClick()
        assertEquals("step-second", openedAction)
    }

    @Test
    fun `focused route says when no milestone changes the selected door`() = runComposeUiTest {
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = RouteUiState(
                        isLoading = false,
                        focusedOpportunityId = "chosen-door",
                        focusedOpportunityTitle = "Chosen door",
                    ),
                    onUiEvent = {},
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithText("For Chosen door").assertExists()
        onNodeWithText("No route changes this opportunity within these limits").assertExists()
    }

    @Test
    fun `all cost and effort choices remain reachable on compact route`() = runComposeUiTest {
        val events = mutableListOf<RouteUiEvent>()
        setContent {
            AppTheme {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    RouteScreen(
                        uiState = routeState,
                        onUiEvent = events::add,
                        onOpenNextMoves = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-cost-unknown"))
        onNodeWithTag("route-cost-unknown").assertIsDisplayed().performClick()
        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-effort-very_high"))
        onNodeWithTag("route-effort-very_high").assertIsDisplayed().performClick()
        assertEquals(listOf(RouteUiEvent.MaxCostChanged(CostBand.UNKNOWN), RouteUiEvent.MaxEffortChanged(EffortLevel.VERY_HIGH)), events)
    }

    @Test
    fun `save stays disabled while route is recalculating`() = runComposeUiTest {
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = routeState.copy(isRecalculating = true),
                    onUiEvent = {},
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-save"))
        onNodeWithTag("route-save").assertIsNotEnabled()
    }

    @Test
    fun `saved route confirms the active plan beside the save button`() = runComposeUiTest {
        var openedActivePlan = false
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = routeState.copy(notice = RouteNotice.SAVED),
                    onUiEvent = {},
                    onOpenNextMoves = {},
                    onOpenActivePlan = { openedActivePlan = true },
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-save-feedback"))
        onNodeWithTag("route-save").assertIsNotEnabled()
        onNodeWithText("Route saved to your active plan.").assertIsDisplayed()
        onNodeWithTag("route-view-saved-plan").assertIsDisplayed().performClick()
        assertTrue(openedActivePlan)
    }

    @Test
    fun `empty route shows a way to adjust limits`() = runComposeUiTest {
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = RouteUiState(isLoading = false),
                    onUiEvent = {},
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-empty"))
        onNodeWithTag("route-empty").assertExists()
    }

    @Test
    fun `zero opened doors is stated before route explanation`() = runComposeUiTest {
        setContent {
            AppTheme {
                RouteScreen(
                    uiState = RouteUiState(isLoading = false, routes = listOf(routeOption("zero", 0, "Door A"))),
                    onUiEvent = {},
                    onOpenNextMoves = {},
                    onOpenActivePlan = {},
                )
            }
        }

        onNodeWithText("1 opportunity could gain a passing check.").assertExists()
        onNodeWithText("No door passes every assessed check yet.").assertExists()
        onNodeWithTag("route-summary").assertDoesNotExist()
        onNodeWithTag("route-why").performClick()
        onNodeWithTag("route-summary").assertExists()
    }

    @Test
    fun `reduced motion completes the metro path and leaves controls usable`() = runComposeUiTest(
        effectContext = RouteReducedMotionScale,
    ) {
        setContent {
            AppTheme {
                Box(Modifier.size(width = 1000.dp, height = 720.dp)) {
                    RouteScreen(
                        uiState = routeState,
                        onUiEvent = {},
                        onOpenNextMoves = {},
                        onOpenActivePlan = {},
                    )
                }
            }
        }

        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-metro-map"))
        onNodeWithTag("route-metro-map").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Route path ready"),
        )
        onNodeWithTag("route-list").performScrollToNode(hasTestTag("route-why"))
        onNodeWithTag("route-why").performClick()
        onNodeWithTag("route-summary").assertExists()
    }
}

private object RouteReducedMotionScale : MotionDurationScale {
    override val scaleFactor: Float = 0f
}

private val routeState = RouteUiState(
    isLoading = false,
    canSave = true,
    routes = listOf(
        routeOption("first", 2, "Door A"),
        routeOption("second", 1, "Door B"),
    ),
)

private fun routeOption(id: String, openedTotal: Int, doorTitle: String) = RouteOptionUiState(
    id = id,
    steps = listOf(
        RouteStepUiState(
            id = "step-$id",
            title = "Complete $id milestone",
            startDateLabel = "2026-11-11",
            endDateLabel = "2027-01-10",
            durationDays = 60,
            doorsOpened = if (openedTotal > 0) listOf(doorTitle) else emptyList(),
            doorsImproved = listOf(doorTitle),
            doorsLost = emptyList(),
            effort = EffortLevel.MEDIUM,
            cost = CostBand.LOW,
        ),
    ),
    openedTotal = openedTotal,
    openedSaved = 0,
    daysToFirstDoor = 60,
    totalDays = 60,
    costRankSum = 1,
    reason = if (openedTotal > 0) {
        RouteReason(RouteReasonKind.OPENS, openedTotal)
    } else {
        RouteReason(RouteReasonKind.IMPROVES, 1)
    },
)
