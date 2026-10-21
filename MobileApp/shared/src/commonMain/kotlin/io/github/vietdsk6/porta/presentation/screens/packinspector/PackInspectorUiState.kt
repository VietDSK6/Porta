package io.github.vietdsk6.porta.presentation.screens.packinspector

data class PackInspectorUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val activePack: ActivePackUiState? = null,
    val selectedOpportunityId: String? = null,
    val diagnostics: List<PackDiagnosticUiState> = emptyList(),
    val isRestoreConfirmationVisible: Boolean = false,
    val message: String? = null,
)

data class ActivePackUiState(
    val id: String,
    val name: String,
    val publisher: String,
    val versionLabel: String,
    val schemaLabel: String,
    val recordCountLabel: String,
    val checkedLabel: String,
    val verificationRangeLabel: String,
    val importedLabel: String,
    val sourceUrl: String?,
    val isBundledPack: Boolean,
    val providers: List<String>,
    val opportunities: List<PackOpportunityUiState>,
)

data class PackOpportunityUiState(
    val id: String,
    val title: String,
    val provider: String,
    val categoryLabel: String,
    val checkedLabel: String,
    val sourceUrl: String,
    val sourceSection: String?,
    val ruleNodes: List<RuleNodeUiState>,
)

data class RuleNodeUiState(
    val id: String,
    val depth: Int,
    val typeLabel: String,
    val summary: String,
    val sourceNote: String? = null,
)

data class PackDiagnosticUiState(
    val code: String,
    val path: String,
    val message: String,
    val severity: PackDiagnosticSeverityUi,
)

enum class PackDiagnosticSeverityUi {
    ERROR,
    WARNING,
}

sealed interface PackInspectorUiEvent {
    data class OpportunitySelected(val opportunityId: String) : PackInspectorUiEvent

    data class ImportPack(val fileName: String, val rawJson: String) : PackInspectorUiEvent

    data object ImportReadFailed : PackInspectorUiEvent

    data object ExportActivePack : PackInspectorUiEvent

    data object ExportDiagnostics : PackInspectorUiEvent

    data object RestoreBundledRequested : PackInspectorUiEvent

    data object RestoreBundledCancelled : PackInspectorUiEvent

    data object RestoreBundledConfirmed : PackInspectorUiEvent

    data object MessageHandled : PackInspectorUiEvent
}
