package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import io.github.vietdsk6.porta.domain.model.FactKey
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
    val errorCode: ActualCopyCode? = null,
    val evidenceAttachmentErrorCode: ActualCopyCode? = null,
    val scopeWarningCode: ActualCopyCode? = null,
    val achievedValueErrorCode: ActualValidationCode? = null,
    val validationScale: String? = null,
    val factKey: FactKey? = null,
    val isProgrammeSpecific: Boolean = false,
    val currentValueMissing: Boolean = false,
    val scopeProgrammeName: String? = null,
    val scopeRequirement: String? = null,
    val scopeRequirementMissing: Boolean = false,
) {
    val confirmationGuidanceCode: ConfirmationGuidance?
        get() = when {
            scopeWarning != null -> ConfirmationGuidance.REVIEW_SOURCE
            !hasActualResult && inputKind == ActualValueInputKind.BOOLEAN -> ConfirmationGuidance.SELECT_BOOLEAN
            !hasActualResult -> ConfirmationGuidance.ENTER_RESULT
            achievedValueError != null -> ConfirmationGuidance.FIX_RESULT
            requiresSupportingEvidence && (!includeEvidence || !hasSupportingEvidence) -> ConfirmationGuidance.ADD_EVIDENCE
            includeEvidence && !hasSupportingEvidence -> ConfirmationGuidance.ADD_EVIDENCE
            else -> null
        }

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
        get() = when (confirmationGuidanceCode) {
            ConfirmationGuidance.REVIEW_SOURCE -> "Review the current programme source to continue"
            ConfirmationGuidance.SELECT_BOOLEAN -> "Select Yes or No to continue"
            ConfirmationGuidance.ENTER_RESULT -> "Enter your actual result to continue"
            ConfirmationGuidance.FIX_RESULT -> "Fix the actual result to continue"
            ConfirmationGuidance.ADD_EVIDENCE -> "Add an evidence title or supporting file to continue"
            null -> null
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

enum class ConfirmationGuidance { REVIEW_SOURCE, SELECT_BOOLEAN, ENTER_RESULT, FIX_RESULT, ADD_EVIDENCE }

enum class ActualCopyCode {
    PLAN_CHANGED,
    RULE_CHANGED,
    INVALID_VALUE,
    PROFILE_READ_FAILED,
    FILE_COPY_FAILED,
    FACT_SAVE_FAILED,
    FILE_MISSING,
    FILE_OPEN_FAILED,
    SCOPE_CHANGED,
    FILE_SELECTION_FAILED,
    FILE_NAME_REQUIRED,
    FILE_EMPTY,
    FILE_TOO_LARGE,
    FILE_UNSUPPORTED,
    FILE_MIME_MISMATCH,
}

enum class ActualValidationCode {
    ENUM_LIST,
    DATE,
    DURATION,
    NUMBER,
    GPA_SCALE,
    IELTS_RANGE,
    EXPERIENCE_MONTHS,
    NONNEGATIVE,
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
