@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation.screens.onboarding

import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnBoardingViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private lateinit var userPreferences: FakeUserPreferences

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        userPreferences = FakeUserPreferences()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first launch starts with an unselected direction`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.completion)
        assertNull(viewModel.uiState.value.selectedGoal)
        assertEquals(0, viewModel.uiState.value.currentStep)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `returning user skips onboarding and opens the sample route`() = runTest(mainDispatcher) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OnboardingCompletion(OnboardingDestination.SAMPLE_MAP, isNewUser = false),
            viewModel.uiState.value.completion,
        )
    }

    @Test
    fun `preference read failure never leaves onboarding on the splash`() = runTest(mainDispatcher) {
        val viewModel = createViewModel(FailingReadUserPreferences())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.completion)
    }

    @Test
    fun `choosing a direction persists it and unlocks the second step`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.currentStep)

        viewModel.onUiEvent(OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS))
        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentStep)
        assertEquals(OnboardingGoal.SCHOLARSHIPS.storageValue, userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL))
        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `exploring sample completes without installing a profile`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.ExploreSample)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.SAMPLE_MAP, viewModel.uiState.value.completion?.destination)
        assertTrue(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `skip remains available from the first step`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.SkipDemo)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.SAMPLE_MAP, viewModel.uiState.value.completion?.destination)
    }

    @Test
    fun `manual profile choice completes toward profile setup`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.BuildMyMap)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.PROFILE_SETUP, viewModel.uiState.value.completion?.destination)
        viewModel.onFinishHandled()
        assertNull(viewModel.uiState.value.completion)
    }

    private fun createViewModel(preferences: UserPreferences = userPreferences) = OnBoardingViewModel(userPreferences = preferences)

    private class FailingReadUserPreferences : UserPreferences by FakeUserPreferences() {
        override suspend fun getString(key: String, defaultValue: String?): String? = error("Read failed")
    }
}
