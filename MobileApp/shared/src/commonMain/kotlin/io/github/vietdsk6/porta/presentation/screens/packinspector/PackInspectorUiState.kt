package io.github.vietdsk6.porta.presentation.screens.packinspector

import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import kotlinx.datetime.LocalDate

data class PackInspectorUiState(
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val activePack: ActivePackUiState? = null,
    val selectedOpportunityId: String? = null,
    val diagnostics: List<PackDiagnosticUiState> = emptyList(),
    val pendingUpdate: PendingPackUpdateUiState? = null,
    val message: PackInspectorNotice? = null,
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
    val version: Int? = null,
    val schemaVersion: Int? = null,
    val recordCount: Int? = null,
    val packCheckedOn: LocalDate? = null,
    val earliestRecordCheckedOn: LocalDate? = null,
    val latestRecordCheckedOn: LocalDate? = null,
    val sourceReviewDueCount: Int? = null,
    val importedAtEpochMillis: Long? = null,
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
    val category: OpportunityCategory? = null,
    val lastCheckedOn: LocalDate? = null,
    val sourceFreshness: SourceFreshness? = null,
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
    val previousVersion: Int? = null,
    val candidateVersion: Int? = null,
)

enum class PackRuleKind { ALL, ANY, ATOMIC, MANUAL_CHECK }

data class RuleNodeUiState(
    val id: String,
    val depth: Int,
    val typeLabel: String,
    val summary: String,
    val sourceNote: String? = null,
    val kind: PackRuleKind? = null,
    val factKey: FactKey? = null,
    val operator: Operator? = null,
    val expectedDisplayValue: String? = null,
    val childRuleCount: Int? = null,
)

sealed interface PackInspectorNotice {
    data class MatchesActive(val fileName: String) : PackInspectorNotice
    data class Activated(val fileName: String, val staleAssertionCount: Int) : PackInspectorNotice
    data object ImportReadFailed : PackInspectorNotice
    data object ImportRejected : PackInspectorNotice
    data object ExportedPack : PackInspectorNotice
    data object ExportedDiagnostics : PackInspectorNotice
    data object ExportPackFailed : PackInspectorNotice
    data object SaveFailed : PackInspectorNotice
    data object LoadFailed : PackInspectorNotice
}

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
