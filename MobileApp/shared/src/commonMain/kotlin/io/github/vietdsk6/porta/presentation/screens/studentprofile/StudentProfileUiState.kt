package io.github.vietdsk6.porta.presentation.screens.studentprofile

import io.github.vietdsk6.porta.domain.model.FactKey

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val hasRealProfile: Boolean = false,
    val isDemoProfile: Boolean = false,
    val factCount: Int = 0,
    val catalogOpportunityCount: Int = 0,
    val dependentOpportunityCount: Int = 0,
    val sections: List<ProfileFactSectionUiState> = emptyList(),
    val uncheckedFacts: List<UncheckedProfileFactUiState> = emptyList(),
    val isSampleCatalog: Boolean = true,
    val expandedFactKey: FactKey? = null,
    val factPendingDelete: ProfileFactUiState? = null,
    val pendingDataAction: ProfileDataAction? = null,
    val supportsProfileImport: Boolean = false,
    val pendingProfileImport: ProfileImportPreviewUiState? = null,
    val profileImportError: String? = null,
    val message: String? = null,
)

data class ProfileImportPreviewUiState(
    val fileName: String,
    val facts: List<ProfileImportFactUiState>,
    val ruleAssertionCount: Int,
    val evidenceMetadataCount: Int,
    val sensitiveFactCount: Int,
)

data class ProfileImportFactUiState(
    val key: FactKey,
    val label: String,
    val value: String,
    val provenanceLabel: String,
    val hasEvidenceMetadata: Boolean,
    val isSensitive: Boolean,
)

enum class ProfileDataAction {
    CLEAR_PROFILE_AND_PLANS,
    RESET_ALL_LOCAL_DATA,
}

data class UncheckedProfileFactUiState(
    val key: FactKey,
    val label: String,
    val dependentDoorCount: Int,
)

data class ProfileFactSectionUiState(
    val title: String,
    val description: String,
    val facts: List<ProfileFactUiState>,
)

data class ProfileFactUiState(
    val key: FactKey,
    val label: String,
    val value: String,
    val detail: String? = null,
    val isProvided: Boolean = true,
    val isGrowthEvidence: Boolean = false,
    val provenanceLabel: String,
    val actionabilityLabel: String,
    val evidenceLabel: String?,
    val dependentDoorTitles: List<String>,
    val evidenceAttachmentName: String? = null,
    val evidenceAttachmentDetail: String? = null,
    val canOpenEvidenceAttachment: Boolean = false,
) {
    val usedByLabel: String
        get() = when (dependentDoorTitles.size) {
            0 -> "No current pack rule references this detail"
            1 -> "Referenced by 1 opportunity"
            else -> "Referenced by ${dependentDoorTitles.size} opportunities"
        }
}

sealed interface StudentProfileUiEvent {
    data class ToggleDependencies(val key: FactKey) : StudentProfileUiEvent

    data class DeleteFactRequested(val fact: ProfileFactUiState) : StudentProfileUiEvent

    data class OpenEvidenceAttachment(val key: FactKey) : StudentProfileUiEvent

    data object DeleteFactCancelled : StudentProfileUiEvent

    data object DeleteFactConfirmed : StudentProfileUiEvent

    data object ExportMetadata : StudentProfileUiEvent

    data class ImportProfileSelected(
        val fileName: String,
        val encodedProfile: String,
    ) : StudentProfileUiEvent

    data object ImportProfileReadFailed : StudentProfileUiEvent

    data object ImportProfileCancelled : StudentProfileUiEvent

    data object ImportProfileConfirmed : StudentProfileUiEvent

    data class DataActionRequested(val action: ProfileDataAction) : StudentProfileUiEvent

    data object DataActionCancelled : StudentProfileUiEvent

    data object DataActionConfirmed : StudentProfileUiEvent

    data object MessageHandled : StudentProfileUiEvent
}
