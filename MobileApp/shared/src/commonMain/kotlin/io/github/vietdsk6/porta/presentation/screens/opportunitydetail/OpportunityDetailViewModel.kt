package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpportunityDetailViewModel(
    private val opportunityId: String,
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        buildState(
            profile = PortaSampleData.profile,
            isSampleProfile = true,
        ),
    )
    val uiState: StateFlow<OpportunityDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getProfile().onSuccess { profile ->
                if (profile.facts.isNotEmpty()) {
                    _uiState.value = buildState(profile = profile, isSampleProfile = false)
                }
            }
        }
    }

    fun onUiEvent(event: OpportunityDetailUiEvent) {
        when (event) {
            OpportunityDetailUiEvent.Acknowledged -> Unit
        }
    }

    private fun buildState(
        profile: ProfileSnapshot,
        isSampleProfile: Boolean,
    ): OpportunityDetailUiState = PortaSampleData.opportunities
        .firstOrNull { it.id == opportunityId }
        ?.let { opportunity ->
            val receipt = eligibilityEvaluator.evaluate(
                profile,
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
                isSampleProfile = isSampleProfile,
            )
        } ?: OpportunityDetailUiState(error = "This opportunity is not in the active sample pack.")

    private fun Deadline.label(): String = when (this) {
        is Deadline.Known -> "Deadline $date"
        Deadline.Missing -> "Deadline missing"
        Deadline.Tba -> "Deadline TBA"
    }
}
