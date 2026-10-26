package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleResult

data class OpportunityDetailUiState(
    val id: String = "",
    val title: String = "",
    val provider: String = "",
    val state: EligibilityState? = null,
    val stateLabel: String = "",
    val conclusionSummary: String = "",
    val timingLabel: String = "",
    val deadlineLabel: String = "",
    val checkedLabel: String = "",
    val sourceReviewNeedsAttention: Boolean = false,
    val isArchived: Boolean = false,
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val disclaimer: String = "",
    val rules: List<RuleReceiptUiState> = emptyList(),
    val softCriteria: List<String> = emptyList(),
    val relatedActionId: String? = null,
    val relatedActionTitle: String = "",
    val nextStepGuidance: String = "",
    val informationTasks: List<InformationTaskUiState> = emptyList(),
    val canOpenSource: Boolean = false,
    val isSampleProfile: Boolean = true,
    val isSampleCatalog: Boolean = true,
    val error: String? = null,
)

data class RuleReceiptUiState(
    val id: String,
    val result: RuleResult,
    val explanation: String,
    val actualLabel: String,
    val expectedLabel: String,
    val sourceNote: String,
)

data class InformationTaskUiState(
    val id: String,
    val factKey: FactKey,
    val title: String,
    val question: String,
    val sourceNote: String,
    val destination: InformationTaskDestination,
)

enum class InformationTaskDestination {
    ACADEMIC_PROFILE,
    OFFICIAL_SOURCE,
}

sealed interface OpportunityDetailUiEvent {
    data object Acknowledged : OpportunityDetailUiEvent
}
