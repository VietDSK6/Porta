package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
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
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingThresholdLessonUiState
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OnboardingUiTest {
    @Test
    fun `intro and continue remain reachable at 320 dp and 200 percent text`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                Box(Modifier.size(width = 320.dp, height = 640.dp)) {
                    AppTheme {
                        OnBoardingScreen(
                            uiState = OnBoardingUiState(isLoading = false),
                            onUiEvent = events::add,
                        )
                    }
                }
            }
        }

        onNodeWithText("A list of doors is not a plan.").assertExists()
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
                            uiState = OnBoardingUiState(currentStep = 1, thresholdLesson = sampleLesson, isLoading = false),
                            onUiEvent = events::add,
                        )
                    }
                }
            }
        }

        onNodeWithTag("onboarding-try-threshold").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.ThresholdPreviewToggled, events.last())
        onNodeWithTag("onboarding-use-demo-profile").assertIsDisplayed().performClick()
        assertEquals(OnBoardingUiEvent.UseDemoProfile, events.last())
        onNodeWithTag("onboarding-build-profile").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.BuildMyMap, events.last())
        onNodeWithTag("onboarding-explore-sample").performScrollTo().performClick()
        assertEquals(OnBoardingUiEvent.ExploreSample, events.last())
    }

    @Test
    fun `intro can be skipped`() = runComposeUiTest {
        val events = mutableListOf<OnBoardingUiEvent>()
        setContent {
            AppTheme {
                OnBoardingScreen(uiState = OnBoardingUiState(isLoading = false), onUiEvent = events::add)
            }
        }

        onNodeWithText("A list of doors is not a plan.").assertExists()
        onNodeWithTag("onboarding-skip").performClick()
        assertEquals(OnBoardingUiEvent.SkipDemo, events.last())
    }

    @Test
    fun `synthetic threshold lesson is distinct from Linh's sourced route`() = runComposeUiTest {
        setContent {
            AppTheme {
                OnBoardingScreen(
                    uiState = OnBoardingUiState(currentStep = 1, thresholdLesson = sampleLesson, isLoading = false),
                    onUiEvent = {},
                )
            }
        }

        onNodeWithTag("onboarding-threshold-preview").assertExists()
        onNodeWithText("SYNTHETIC IELTS EXAMPLE").assertExists()
        onNodeWithText("FICTIONAL PROFILE · SOURCED CATALOG").assertExists()
        onNodeWithText("Explore Linh’s sourced route").assertExists()
    }

    @Test
    fun `trying IELTS threshold updates only the four evaluated sample checks`() = runComposeUiTest {
        val screenState = mutableStateOf(
            OnBoardingUiState(currentStep = 1, thresholdLesson = sampleLesson, isLoading = false),
        )
        setContent {
            AppTheme {
                OnBoardingScreen(
                    uiState = screenState.value,
                    onUiEvent = { event ->
                        if (event == OnBoardingUiEvent.ThresholdPreviewToggled) {
                            screenState.value = screenState.value.copy(
                                isThresholdPreviewApplied = !screenState.value.isThresholdPreviewApplied,
                            )
                        }
                    },
                )
            }
        }

        onNodeWithTag("onboarding-threshold-outcome").assertTextEquals("4 highlighted sample checks can change")
        onAllNodesWithText("Needs a higher IELTS score").assertCountEquals(4)
        onNodeWithTag("onboarding-try-threshold").performScrollTo().performClick()
        onNodeWithTag("onboarding-threshold-outcome").assertTextEquals("4 example IELTS checks would pass")
        onAllNodesWithText("IELTS check would pass").assertCountEquals(4)
        onNodeWithTag("onboarding-try-threshold").performScrollTo().performClick()
        onNodeWithTag("onboarding-threshold-outcome").assertTextEquals("4 highlighted sample checks can change")
    }

    @Test
    fun `moving to the route choice resets the scroll position`() = runComposeUiTest {
        val screenState = mutableStateOf(OnBoardingUiState(thresholdLesson = sampleLesson, isLoading = false))
        setContent {
            AppTheme {
                Box(Modifier.size(width = 320.dp, height = 400.dp)) {
                    OnBoardingScreen(
                        uiState = screenState.value,
                        onUiEvent = { event ->
                            if (event == OnBoardingUiEvent.Next) screenState.value = screenState.value.copy(currentStep = 1)
                        },
                    )
                }
            }
        }

        onNodeWithText("Start with a fictional example. No account or personal data needed.").performScrollTo()
        onNodeWithTag("onboarding-continue").performClick()
        onNodeWithText("One score changes the map.").assertIsDisplayed()
    }
}

private val sampleLesson = OnboardingThresholdLessonUiState(
    beforeScore = 6.5,
    afterScore = 7.0,
    changedOpportunityTitles = listOf("Aurora Scholars", "Forge Student Lab", "Horizon Exchange", "Spark Fellowship"),
    unchangedOpportunityTitles = listOf("Local Makers Challenge"),
)
