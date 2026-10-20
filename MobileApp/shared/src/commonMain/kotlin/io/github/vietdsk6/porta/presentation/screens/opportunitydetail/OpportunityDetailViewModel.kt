package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.lifecycle.ViewModel
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OpportunityDetailViewModel(
    opportunityId: String,
    eligibilityEvaluator: EligibilityEvaluator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PortaSampleData.opportunities.firstOrNull { it.id == opportunityId }?.let { opportunity ->
            val receipt = eligibilityEvaluator.evaluate(
                PortaSampleData.profile,
                opportunity,
                PortaSampleData.referenceDate,
            )
            OpportunityDetailUiState(
                id = opportunity.id,
                title = opportunity.title,
                provider = opportunity.provider,
                stateLabel = receipt.state.name.replace('_', ' '),
                deadlineLabel = opportunity.deadline.label(),
                checkedLabel = "Checked ${opportunity.lastCheckedOn}",
                sourceUrl = opportunity.sourceUrl,
                disclaimer = opportunity.disclaimer,
                rules = receipt.rules.map { rule ->
                    RuleReceiptUiState(
                        id = rule.ruleId,
                        result = rule.result,
                        explanation = rule.explanation,
                        actualLabel = rule.actual?.displayValue() ?: "Unknown",
                        expectedLabel = rule.expected?.displayValue() ?: "Review source",
                        sourceNote = rule.sourceNote.orEmpty(),
                    )
                },
                relatedActionId = receipt.rules.firstNotNullOfOrNull { it.actionTemplateId },
            )
        } ?: OpportunityDetailUiState(error = "This opportunity is not in the active sample pack."),
    )
    val uiState: StateFlow<OpportunityDetailUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: OpportunityDetailUiEvent) {
        when (event) {
            OpportunityDetailUiEvent.Acknowledged -> Unit
        }
    }

    private fun Deadline.label(): String = when (this) {
        is Deadline.Known -> "Deadline $date"
        Deadline.Missing -> "Deadline missing"
        Deadline.Tba -> "Deadline TBA"
    }
}
