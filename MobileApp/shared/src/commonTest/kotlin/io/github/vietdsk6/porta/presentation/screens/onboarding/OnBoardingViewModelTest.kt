@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation.screens.onboarding

import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
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
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.completion)
        assertNull(state.selectedGoal)
        assertEquals(4, state.demoDoors.size)
        assertEquals(0, state.satisfiedIeltsRequirementCount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `returning user skips onboarding and opens the sample map`() = runTest(mainDispatcher) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)

        val viewModel = createViewModel()
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
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS))
        advanceUntilIdle()

        assertEquals(OnboardingGoal.SCHOLARSHIPS, viewModel.uiState.value.selectedGoal)
        assertEquals(
            OnboardingGoal.SCHOLARSHIPS.storageValue,
            userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL),
        )
        assertEquals("Aurora Scholars", viewModel.uiState.value.demoDoors.first().title)
    }

    @Test
    fun `sample control rounds to honest half band increments`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(6.82))
        advanceUntilIdle()

        assertEquals(7.0, viewModel.uiState.value.sampleIelts)
        assertEquals(4, viewModel.uiState.value.satisfiedIeltsRequirementCount)
        assertTrue(viewModel.uiState.value.isDemoComplete)
        assertFalse(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `demo impact comes from synthetic rules and excludes requirements already met`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(7.0))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.demoDoors.size)
        assertEquals(4, state.satisfiedIeltsRequirementCount)
        assertFalse(state.demoDoors.any { it.title == "Local Makers Challenge" })
    }

    @Test
    fun `selected focus moves its matching fictional example first`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.GoalSelected(OnboardingGoal.EXCHANGES))
        advanceUntilIdle()

        assertEquals("Horizon Exchange", viewModel.uiState.value.demoDoors.first().title)
    }

    @Test
    fun `step transitions enforce selection and completed demo`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.currentStep)

        viewModel.onUiEvent(OnBoardingUiEvent.GoalSelected(OnboardingGoal.SCHOLARSHIPS))
        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.currentStep)

        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.currentStep)

        viewModel.onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(7.0))
        viewModel.onUiEvent(OnBoardingUiEvent.Next)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `replay resets only in-memory fictional demo state`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.SampleIeltsChanged(7.0))
        viewModel.onUiEvent(OnBoardingUiEvent.ResetDemo)
        advanceUntilIdle()

        assertEquals(6.5, viewModel.uiState.value.sampleIelts)
        assertEquals(0, viewModel.uiState.value.satisfiedIeltsRequirementCount)
        assertFalse(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `exploring sample completes onboarding without creating a profile`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.ExploreSample)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.SAMPLE_MAP, viewModel.uiState.value.completion?.destination)
        assertTrue(viewModel.uiState.value.completion?.isNewUser == true)
        assertTrue(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `build my map completes toward profile setup`() = runTest(mainDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.BuildMyMap)
        advanceUntilIdle()

        assertEquals(OnboardingDestination.PROFILE_SETUP, viewModel.uiState.value.completion?.destination)

        viewModel.onFinishHandled()
        assertNull(viewModel.uiState.value.completion)
    }

    private fun createViewModel(): OnBoardingViewModel = OnBoardingViewModel(
        userPreferences = userPreferences,
        eligibilityEvaluator = EligibilityEvaluator(),
    )
}
