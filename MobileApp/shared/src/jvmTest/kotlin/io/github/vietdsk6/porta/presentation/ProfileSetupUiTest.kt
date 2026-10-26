package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiEvent
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiState
import io.github.vietdsk6.porta.presentation.screens.profilesetup.StudyLevelOption
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class ProfileSetupUiTest {
    @Test
    fun `minimum profile save remains reachable at 320 dp and 200 percent text`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        ProfileSetupScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = ProfileSetupUiState(
                                studyLevel = StudyLevelOption.UNDERGRADUATE,
                                fieldOfStudy = "Computer Science",
                            ),
                            onUiEvent = {},
                            onNavigateBack = {},
                        )
                    }
                }
            }
        }

        onNodeWithTag("profile-setup-save").performScrollTo().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun `profile validation rejects unsupported IELTS increments and future birth dates`() {
        val state = ProfileSetupUiState(
            studyLevel = StudyLevelOption.UNDERGRADUATE,
            fieldOfStudy = "Computer Science",
            ielts = "6.3",
            dateOfBirth = "2027-01-01",
            validationDate = LocalDate(2026, 10, 26),
        )

        assertEquals("Use a whole or half band, for example 6.5", state.ieltsError)
        assertEquals("Date of birth cannot be in the future", state.dateOfBirthError)
        assertFalse(state.canSave)
    }

    @Test
    fun `new profile asks for essentials before optional facts`() = runComposeUiTest {
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithTag("profile-gpa").assertDoesNotExist()
        onNodeWithTag("profile-setup-save").performScrollTo().assertIsNotEnabled()
        onNodeWithText("Choose your current study level to continue.").assertExists()
    }

    @Test
    fun `requested fact explains the reason and opens its section`() = runComposeUiTest {
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                    requestedFactKey = FactKey.DEGREE_COMPLETED,
                    requestContext = "Applicants must hold a bachelor's degree or equivalent.",
                )
            }
        }

        onNodeWithTag("profile-requested-degree_completed").assertExists()
        onNodeWithText("WHY PORTA IS ASKING").assertExists()
        onNodeWithText("First degree completed").performScrollTo().assertExists()
    }

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
