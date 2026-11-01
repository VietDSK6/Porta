package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.dataprivacy.DataPrivacyScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileDataAction
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DataPrivacyUiTest {
    @Test
    fun `data route exposes export and distinct removal scopes`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        var openedInspector = false
        setContent {
            AppTheme {
                DataPrivacyScreen(
                    uiState = StudentProfileUiState(isLoading = false, hasRealProfile = true),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onOpenPackInspector = { openedInspector = true },
                )
            }
        }

        onNodeWithTag("student-profile-pack-inspector").performScrollTo().performClick()
        assertTrue(openedInspector)
        onNodeWithTag("student-profile-export").performScrollTo().performClick()
        assertEquals(StudentProfileUiEvent.ExportMetadata, events.last())
        onNodeWithText("Evidence files are not included.", substring = true).assertExists()

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
    fun `empty profile cannot export or clear but can reset`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                DataPrivacyScreen(
                    uiState = StudentProfileUiState(isLoading = false),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithTag("student-profile-export").performScrollTo().assertIsNotEnabled()
        onNodeWithTag("student-profile-clear").performScrollTo().assertIsNotEnabled()
        onNodeWithTag("student-profile-reset-all").performScrollTo().performClick()
        assertEquals(
            StudentProfileUiEvent.DataActionRequested(ProfileDataAction.RESET_ALL_LOCAL_DATA),
            events.last(),
        )
    }

    @Test
    fun `clear requires explicit confirmation and names retained data`() = runComposeUiTest {
        val events = mutableListOf<StudentProfileUiEvent>()
        setContent {
            AppTheme {
                DataPrivacyScreen(
                    uiState = StudentProfileUiState(
                        isLoading = false,
                        hasRealProfile = true,
                        pendingDataAction = ProfileDataAction.CLEAR_PROFILE_AND_PLANS,
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithText("Clear academic profile and plans?").assertExists()
        onNodeWithText("Saved doors, the opportunity catalog", substring = true).assertExists()
        onNodeWithText("Keep data").performClick()
        assertEquals(StudentProfileUiEvent.DataActionCancelled, events.last())
        onNodeWithTag("student-profile-data-confirm").performClick()
        assertEquals(StudentProfileUiEvent.DataActionConfirmed, events.last())
    }

    @Test
    fun `reset confirmation names bundled catalog recovery`() = runComposeUiTest {
        setContent {
            AppTheme {
                DataPrivacyScreen(
                    uiState = StudentProfileUiState(
                        isLoading = false,
                        pendingDataAction = ProfileDataAction.RESET_ALL_LOCAL_DATA,
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                    onOpenPackInspector = {},
                )
            }
        }

        onNodeWithText("Reset Porta on this device?").assertExists()
        onNodeWithText("saved doors, imported opportunity catalogs", substring = true).assertExists()
        onNodeWithText("Porta restores the bundled catalog", substring = true).assertExists()
    }
}
