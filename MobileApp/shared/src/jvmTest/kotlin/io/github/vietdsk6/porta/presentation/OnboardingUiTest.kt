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
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiEvent
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingUiState
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingGoal
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OnboardingUiTest {
    @Test
    fun `direction and continue remain reachable at 320 dp and 200 percent text`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        OnBoardingScreen(
                            uiState = OnBoardingUiState(selectedGoal = OnboardingGoal.SCHOLARSHIPS, isLoading = false),
                            onUiEvent = events::add,
                        )
                    }
                }
            }
        }

        onNodeWithTag("onboarding-goal-exchanges").performScrollTo().assertIsDisplayed()
        onNodeWithTag("onboarding-continue").assertIsDisplayed().performClick()
        assertEquals(OnBoardingUiEvent.Next, events.last())
    }

    @Test
    fun `second step keeps all three paths reachable at large text`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        OnBoardingScreen(
                            uiState = OnBoardingUiState(currentStep = 1, selectedGoal = OnboardingGoal.SCHOLARSHIPS, isLoading = false),
                            onUiEvent = events::add,
                        )
                    }
                }
            }
        }

        onNodeWithTag("onboarding-use-demo-profile").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.UseDemoProfile, events.last())
        onNodeWithTag("onboarding-build-profile").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.BuildMyMap, events.last())
        onNodeWithTag("onboarding-explore-sample").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.ExploreSample, events.last())
    }

    @Test
    fun `direction choice is explicit and can be skipped`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(uiState = OnBoardingUiState(isLoading = false), onUiEvent = events::add)
            }
        }

        onNodeWithText("What opportunities excite you?").assertExists()
        onNodeWithTag("onboarding-goal-scholarships").performClick()
        assertEquals(OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS), events.last())
        onNodeWithTag("onboarding-skip").performClick()
        assertEquals(OnBoardingUiEvent.SkipDemo, events.last())
    }

    @Test
    fun `Linh's demo claims zero doors passing checks`() = runComposeUiTest {
        setContent {
            AppTheme {
                OnBoardingScreen(
                    uiState = OnBoardingUiState(currentStep = 1, selectedGoal = OnboardingGoal.SCHOLARSHIPS, isLoading = false),
                    onUiEvent = {},
                )
            }
        }

        onNodeWithText("FICTIONAL DEMO · SAVED ON THIS DEVICE").assertExists()
        onNodeWithText("Linh currently has 0 doors passing every assessed check", substring = true).assertExists()
        onNodeWithText("Show me Linh’s route").assertExists()
    }

    @Test
    fun `moving to the route choice resets the scroll position`() = runComposeUiTest {
        val screenState = mutableStateOf(OnBoardingUiState(selectedGoal = OnboardingGoal.SCHOLARSHIPS, isLoading = false))
        setContent {
            AppTheme {
                OnBoardingScreen(
                    uiState = screenState.value,
                    onUiEvent = { event ->
                        if (event == OnBoardingUiEvent.Next) screenState.value = screenState.value.copy(currentStep = 1)
                    },
                )
            }
        }

        onNodeWithText("Exchange programs").performScrollTo()
        onNodeWithTag("onboarding-continue").performClick()
        onNodeWithText("See how a route takes shape.").assertIsDisplayed()
    }
}
