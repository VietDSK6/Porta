package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

data class ActualProfileUpdateUiState(
    val isLoading: Boolean = true,
    val hasCompletedPlan: Boolean = false,
    val planTitle: String = "",
    val factLabel: String = "",
    val currentValueLabel: String = "Not recorded",
    val plannedTargetLabel: String = "",
    val inputKind: ActualValueInputKind = ActualValueInputKind.NUMBER,
    val achievedValue: String = "",
    val achievedBoolean: Boolean? = null,
    val unitLabel: String = "",
    val includeEvidence: Boolean = false,
    val evidenceLabel: String = "",
    val evidenceReference: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val achievedValueError: String? = null,
) {
    val canConfirm: Boolean
        get() = hasCompletedPlan &&
            achievedValueError == null &&
            when (inputKind) {
                ActualValueInputKind.BOOLEAN -> achievedBoolean != null

                ActualValueInputKind.NUMBER,
                ActualValueInputKind.TEXT,
                -> achievedValue.isNotBlank()
            } &&
            (!includeEvidence || evidenceLabel.isNotBlank()) &&
            !isSaving
}

enum class ActualValueInputKind {
    NUMBER,
    BOOLEAN,
    TEXT,
}

sealed interface ActualProfileUpdateUiEvent {
    data class AchievedValueChanged(val value: String) : ActualProfileUpdateUiEvent

    data class AchievedBooleanChanged(val value: Boolean) : ActualProfileUpdateUiEvent

    data class IncludeEvidenceChanged(val value: Boolean) : ActualProfileUpdateUiEvent

    data class EvidenceLabelChanged(val value: String) : ActualProfileUpdateUiEvent

    data class EvidenceReferenceChanged(val value: String) : ActualProfileUpdateUiEvent

    data object Confirm : ActualProfileUpdateUiEvent

    data object SaveHandled : ActualProfileUpdateUiEvent
}
