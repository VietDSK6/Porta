package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreen
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiEvent
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiState
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualValueInputKind
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.EvidenceAttachmentUiState
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
                    uiState = completedState.copy(achievedValue = "7.0"),
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
    fun `actual result starts empty and cannot be confirmed without user input`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(inputKind = ActualValueInputKind.BOOLEAN),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("Choose or enter what is true now. The planned target is only a reference.").assertExists()
        onNodeWithText("Complete these steps").assertExists()
        onNodeWithText("Select actual result").assertExists()
        onNodeWithText("Add supporting evidence").assertExists()
        onNodeWithText("Select Yes or No to continue").assertExists()
        onNodeWithTag("confirm-actual-profile-update").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `required evidence is explained without a premature error`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        inputKind = ActualValueInputKind.BOOLEAN,
                        achievedBoolean = true,
                        isEvidenceRequired = true,
                        includeEvidence = true,
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("evidence-label-input").performScrollTo().assertExists()
        onNodeWithText("Required for a Yes result: add a title or file").assertExists()
        onNodeWithText("Add a short title, or attach a file instead").assertExists()
        onNodeWithText("Add a short title or attach a file").assertDoesNotExist()
    }

    @Test
    fun `No result clearly skips programme evidence`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        inputKind = ActualValueInputKind.BOOLEAN,
                        achievedBoolean = false,
                        isEvidenceRequired = true,
                        includeEvidence = false,
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("Not needed for No").assertExists()
        onNodeWithText("No supporting evidence is required for a No result.").assertExists()
        onNodeWithTag("evidence-label-input").assertDoesNotExist()
        onNodeWithTag("confirm-actual-profile-update").performScrollTo().assertIsEnabled()
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

    @Test
    fun `date input explains the accepted portable format`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        inputKind = ActualValueInputKind.DATE,
                        unitLabel = "",
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("achieved-value-input").performScrollTo().assertExists()
        onNodeWithText("Actual date").assertExists()
        onNodeWithTag("achieved-value-input").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expected date format YYYY-MM-DD"),
        )
        onNodeWithText("Use an ISO date in YYYY-MM-DD format").assertExists()
    }

    @Test
    fun `duration input shows and preserves the plan unit`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        inputKind = ActualValueInputKind.DURATION,
                        unitLabel = "months",
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("achieved-value-input").performScrollTo().assertExists()
        onNodeWithText("Actual duration").assertExists()
        onNodeWithTag("achieved-value-input").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Duration unit: months"),
        )
        onNodeWithText("Enter a whole number; the plan unit is preserved").assertExists()
    }

    @Test
    fun `enum-list input explains how to enter multiple values`() = runComposeUiTest {
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        inputKind = ActualValueInputKind.ENUM_LIST,
                        unitLabel = "",
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("achieved-value-input").performScrollTo().assertExists()
        onNodeWithText("Achieved values").assertExists()
        onNodeWithText("Separate multiple values with commas or new lines").assertExists()
    }

    @Test
    fun `stored evidence attachment can be opened or removed`() = runComposeUiTest {
        val events = mutableListOf<ActualProfileUpdateUiEvent>()
        setContent {
            AppTheme {
                ActualProfileUpdateScreen(
                    uiState = completedState.copy(
                        includeEvidence = true,
                        evidenceLabel = "Official score report",
                        evidenceAttachment = EvidenceAttachmentUiState(
                            displayName = "ielts-result.pdf",
                            mimeType = "application/pdf",
                            byteSize = 4_096L,
                            isStored = true,
                        ),
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("evidence-attachment-card").performScrollTo().assertExists()
        onNodeWithText("ielts-result.pdf").assertExists()
        onNodeWithText("application/pdf · 4 KB").assertExists()
        onNodeWithTag("open-evidence-attachment").performClick()
        assertEquals(ActualProfileUpdateUiEvent.OpenEvidenceAttachment, events.last())
        onNodeWithTag("remove-evidence-attachment").performClick()
        assertEquals(ActualProfileUpdateUiEvent.RemoveEvidenceAttachment, events.last())
        onNodeWithText("Porta does not independently verify authenticity or submit it.", substring = true)
            .assertExists()
    }

    private val completedState = ActualProfileUpdateUiState(
        isLoading = false,
        hasCompletedPlan = true,
        planTitle = "Raise IELTS to 7.0 band",
        factLabel = "IELTS score",
        currentValueLabel = "6.5 band",
        plannedTargetLabel = "7.0 band",
        achievedValue = "",
        unitLabel = "band",
    )
}
