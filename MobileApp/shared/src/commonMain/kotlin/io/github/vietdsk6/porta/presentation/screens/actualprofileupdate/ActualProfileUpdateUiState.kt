package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import io.github.vietdsk6.porta.util.file.SelectedEvidenceFile

data class ActualProfileUpdateUiState(
    val isLoading: Boolean = true,
    val hasCompletedPlan: Boolean = false,
    val planTitle: String = "",
    val factLabel: String = "",
    val scopeDescription: String? = null,
    val currentValueLabel: String = "Not recorded",
    val plannedTargetLabel: String = "",
    val inputKind: ActualValueInputKind = ActualValueInputKind.NUMBER,
    val achievedValue: String = "",
    val achievedBoolean: Boolean? = null,
    val unitLabel: String = "",
    val includeEvidence: Boolean = false,
    val isEvidenceRequired: Boolean = false,
    val evidenceLabel: String = "",
    val evidenceReference: String = "",
    val evidenceAttachment: EvidenceAttachmentUiState? = null,
    val evidenceAttachmentError: String? = null,
    val showEvidenceLabelError: Boolean = false,
    val scopeWarning: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val achievedValueError: String? = null,
) {
    val hasActualResult: Boolean
        get() = when (inputKind) {
            ActualValueInputKind.BOOLEAN -> achievedBoolean != null

            ActualValueInputKind.NUMBER,
            ActualValueInputKind.ENUM,
            ActualValueInputKind.ENUM_LIST,
            ActualValueInputKind.TEXT,
            ActualValueInputKind.DATE,
            ActualValueInputKind.DURATION,
            -> achievedValue.isNotBlank()
        }

    val hasSupportingEvidence: Boolean
        get() = evidenceLabel.isNotBlank() || evidenceAttachment != null

    val requiresSupportingEvidence: Boolean
        get() = isEvidenceRequired && when (inputKind) {
            ActualValueInputKind.BOOLEAN -> achievedBoolean == true
            else -> true
        }

    val showEvidenceInputs: Boolean
        get() = includeEvidence &&
            (!isEvidenceRequired || inputKind != ActualValueInputKind.BOOLEAN || achievedBoolean == true)

    val confirmationGuidance: String?
        get() = when {
            scopeWarning != null -> "Review the current programme source to continue"

            !hasActualResult -> if (inputKind == ActualValueInputKind.BOOLEAN) {
                "Select Yes or No to continue"
            } else {
                "Enter your actual result to continue"
            }

            achievedValueError != null -> "Fix the actual result to continue"

            requiresSupportingEvidence && (!includeEvidence || !hasSupportingEvidence) ->
                "Add an evidence title or supporting file to continue"

            includeEvidence && !hasSupportingEvidence ->
                "Add an evidence title or supporting file to continue"

            else -> null
        }

    val canConfirm: Boolean
        get() = hasCompletedPlan &&
            scopeWarning == null &&
            achievedValueError == null &&
            hasActualResult &&
            (!requiresSupportingEvidence || (includeEvidence && hasSupportingEvidence)) &&
            (!includeEvidence || hasSupportingEvidence) &&
            !isSaving
}

data class EvidenceAttachmentUiState(
    val displayName: String,
    val mimeType: String? = null,
    val byteSize: Long,
    val isStored: Boolean,
)

enum class ActualValueInputKind {
    NUMBER,
    BOOLEAN,
    ENUM,
    ENUM_LIST,
    TEXT,
    DATE,
    DURATION,
}

sealed interface ActualProfileUpdateUiEvent {
    data class AchievedValueChanged(val value: String) : ActualProfileUpdateUiEvent

    data class AchievedBooleanChanged(val value: Boolean) : ActualProfileUpdateUiEvent

    data class IncludeEvidenceChanged(val value: Boolean) : ActualProfileUpdateUiEvent

    data class EvidenceLabelChanged(val value: String) : ActualProfileUpdateUiEvent

    data class EvidenceReferenceChanged(val value: String) : ActualProfileUpdateUiEvent

    data class EvidenceFileSelected(val file: SelectedEvidenceFile) : ActualProfileUpdateUiEvent

    data class EvidenceFileSelectionFailed(val message: String) : ActualProfileUpdateUiEvent

    data object RemoveEvidenceAttachment : ActualProfileUpdateUiEvent

    data object OpenEvidenceAttachment : ActualProfileUpdateUiEvent

    data object Confirm : ActualProfileUpdateUiEvent

    data object SaveHandled : ActualProfileUpdateUiEvent
}
