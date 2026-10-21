package io.github.vietdsk6.porta.presentation.screens.studentprofile

import io.github.vietdsk6.porta.domain.model.FactKey

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val hasRealProfile: Boolean = false,
    val factCount: Int = 0,
    val sections: List<ProfileFactSectionUiState> = emptyList(),
    val isSampleCatalog: Boolean = true,
    val expandedFactKey: FactKey? = null,
    val factPendingDelete: ProfileFactUiState? = null,
    val isClearConfirmationVisible: Boolean = false,
    val message: String? = null,
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
    val provenanceLabel: String,
    val actionabilityLabel: String,
    val evidenceLabel: String?,
    val dependentDoorTitles: List<String>,
) {
    val usedByLabel: String
        get() = when (dependentDoorTitles.size) {
            0 -> "Not used by the current door pack"
            1 -> "Used by 1 door"
            else -> "Used by ${dependentDoorTitles.size} doors"
        }
}

sealed interface StudentProfileUiEvent {
    data class ToggleDependencies(val key: FactKey) : StudentProfileUiEvent

    data class DeleteFactRequested(val fact: ProfileFactUiState) : StudentProfileUiEvent

    data object DeleteFactCancelled : StudentProfileUiEvent

    data object DeleteFactConfirmed : StudentProfileUiEvent

    data object ExportMetadata : StudentProfileUiEvent

    data object ClearProfileRequested : StudentProfileUiEvent

    data object ClearProfileCancelled : StudentProfileUiEvent

    data object ClearProfileConfirmed : StudentProfileUiEvent

    data object MessageHandled : StudentProfileUiEvent
}
