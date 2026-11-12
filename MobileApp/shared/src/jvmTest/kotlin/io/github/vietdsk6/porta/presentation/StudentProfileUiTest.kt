package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileDataAction
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileFactSectionUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileFactUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.UncheckedProfileFactUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StudentProfileUiTest {
    @Test
    fun `profile offers Gemini key setup near the summary and sends the entered key`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(aiDraftSettingsAvailable = true),
                    onUiEvent = events::add,
                    onEditProfile = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithText("Gemini drafting").assertExists()
        onNodeWithText("Offline demo is active. Add a Gemini key to enable drafting.").assertExists()
        onNodeWithTag("student-profile-ai-key-input").performScrollTo().performTextInput("demo-key")
        onNodeWithTag("student-profile-ai-key-save").performScrollTo().performClick()
        assertEquals("demo-key", (events.single() as StudentProfileUiEvent.AiKeySaveRequested).key)
    }

    @Test
    fun `real profile shows precise reference count and opens edit and data privacy`() = runComposeUiTest {
        var editRequested = false
        var privacyRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState,
                    onUiEvent = {},
                    onEditProfile = { editRequested = true },
                    onOpenPackInspector = {},
                    onOpenDataPrivacy = { privacyRequested = true },
                )
            }
        }

        onNodeWithText("1 detail provided").assertExists()
        onNodeWithText("Rules in 1 of 15 catalog opportunities reference your details.").assertExists()
        onNodeWithTag("student-profile-edit").performClick()
        assertTrue(editRequested)
        onNodeWithTag("student-profile-data-privacy").performScrollTo().performClick()
        assertTrue(privacyRequested)
    }

    @Test
    fun `saving from profile edit shows a dismissible local confirmation`() = runComposeUiTest {
        var dismissed = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState,
                    onUiEvent = {},
                    onEditProfile = {},
                    onOpenPackInspector = {},
                    saveNotice = true,
                    onSaveNoticeDismissed = { dismissed = true },
                )
            }
        }

        onNodeWithText("Profile saved on this device.", substring = true).assertExists()
        onNodeWithText("Dismiss").performScrollTo().performClick()
        assertTrue(dismissed)
    }

    @Test
    fun `persisted demo profile remains clearly labelled as fictional`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(isDemoProfile = true),
                    onUiEvent = { events += it },
                    onEditProfile = {},
                    onOpenPackInspector = {},
                    onOpenDataPrivacy = {},
                )
            }
        }

        onNodeWithTag("student-profile-demo-notice").assertExists()
        onNodeWithText("FICTIONAL DEMO PROFILE").assertExists()
        onNodeWithText("These fictional facts were saved locally", substring = true).assertExists()
        onNodeWithTag("student-profile-remove-demo").performClick()
        assertTrue(events.last() == StudentProfileUiEvent.DataActionRequested(ProfileDataAction.CLEAR_PROFILE_AND_PLANS))
    }

    @Test
    fun `demo removal asks for confirmation before clearing profile and plans`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(
                        isDemoProfile = true,
                        pendingDataAction = ProfileDataAction.CLEAR_PROFILE_AND_PLANS,
                    ),
                    onUiEvent = { events += it },
                    onEditProfile = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithText("Clear academic profile and plans?").assertExists()
        onNodeWithText("Saved doors, the opportunity catalog", substring = true).assertExists()
        onNodeWithTag("student-profile-data-confirm").performClick()
        assertTrue(events.last() == StudentProfileUiEvent.DataActionConfirmed)
    }

    @Test
    fun `empty profile can start setup and still opens data privacy`() = runComposeUiTest {
        var buildRequested = false
        var inspectorRequested = false
        var privacyRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = StudentProfileUiState(isLoading = false),
                    onUiEvent = {},
                    onEditProfile = { buildRequested = true },
                    onOpenPackInspector = { inspectorRequested = true },
                    onOpenDataPrivacy = { privacyRequested = true },
                )
            }
        }

        onNodeWithTag("student-profile-build").performClick()
        assertTrue(buildRequested)
        onNodeWithTag("student-profile-explore-pack").performClick()
        assertTrue(inspectorRequested)
        onNodeWithTag("student-profile-data-privacy").performScrollTo().performClick()
        assertTrue(privacyRequested)
    }

    @Test
    fun `zero rule references omit badge and explain current pack scope on expansion`() = runComposeUiTest {
        val state = actualProfileState.copy(
            dependentOpportunityCount = 0,
            sections = listOf(
                actualProfileState.sections.single().copy(
                    facts = listOf(actualProfileState.sections.single().facts.single().copy(dependentDoorTitles = emptyList())),
                ),
            ),
        )
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = state,
                    onUiEvent = {},
                    onEditProfile = {},
                    onOpenPackInspector = {},
                    onOpenDataPrivacy = {},
                )
            }
        }

        onNodeWithText("No current pack rules reference these details yet.").assertExists()
        onNodeWithText("No doors").assertDoesNotExist()
        onNodeWithText("No current pack rule references ielts score.", substring = true).assertExists()
    }

    @Test
    fun `profile summary distinguishes thirteen and fifteen referenced opportunities`() = runComposeUiTest {
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(dependentOpportunityCount = 13),
                    onUiEvent = {},
                    onEditProfile = {},
                    onOpenPackInspector = {},
                    onOpenDataPrivacy = {},
                )
            }
        }

        onNodeWithText("Rules in 13 of 15 catalog opportunities reference your details.").assertExists()
    }

    @Test
    fun `missing referenced detail is optional and never shown as failed`() = runComposeUiTest {
        var editRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(
                        uncheckedFacts = listOf(
                            UncheckedProfileFactUiState(
                                key = FactKey.DEGREE_COMPLETED,
                                label = "Degree completed",
                                dependentDoorCount = 2,
                            ),
                        ),
                    ),
                    onUiEvent = {},
                    onEditProfile = { editRequested = true },
                    onOpenPackInspector = {},
                    onOpenDataPrivacy = {},
                )
            }
        }

        onNodeWithTag("student-profile-unchecked").assertExists()
        onNodeWithText("A missing detail can leave a check unknown. It never means failed.").assertExists()
        onNodeWithTag("student-profile-add-missing").performScrollTo().performClick()
        assertTrue(editRequested)
    }

    private val actualProfileState = StudentProfileUiState(
        isLoading = false,
        hasRealProfile = true,
        factCount = 1,
        catalogOpportunityCount = 15,
        dependentOpportunityCount = 1,
        expandedFactKey = FactKey.IELTS_SCORE,
        sections = listOf(
            ProfileFactSectionUiState(
                title = "Academic record",
                description = "Measured results stay on their original scale.",
                facts = listOf(
                    ProfileFactUiState(
                        key = FactKey.IELTS_SCORE,
                        label = "IELTS score",
                        value = "6.5 band",
                        provenanceLabel = "Self-reported",
                        actionabilityLabel = "Can change",
                        evidenceLabel = null,
                        dependentDoorTitles = listOf("Aurora Scholars"),
                    ),
                ),
            ),
        ),
    )
}
