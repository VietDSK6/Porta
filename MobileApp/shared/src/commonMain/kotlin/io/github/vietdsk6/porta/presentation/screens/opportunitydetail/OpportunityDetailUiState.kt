package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.RuleResult

data class OpportunityDetailUiState(
    val id: String = "",
    val title: String = "",
    val provider: String = "",
    val stateLabel: String = "",
    val conclusionSummary: String = "",
    val deadlineLabel: String = "",
    val checkedLabel: String = "",
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val disclaimer: String = "",
    val rules: List<RuleReceiptUiState> = emptyList(),
    val softCriteria: List<String> = emptyList(),
    val relatedActionId: String? = null,
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

sealed interface OpportunityDetailUiEvent {
    data object Acknowledged : OpportunityDetailUiEvent
}
