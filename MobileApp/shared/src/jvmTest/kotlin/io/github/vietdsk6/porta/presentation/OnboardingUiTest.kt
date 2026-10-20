package io.github.vietdsk6.porta.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenStyle
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiEvent
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiState
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingGoal
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OnboardingUiTest {
    @Test
    fun `goal choice is explicit before the sample demo`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = OnBoardingUiState(isLoading = false),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithText("Which doors matter to you?").assertExists()
        onNodeWithTag("onboarding-goal-scholarships").performClick()
        assertEquals(
            OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS),
            events.last(),
        )
    }

    @Test
    fun `sample scenario exposes the preserved IELTS action`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = OnBoardingUiState(
                        currentStep = 1,
                        selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                        isLoading = false,
                    ),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithText("SAMPLE PROFILE · FICTIONAL DATA").assertExists()
        onNodeWithTag("onboarding-try-ielts").performClick()
        assertEquals(OnBoardingUiEvent.TryIeltsSeven, events.last())
    }

    @Test
    fun `receipt explains the result without claiming acceptance`() = runComposeUiTest {
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = OnBoardingUiState(
                        currentStep = 2,
                        selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                        sampleIelts = 7.0,
                        isReceiptExpanded = true,
                        isLoading = false,
                    ),
                    onUiEvent = {},
                )
            }
        }

        onNodeWithText("4 DOORS OPENED").assertExists()
        onNodeWithText("Actual in scenario: 7.0 band").assertExists()
        onNodeWithText(
            "Synthetic demo only · no official provider source. Real opportunity rules must carry a source and verification date.",
        ).assertExists()
    }

    @Test
    fun `final choice keeps sample and real profile paths separate`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = OnBoardingUiState(
                        currentStep = 3,
                        selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                        sampleIelts = 7.0,
                        isLoading = false,
                    ),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithTag("onboarding-build-profile").performClick()
        assertEquals(OnBoardingUiEvent.BuildMyMap, events.last())
    }
}
