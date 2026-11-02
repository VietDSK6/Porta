package io.github.vietdsk6.porta.presentation.screens.onboarding

import io.github.vietdsk6.porta.domain.model.OpportunityCategory

data class OnBoardingUiState(
    val currentStep: Int = 0,
    val selectedGoal: OnboardingGoal? = null,
    val sampleIelts: Double = 6.5,
    val demoDoors: List<OnboardingDemoDoorUiState> = emptyList(),
    val isReceiptExpanded: Boolean = false,
    val completion: OnboardingCompletion? = null,
    val isLoading: Boolean = true,
) {
    /** Demo doors are filtered to checks that fail at 6.5 and pass at 7.0. */
    val satisfiedIeltsRequirementCount: Int
        get() = demoDoors.count(OnboardingDemoDoorUiState::isRequirementMet)

    val isDemoComplete: Boolean
        get() = demoDoors.isNotEmpty() && satisfiedIeltsRequirementCount == demoDoors.size
}

internal fun OnBoardingUiState.sampleRuleSummary(): String {
    if (demoDoors.isEmpty()) return "No fictional IELTS checks are available."
    val thresholds = demoDoors.map(OnboardingDemoDoorUiState::requiredIelts).distinct()
    val rule = if (thresholds.size == 1) {
        "one IELTS ≥ ${thresholds.single().asBand()} check each"
    } else {
        "one IELTS check each"
    }
    return "Fictional examples: $rule. Other conditions are not assessed."
}

private fun Double.asBand(): String = "${toInt()}.${((this * 10).toInt() % 10)}"

data class OnboardingDemoDoorUiState(
    val opportunityId: String,
    val title: String,
    val category: OpportunityCategory,
    val requiredIelts: Double,
    val isRequirementMet: Boolean,
)

enum class OnboardingGoal(
    val storageValue: String,
    val label: String,
    val description: String,
    val category: OpportunityCategory,
) {
    SCHOLARSHIPS(
        storageValue = "scholarships",
        label = "Scholarships",
        description = "Fund your education",
        category = OpportunityCategory.SCHOLARSHIP,
    ),
    INTERNSHIPS(
        storageValue = "internships",
        label = "Internships",
        description = "Gain real-world experience",
        category = OpportunityCategory.INTERNSHIP,
    ),
    EXCHANGES(
        storageValue = "exchanges",
        label = "Exchange programs",
        description = "Study somewhere new",
        category = OpportunityCategory.ACADEMIC_PROGRAM,
    ),
    COMPETITIONS(
        storageValue = "competitions",
        label = "Student competitions",
        description = "Challenges where projects and teamwork create proof",
        category = OpportunityCategory.STUDENT_CONTEST,
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

    data class SampleIeltsChanged(val value: Double) : OnBoardingUiEvent

    data object ResetDemo : OnBoardingUiEvent

    data object ReceiptToggled : OnBoardingUiEvent

    data object ExploreSample : OnBoardingUiEvent

    data object BuildMyMap : OnBoardingUiEvent

    data object SkipDemo : OnBoardingUiEvent
}
