package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiEvent
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiState
import io.github.vietdsk6.porta.presentation.screens.profilesetup.StudyLevelOption
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfileSetupUiTest {
    @Test
    fun `valid minimum profile emits an explicit local save`() = runComposeUiTest {
        val events = mutableListOf<ProfileSetupUiEvent>()
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                        nationality = "VN",
                        gpa = "3.6",
                        ielts = "6.5",
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("LOCAL-FIRST").assertExists()
        onNodeWithTag("profile-setup-save").performScrollTo().assertIsEnabled().performClick()
        assertEquals(ProfileSetupUiEvent.Save, events.last())
    }
}
