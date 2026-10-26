package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenStyle
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiEvent
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiState
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingGoal
import io.github.vietdsk6.porta.presentation.screens.onboarding.buildOnboardingDemoDoors
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OnboardingUiTest {
    @Test
    fun `primary action remains reachable at 320 dp and 200 percent text`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        OnBoardingScreen(
                            style = OnBoardingScreenStyle.STYLE1,
                            uiState = onboardingState(currentStep = 0, sampleIelts = 6.5),
                            onUiEvent = { events += it },
                        )
                    }
                }
            }
        }

        onNodeWithTag("onboarding-continue").assertIsDisplayed().performClick()
        assertEquals(OnBoardingUiEvent.Next, events.last())
    }

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

        onNodeWithText("What opportunities excite you?").assertExists()
        onNodeWithTag("onboarding-goal-scholarships").performClick()
        assertEquals(
            OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS),
            events.last(),
        )
    }

    @Test
    fun `sample scenario uses one IELTS interaction without a redundant action`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = onboardingState(currentStep = 1, sampleIelts = 6.5),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithText("INTERACTIVE DEMO · FICTIONAL DATA").assertExists()
        onNodeWithTag("onboarding-ielts-slider").assertExists()
        onNodeWithText("Try IELTS 7.0").assertDoesNotExist()
        onNodeWithText("Move the score to 7.0 to test 4 sample requirements").assertExists()
    }

    @Test
    fun `result explains requirement changes without claiming acceptance`() = runComposeUiTest {
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = onboardingState(
                        currentStep = 2,
                        sampleIelts = 7.0,
                        isReceiptExpanded = true,
                    ),
                    onUiEvent = {},
                )
            }
        }

        onNodeWithTag("onboarding-header").assertIsDisplayed()
        onNodeWithTag("onboarding-progress").assertIsDisplayed()
        onNodeWithText("4 sample IELTS requirements met").assertExists()
        onNodeWithTag("onboarding-door-aurora").assertExists()
        onNodeWithText(
            "The IELTS requirement is met. Other conditions remain outside this fictional demonstration.",
        ).assertExists()
    }

    @Test
    fun `completed demo offers a replay action`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = onboardingState(currentStep = 1, sampleIelts = 7.0),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithTag("onboarding-reset-demo").performClick()
        assertEquals(OnBoardingUiEvent.ResetDemo, events.last())
    }

    @Test
    fun `moving to the next step resets the scroll position`() = runComposeUiTest {
        val screenState = mutableStateOf(onboardingState(currentStep = 0, sampleIelts = 6.5))
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = screenState.value,
                    onUiEvent = { event ->
                        if (event == OnBoardingUiEvent.Next) {
                            screenState.value = screenState.value.copy(currentStep = 1)
                        }
                    },
                )
            }
        }

        onNodeWithText("Exchange programs").performScrollTo()
        onNodeWithTag("onboarding-continue").performClick()

        onNodeWithText("One change. More possibilities.").assertIsDisplayed()
        onNodeWithTag("onboarding-header").assertIsDisplayed()
    }

    @Test
    fun `final choice keeps sample and real profile paths separate`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(
                    style = OnBoardingScreenStyle.STYLE1,
                    uiState = onboardingState(currentStep = 3, sampleIelts = 7.0),
                    onUiEvent = { events += it },
                )
            }
        }

        onNodeWithText("Example data is separate from your profile. Your profile stays on this device.")
            .assertExists()
        onNodeWithTag("onboarding-build-profile").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.BuildMyMap, events.last())
    }

    private fun onboardingState(
        currentStep: Int,
        sampleIelts: Double,
        isReceiptExpanded: Boolean = false,
    ): OnBoardingUiState = OnBoardingUiState(
        currentStep = currentStep,
        selectedGoal = OnboardingGoal.SCHOLARSHIPS,
        sampleIelts = sampleIelts,
        demoDoors = buildOnboardingDemoDoors(
            eligibilityEvaluator = EligibilityEvaluator(),
            sampleIelts = sampleIelts,
            selectedGoal = OnboardingGoal.SCHOLARSHIPS,
        ),
        isReceiptExpanded = isReceiptExpanded,
        isLoading = false,
    )
}
