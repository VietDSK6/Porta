package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind

data class OpportunityStudioUiState(
    val isLoading: Boolean = true,
    val isSupported: Boolean = true,
    val isWorking: Boolean = false,
    val basePackName: String = "",
    val basePackVersion: Int? = null,
    val nextPackVersion: Int? = null,
    val draft: OpportunityStudioDraft? = null,
    val selectedRuleIndex: Int? = null,
    val diagnostics: List<OpportunityStudioDiagnosticUiState> = emptyList(),
    val rawJson: String? = null,
    val isJsonPreviewVisible: Boolean = false,
    val message: String? = null,
) {
    val canExport: Boolean
        get() = isSupported && !isLoading && !isWorking && rawJson != null && diagnostics.isEmpty()

    val versionTransitionLabel: String
        get() = basePackVersion?.let { current ->
            nextPackVersion?.let { next -> "Pack version $current → $next" }
        }.orEmpty()
}

data class OpportunityStudioDiagnosticUiState(
    val path: String,
    val message: String,
)

enum class OpportunityStudioMetadataField {
    OPPORTUNITY_ID,
    TITLE,
    PROVIDER,
    CYCLE,
    CYCLE_START_ON,
    SOURCE_URL,
    SOURCE_SECTION,
    LAST_CHECKED_ON,
    DEADLINE_DATE,
}

enum class OpportunityStudioRuleTextField {
    ID,
    EXPECTED_VALUE,
    EXPECTED_UNIT,
    EXPLANATION,
    SOURCE_NOTE,
}

sealed interface OpportunityStudioUiEvent {
    data class MetadataChanged(
        val field: OpportunityStudioMetadataField,
        val value: String,
    ) : OpportunityStudioUiEvent

    data class CategoryChanged(val category: OpportunityCategory) : OpportunityStudioUiEvent

    data class DeadlineKindChanged(val kind: StudioDeadlineKind) : OpportunityStudioUiEvent

    data class RootRuleKindChanged(val kind: StudioRootRuleKind) : OpportunityStudioUiEvent

    data object AddAtomicRule : OpportunityStudioUiEvent

    data object AddManualRule : OpportunityStudioUiEvent

    data class RuleSelected(val ruleIndex: Int) : OpportunityStudioUiEvent

    data class RuleRemoved(val ruleIndex: Int) : OpportunityStudioUiEvent

    data class RuleTextChanged(
        val ruleIndex: Int,
        val field: OpportunityStudioRuleTextField,
        val value: String,
    ) : OpportunityStudioUiEvent

    data class AtomicFactKeyChanged(
        val ruleIndex: Int,
        val factKey: FactKey,
    ) : OpportunityStudioUiEvent

    data class AtomicOperatorChanged(
        val ruleIndex: Int,
        val operator: Operator,
    ) : OpportunityStudioUiEvent

    data class AtomicActionabilityChanged(
        val ruleIndex: Int,
        val actionability: Actionability,
    ) : OpportunityStudioUiEvent

    data class AtomicExpectedKindChanged(
        val ruleIndex: Int,
        val kind: StudioFactValueKind,
    ) : OpportunityStudioUiEvent

    data object ToggleJsonPreview : OpportunityStudioUiEvent

    data object ExportDraft : OpportunityStudioUiEvent

    data object MessageHandled : OpportunityStudioUiEvent
}
