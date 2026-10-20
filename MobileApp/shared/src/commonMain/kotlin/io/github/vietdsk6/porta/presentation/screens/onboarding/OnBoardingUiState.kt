package io.github.vietdsk6.porta.presentation.screens.onboarding

data class OnBoardingUiState(
    val currentStep: Int = 0,
    val selectedGoal: OnboardingGoal? = null,
    val sampleIelts: Double = 6.5,
    val isReceiptExpanded: Boolean = false,
    val completion: OnboardingCompletion? = null,
    val isLoading: Boolean = true,
)

enum class OnboardingGoal(
    val storageValue: String,
    val label: String,
    val description: String,
) {
    SCHOLARSHIPS(
        storageValue = "scholarships",
        label = "Scholarships",
        description = "Funding from schools, foundations, and organizations",
    ),
    INTERNSHIPS(
        storageValue = "internships",
        label = "Internships",
        description = "Roles that turn skills into evidence and experience",
    ),
    EXCHANGES(
        storageValue = "exchanges",
        label = "Exchange programs",
        description = "Academic and cultural programs beyond your campus",
    ),
    COMPETITIONS(
        storageValue = "competitions",
        label = "Student competitions",
        description = "Challenges where projects and teamwork create proof",
    ),
    ;

    companion object {
        fun fromStorageValue(value: String?): OnboardingGoal? = entries.firstOrNull { it.storageValue == value }
    }
}

enum class OnboardingDestination {
    SAMPLE_MAP,
    PROFILE_SETUP,
}

data class OnboardingCompletion(
    val destination: OnboardingDestination,
    val isNewUser: Boolean,
)

sealed interface OnBoardingUiEvent {
    data class GoalSelected(val goal: OnboardingGoal) : OnBoardingUiEvent

    data object Next : OnBoardingUiEvent

    data object Back : OnBoardingUiEvent

    data class SampleIeltsChanged(val value: Double) : OnBoardingUiEvent

    data object TryIeltsSeven : OnBoardingUiEvent

    data object ReceiptToggled : OnBoardingUiEvent

    data object ExploreSample : OnBoardingUiEvent

    data object BuildMyMap : OnBoardingUiEvent

    data object SkipDemo : OnBoardingUiEvent
}
