package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    fun `real profile exposes its explicit edit and data controls`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        var editRequested = false
        var inspectorRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState,
                    onUiEvent = { events += it },
                    onEditProfile = { editRequested = true },
                    onOpenPackInspector = { inspectorRequested = true },
                )
            }
        }

        onNodeWithText("ACADEMIC PROFILE").assertExists()
        onNodeWithText("Your student profile").assertExists()
        onNodeWithTag("student-profile-edit").performClick()
        assertTrue(editRequested)

        onNodeWithText(
            text = "Aurora Scholars",
            substring = true,
            useUnmergedTree = true,
        ).assertExists()

        onNodeWithTag("student-profile-export").performScrollTo().performClick()
        assertEquals(StudentProfileUiEvent.ExportMetadata, events.last())

        onNodeWithTag("student-profile-pack-inspector").performScrollTo().performClick()
        assertTrue(inspectorRequested)
    }

    @Test
    fun `empty profile sends the student to explicit profile setup`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        var buildRequested = false
        var inspectorRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = StudentProfileUiState(isLoading = false),
                    onUiEvent = { events += it },
                    onEditProfile = { buildRequested = true },
                    onOpenPackInspector = { inspectorRequested = true },
                )
            }
        }

        onNodeWithTag("student-profile-build").performClick()
        assertTrue(buildRequested)
        onNodeWithTag("student-profile-explore-pack").performClick()
        assertTrue(inspectorRequested)
        onNodeWithTag("student-profile-reset-all").performScrollTo().performClick()
        assertEquals(
            StudentProfileUiEvent.DataActionRequested(ProfileDataAction.RESET_ALL_LOCAL_DATA),
            events.last(),
        )
    }

    @Test
    fun `profile data controls expose two distinct deletion scopes`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState,
                    onUiEvent = { events += it },
                    onEditProfile = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithTag("student-profile-clear").performScrollTo().performClick()
        assertEquals(
            StudentProfileUiEvent.DataActionRequested(ProfileDataAction.CLEAR_PROFILE_AND_PLANS),
            events.last(),
        )
        onNodeWithTag("student-profile-reset-all").performScrollTo().performClick()
        assertEquals(
            StudentProfileUiEvent.DataActionRequested(ProfileDataAction.RESET_ALL_LOCAL_DATA),
            events.last(),
        )
    }

    @Test
    fun `clear profile confirmation names both removed and retained data`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(
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
        assertEquals(StudentProfileUiEvent.DataActionConfirmed, events.last())
    }

    @Test
    fun `reset confirmation inventories all data and bundled catalog recovery`() = runComposeUiTest {
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = actualProfileState.copy(
                        pendingDataAction = ProfileDataAction.RESET_ALL_LOCAL_DATA,
                    ),
                    onUiEvent = {},
                    onEditProfile = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithText("Reset Porta on this device?").assertExists()
        onNodeWithText("saved doors, imported opportunity catalogs", substring = true).assertExists()
        onNodeWithText("Porta restores the bundled catalog", substring = true).assertExists()
    }

    @Test
    fun `profile names missing facts without treating them as failures`() = runComposeUiTest {
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
                )
            }
        }

        onNodeWithTag("student-profile-unchecked").assertExists()
        onNodeWithText("Missing never means failed.", substring = true).assertExists()
        onNodeWithTag("student-profile-add-missing").performScrollTo().performClick()
        assertTrue(editRequested)
    }

    private val actualProfileState = StudentProfileUiState(
        isLoading = false,
        hasRealProfile = true,
        factCount = 1,
        expandedFactKey = FactKey.IELTS_SCORE,
        sections = listOf(
            ProfileFactSectionUiState(
                title = "Academic",
                description = "Study status and measurable academic facts.",
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
