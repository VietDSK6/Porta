package io.github.vietdsk6.porta.presentation.screens.packinspector

data class PackInspectorUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val activePack: ActivePackUiState? = null,
    val selectedOpportunityId: String? = null,
    val diagnostics: List<PackDiagnosticUiState> = emptyList(),
    val pendingUpdate: PendingPackUpdateUiState? = null,
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
    val sourceReviewRangeLabel: String,
    val importedLabel: String,
    val sourceUrl: String?,
    val isBundledPack: Boolean,
    val providers: List<String>,
    val opportunities: List<PackOpportunityUiState>,
    val sourceReviewStatusLabel: String = "",
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
    val sourceReviewNeedsAttention: Boolean = false,
)

data class PendingPackUpdateUiState(
    val fileName: String,
    val packName: String,
    val versionTransitionLabel: String,
    val addedCount: Int,
    val removedCount: Int,
    val changedCount: Int,
    val unchangedCount: Int,
    val staleAssertionCount: Int,
    val sourceReviewDueCount: Int,
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

    data object ImportPreviewCancelled : PackInspectorUiEvent

    data object ImportPreviewConfirmed : PackInspectorUiEvent

    data object ExportActivePack : PackInspectorUiEvent

    data object ExportDiagnostics : PackInspectorUiEvent

    data object RestoreBundledRequested : PackInspectorUiEvent

    data object MessageHandled : PackInspectorUiEvent
}
