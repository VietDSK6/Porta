package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.RuleResult

data class OpportunityDetailUiState(
    val id: String = "",
    val title: String = "",
    val provider: String = "",
    val stateLabel: String = "",
    val deadlineLabel: String = "",
    val checkedLabel: String = "",
    val sourceUrl: String = "",
    val disclaimer: String = "",
    val rules: List<RuleReceiptUiState> = emptyList(),
    val relatedActionId: String? = null,
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
