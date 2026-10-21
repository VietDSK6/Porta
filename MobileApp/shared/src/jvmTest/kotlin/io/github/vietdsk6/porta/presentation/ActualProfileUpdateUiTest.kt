package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreen
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiEvent
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ActualProfileUpdateUiTest {
    @Test
    fun `completed plan requires explicit confirmation and offers a non-writing exit`() = runComposeUiTest {
        val events = mutableListOf<ActualProfileUpdateUiEvent>()
        var navigatedBack = false
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState,
                    onUiEvent = { events += it },
                    onNavigateBack = { navigatedBack = true },
                )
            }
        }

        onNodeWithText("PLAN COMPLETE · PROFILE STILL UNCHANGED").assertExists()
        onNodeWithText("6.5 band").assertExists()
        onNodeWithText("7.0 band").assertExists()
        onNodeWithTag("confirm-actual-profile-update").performScrollTo().performClick()
        assertEquals(ActualProfileUpdateUiEvent.Confirm, events.last())
        onNodeWithTag("cancel-actual-profile-update").performScrollTo().performClick()
        assertTrue(navigatedBack)
    }

    @Test
    fun `optional evidence remains hidden until the user asks for it`() = runComposeUiTest {
        val events = mutableListOf<ActualProfileUpdateUiEvent>()
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState,
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("evidence-label-input").assertDoesNotExist()
        onNodeWithTag("include-evidence-checkbox").performScrollTo().performClick()
        assertEquals(ActualProfileUpdateUiEvent.IncludeEvidenceChanged(true), events.last())
    }

    private val completedState = ActualProfileUpdateUiState(
        isLoading = false,
        hasCompletedPlan = true,
        planTitle = "Raise IELTS to 7.0 band",
        factLabel = "IELTS score",
        currentValueLabel = "6.5 band",
        plannedTargetLabel = "7.0 band",
        achievedValue = "7.0",
        unitLabel = "band",
    )
}
