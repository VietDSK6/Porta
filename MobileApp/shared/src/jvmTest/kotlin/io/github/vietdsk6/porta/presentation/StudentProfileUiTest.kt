package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileFactSectionUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileFactUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiState
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
        var buildRequested = false
        var inspectorRequested = false
        setContent {
            AppTheme {
                StudentProfileScreen(
                    uiState = StudentProfileUiState(isLoading = false),
                    onUiEvent = {},
                    onEditProfile = { buildRequested = true },
                    onOpenPackInspector = { inspectorRequested = true },
                )
            }
        }

        onNodeWithTag("student-profile-build").performClick()
        assertTrue(buildRequested)
        onNodeWithTag("student-profile-pack-inspector").performClick()
        assertTrue(inspectorRequested)
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
