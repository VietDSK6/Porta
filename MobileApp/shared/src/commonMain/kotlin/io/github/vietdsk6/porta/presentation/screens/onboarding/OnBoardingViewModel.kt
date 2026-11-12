package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.scenario.SampleScenarioProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.logging.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val userPreferences: UserPreferences,
    private val profileRepository: ProfileRepository? = null,
    private val savedOpportunityRepository: SavedOpportunityRepository? = null,
    private val sampleScenarioProvider: SampleScenarioProvider = SampleScenarioProvider(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnBoardingUiState())
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        restoreOnboardingState()
    }

    fun onUiEvent(event: OnBoardingUiEvent) = viewModelScope.launch {
        when (event) {
            is OnBoardingUiEvent.GoalSelected -> {
                userPreferences.putString(UserPreferences.KEY_OPPORTUNITY_GOAL, event.goal.storageValue)
                _uiState.update { state ->
                    state.copy(selectedGoal = event.goal)
                }
            }

            OnBoardingUiEvent.Next -> _uiState.update { state ->
                if (state.currentStep == 0 && state.selectedGoal != null) {
                    state.copy(currentStep = (state.currentStep + 1).coerceAtMost(LAST_STEP))
                } else {
                    state
                }
            }

            OnBoardingUiEvent.Back -> _uiState.update { state ->
                state.copy(currentStep = (state.currentStep - 1).coerceAtLeast(0))
            }

            OnBoardingUiEvent.ExploreSample,
            OnBoardingUiEvent.SkipDemo,
            -> complete(OnboardingDestination.SAMPLE_MAP)

            OnBoardingUiEvent.UseDemoProfile -> installDemoProfile()

            OnBoardingUiEvent.BuildMyMap -> complete(OnboardingDestination.PROFILE_SETUP)
        }
    }

    fun onFinishHandled() = _uiState.update { it.copy(completion = null) }

    private suspend fun complete(destination: OnboardingDestination) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        _uiState.update {
            it.copy(
                completion = OnboardingCompletion(destination = destination, isNewUser = true),
                isInstallingDemoProfile = false,
                demoProfileError = null,
            )
        }
    }

    private suspend fun installDemoProfile() {
        _uiState.update { state ->
            state.copy(isInstallingDemoProfile = true, demoProfileError = null)
        }
        val repository = profileRepository
        if (repository == null) {
            showDemoProfileError()
            return
        }
        val currentProfile = repository.getProfile().getOrElse {
            showDemoProfileError()
            return
        }
        if (currentProfile.facts.isNotEmpty()) {
            if (sampleScenarioProvider.isPersistedDemoProfile(currentProfile)) {
                configureJudgeDemo()
                complete(OnboardingDestination.SAMPLE_MAP)
            } else {
                _uiState.update { state ->
                    state.copy(
                        isInstallingDemoProfile = false,
                        demoProfileError = OnboardingDemoError.EXISTING_PROFILE,
                    )
                }
            }
            return
        }
        repository.saveFacts(sampleScenarioProvider.persistedDemoFacts(nowEpochMillis()))
            .onSuccess {
                configureJudgeDemo()
                complete(OnboardingDestination.SAMPLE_MAP)
            }
            .onFailure { showDemoProfileError() }
    }

    private suspend fun configureJudgeDemo() {
        runCatching {
            userPreferences.putInt(
                UserPreferences.KEY_FRONTIER_HORIZON_DAYS,
                sampleScenarioProvider.judgeDemoHorizonDays,
            )
            userPreferences.putBoolean(UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED, true)
            sampleScenarioProvider.judgeDemoSavedOpportunityIds.forEach { opportunityId ->
                savedOpportunityRepository?.setSaved(opportunityId, isSaved = true)?.getOrThrow()
            }
        }.onFailure { throwable ->
            runCatching { AppLogger.e("Could not apply optional judge-demo planning preferences", throwable) }
        }
    }

    private fun showDemoProfileError() {
        _uiState.update { state ->
            state.copy(
                isInstallingDemoProfile = false,
                demoProfileError = OnboardingDemoError.INSTALL_FAILED,
            )
        }
    }

    private fun restoreOnboardingState() = viewModelScope.launch {
        try {
            val selectedGoal = OnboardingGoal.fromStorageValue(
                userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL),
            )?.takeIf(OnboardingGoal.selectableEntries::contains)
            if (userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN)) {
                _uiState.update {
                    it.copy(
                        selectedGoal = selectedGoal,
                        completion = OnboardingCompletion(
                            destination = OnboardingDestination.SAMPLE_MAP,
                            isNewUser = false,
                        ),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        selectedGoal = selectedGoal,
                        isLoading = false,
                    )
                }
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            // Logging must not become a second startup failure (for example, in an isolated test
            // where the global Koin-backed logger has not been initialized yet).
            runCatching { AppLogger.e("Could not restore onboarding state", throwable) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private companion object {
        const val LAST_STEP = 1
    }
}
