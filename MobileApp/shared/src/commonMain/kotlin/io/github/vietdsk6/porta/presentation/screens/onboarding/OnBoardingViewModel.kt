package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OnBoardingViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnBoardingUiState(isLoading = true))
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        restoreOnboardingState()
    }

    fun onUiEvent(event: OnBoardingUiEvent) = viewModelScope.launch {
        when (event) {
            is OnBoardingUiEvent.GoalSelected -> {
                userPreferences.putString(UserPreferences.KEY_OPPORTUNITY_GOAL, event.goal.storageValue)
                _uiState.update { it.copy(selectedGoal = event.goal) }
            }

            OnBoardingUiEvent.Next -> _uiState.update { state ->
                state.copy(currentStep = (state.currentStep + 1).coerceAtMost(LAST_STEP))
            }

            OnBoardingUiEvent.Back -> _uiState.update { state ->
                state.copy(currentStep = (state.currentStep - 1).coerceAtLeast(0))
            }

            is OnBoardingUiEvent.SampleIeltsChanged -> _uiState.update { state ->
                val halfBandValue = (event.value * 2).roundToInt() / 2.0
                state.copy(sampleIelts = halfBandValue.coerceIn(6.0, 8.0))
            }

            OnBoardingUiEvent.TryIeltsSeven -> _uiState.update { it.copy(sampleIelts = 7.0) }

            OnBoardingUiEvent.ReceiptToggled -> _uiState.update {
                it.copy(isReceiptExpanded = !it.isReceiptExpanded)
            }

            OnBoardingUiEvent.ExploreSample,
            OnBoardingUiEvent.SkipDemo,
            -> complete(OnboardingDestination.SAMPLE_MAP)

            OnBoardingUiEvent.BuildMyMap -> complete(OnboardingDestination.PROFILE_SETUP)
        }
    }

    fun onFinishHandled() = _uiState.update { it.copy(completion = null) }

    private suspend fun complete(destination: OnboardingDestination) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        _uiState.update {
            it.copy(
                completion = OnboardingCompletion(destination = destination, isNewUser = true),
            )
        }
    }

    private fun restoreOnboardingState() = viewModelScope.launch {
        val selectedGoal = OnboardingGoal.fromStorageValue(
            userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL),
        )
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
            _uiState.update { it.copy(selectedGoal = selectedGoal, isLoading = false) }
        }
    }

    private companion object {
        const val LAST_STEP = 3
    }
}
