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
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.presentation.screens.profilesetup.GpaScaleOption
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiEvent
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiState
import io.github.vietdsk6.porta.presentation.screens.profilesetup.StudyLevelOption
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

        onNodeWithText("Saved on this device.").assertExists()
        onNodeWithTag("profile-setup-save").performScrollTo().assertIsEnabled().performClick()
        assertEquals(ProfileSetupUiEvent.Save, events.last())
    }

    @Test
    fun `country search reaches the full local catalog and saves alpha two code`() = runComposeUiTest {
        val events = mutableListOf<ProfileSetupUiEvent>()
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("Context").performScrollTo().performClick()
        onNodeWithTag("profile-nationality").performScrollTo().performClick()
        onNodeWithTag("country-search").performTextInput("Zimbabwe")
        onNodeWithTag("country-zw").assertExists().performClick()
        assertEquals(ProfileSetupUiEvent.NationalityChanged("ZW"), events.last())
    }

    @Test
    fun `country search and cancel remain visible at 320 dp and 200 percent text`() = runComposeUiTest {
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

        onNodeWithText("Context").performScrollTo().performClick()
        onNodeWithTag("profile-nationality").performScrollTo().performClick()
        onNodeWithTag("country-search").assertIsDisplayed()
        onNodeWithTag("country-cancel").assertIsDisplayed().performClick()
        onNodeWithTag("country-search").assertDoesNotExist()
    }

    @Test
    fun `birth date uses a picker and can be explicitly cleared`() = runComposeUiTest {
        val events = mutableListOf<ProfileSetupUiEvent>()
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                        dateOfBirth = "2004-09-18",
                        validationDate = LocalDate(2026, 10, 29),
                    ),
                    onUiEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("Context").performScrollTo().performClick()
        onNodeWithTag("profile-date-of-birth").performScrollTo().performClick()
        onNodeWithTag("profile-date-confirm").assertIsEnabled().performClick()
        assertEquals(ProfileSetupUiEvent.DateOfBirthChanged("2004-09-18"), events.last())
        onNodeWithTag("profile-date-clear").performScrollTo().performClick()
        assertEquals(ProfileSetupUiEvent.DateOfBirthChanged(""), events.last())
    }

    @Test
    fun `future birth date cannot be confirmed from calendar`() = runComposeUiTest {
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                        dateOfBirth = "2027-01-01",
                        validationDate = LocalDate(2026, 10, 29),
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        onNodeWithText("Context").performScrollTo().performClick()
        onNodeWithTag("profile-date-of-birth").performScrollTo().performClick()
        onNodeWithTag("profile-date-confirm").assertIsNotEnabled()
    }

    @Test
    fun `birth date dialog keeps confirmation visible at 320 dp and 200 percent text`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        ProfileSetupScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = ProfileSetupUiState(
                                studyLevel = StudyLevelOption.UNDERGRADUATE,
                                fieldOfStudy = "Computer Science",
                                dateOfBirth = "2004-09-18",
                                validationDate = LocalDate(2026, 10, 29),
                            ),
                            onUiEvent = {},
                            onNavigateBack = {},
                        )
                    }
                }
            }
        }

        onNodeWithText("Context").performScrollTo().performClick()
        onNodeWithTag("profile-date-of-birth").performScrollTo().performClick()
        onNodeWithTag("profile-date-confirm").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun `optional details and original GPA scale remain independent`() {
        val state = ProfileSetupUiState(
            studyLevel = StudyLevelOption.UNDERGRADUATE,
            fieldOfStudy = "Computer Science",
            gpa = "8.5",
            gpaScale = GpaScaleOption.TEN,
        )

        assertTrue(state.canSave)
        assertEquals(1, state.academicProvidedCount)
        assertEquals(0, state.experienceProvidedCount)
        assertEquals("Choose a valid nationality before saving.", state.copy(nationality = "XX").saveDisabledReason)
    }

    @Test
    fun `privacy link is reachable from edit form`() = runComposeUiTest {
        var opened = false
        setContent {
            AppTheme {
                ProfileSetupScreen(
                    uiState = ProfileSetupUiState(
                        studyLevel = StudyLevelOption.UNDERGRADUATE,
                        fieldOfStudy = "Computer Science",
                    ),
                    onUiEvent = {},
                    onNavigateBack = {},
                    onOpenDataPrivacy = { opened = true },
                )
            }
        }

        onNodeWithTag("profile-setup-data-privacy").performScrollTo().performClick()
        assertTrue(opened)
    }
}
