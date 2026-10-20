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
    fun `first launch shows demo without fabricating a selected goal`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.completion)
        assertNull(state.selectedGoal)
        assertFalse(state.isLoading)
    }

    @Test
    fun `returning user skips onboarding and opens the sample map`() = runTest(mainDispatcher) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)

        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        assertEquals(
            OnboardingCompletion(
                destination = OnboardingDestination.SAMPLE_MAP,
                isNewUser = false,
            ),
            viewModel.uiState.value.completion,
        )
    }

    @Test
    fun `selecting a goal persists the local focus`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS))
        advanceUntilIdle()

        assertEquals(OnboardingGoal.SCHOLARSHIPS, viewModel.uiState.value.selectedGoal)
        assertEquals(
            OnboardingGoal.SCHOLARSHIPS.storageValue,
            userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL),
        )
    }

    @Test
    fun `sample control rounds to honest half band increments`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(6.82))
        advanceUntilIdle()

        assertEquals(7.0, viewModel.uiState.value.sampleIelts)
        assertFalse(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `exploring sample completes onboarding without creating a profile`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.ExploreSample)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.SAMPLE_MAP, viewModel.uiState.value.completion?.destination)
        assertTrue(viewModel.uiState.value.completion?.isNewUser == true)
        assertTrue(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `build my map completes toward profile setup`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.BuildMyMap)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.PROFILE_SETUP, viewModel.uiState.value.completion?.destination)

        viewModel.onFinishHandled()
        assertNull(viewModel.uiState.value.completion)
    }
}
