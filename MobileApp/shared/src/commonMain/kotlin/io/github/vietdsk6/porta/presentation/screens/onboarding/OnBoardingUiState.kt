package io.github.vietdsk6.porta.presentation.screens.onboarding

import io.github.vietdsk6.porta.domain.model.OpportunityCategory

data class OnBoardingUiState(
    val currentStep: Int = 0,
    val selectedGoal: OnboardingGoal? = null,
    val completion: OnboardingCompletion? = null,
    val isLoading: Boolean = true,
    val isInstallingDemoProfile: Boolean = false,
    val demoProfileError: OnboardingDemoError? = null,
)

enum class OnboardingDemoError { EXISTING_PROFILE, INSTALL_FAILED }

enum class OnboardingGoal(
    val storageValue: String,
    val category: OpportunityCategory,
) {
    SCHOLARSHIPS("scholarships", OpportunityCategory.SCHOLARSHIP),
    INTERNSHIPS("internships", OpportunityCategory.INTERNSHIP),
    EXCHANGES("exchanges", OpportunityCategory.ACADEMIC_PROGRAM),
    COMPETITIONS(
        "competitions",
        OpportunityCategory.STUDENT_CONTEST,
    ),
    ;

    companion object {
        val selectableEntries: List<OnboardingGoal> = listOf(SCHOLARSHIPS, INTERNSHIPS, EXCHANGES)

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

    data object ExploreSample : OnBoardingUiEvent

    data object UseDemoProfile : OnBoardingUiEvent

    data object BuildMyMap : OnBoardingUiEvent

    data object SkipDemo : OnBoardingUiEvent
}
