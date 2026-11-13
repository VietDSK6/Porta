package io.github.vietdsk6.porta.presentation.screens.onboarding

data class OnBoardingUiState(
    val currentStep: Int = 0,
    val thresholdLesson: OnboardingThresholdLessonUiState? = null,
    val isThresholdPreviewApplied: Boolean = false,
    val completion: OnboardingCompletion? = null,
    val isLoading: Boolean = true,
    val isInstallingDemoProfile: Boolean = false,
    val demoProfileError: OnboardingDemoError? = null,
)

data class OnboardingThresholdLessonUiState(
    val beforeScore: Double,
    val afterScore: Double,
    val changedOpportunityTitles: List<String>,
    val unchangedOpportunityTitles: List<String>,
)

enum class OnboardingDemoError { EXISTING_PROFILE, INSTALL_FAILED }

enum class OnboardingDestination {
    SAMPLE_MAP,
    PROFILE_SETUP,
}

data class OnboardingCompletion(
    val destination: OnboardingDestination,
    val isNewUser: Boolean,
)

sealed interface OnBoardingUiEvent {
    data object Next : OnBoardingUiEvent

    data object Back : OnBoardingUiEvent

    data object ThresholdPreviewToggled : OnBoardingUiEvent

    data object ExploreSample : OnBoardingUiEvent

    data object UseDemoProfile : OnBoardingUiEvent

    data object BuildMyMap : OnBoardingUiEvent

    data object SkipDemo : OnBoardingUiEvent
}
