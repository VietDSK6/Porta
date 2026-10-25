package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.components.requirementAssessmentLabel
import io.github.vietdsk6.porta.presentation.components.timingLabel
import io.github.vietdsk6.porta.presentation.components.trustSummary
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OpportunityDetailViewModel(
    private val opportunityId: String,
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val actionFrontierEngine: ActionFrontierEngine,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        buildState(
            profile = PortaSampleData.profile,
            opportunities = PortaSampleData.opportunities,
            isSampleProfile = true,
            isSampleCatalog = true,
        ),
    )
    val uiState: StateFlow<OpportunityDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
            ) { profile, packResult -> profile to packResult.getOrNull() }
                .collect { (profile, pack) ->
                    val isSampleProfile = profile.facts.isEmpty()
                    _uiState.value = buildState(
                        profile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile,
                        opportunities = pack?.opportunities ?: PortaSampleData.opportunities,
                        isSampleProfile = isSampleProfile,
                        isSampleCatalog = pack == null ||
                            pack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID,
                    )
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
        opportunities: List<Opportunity>,
        isSampleProfile: Boolean,
        isSampleCatalog: Boolean,
    ): OpportunityDetailUiState = opportunities
        .firstOrNull { it.id == opportunityId }
        ?.let { opportunity ->
            val today = currentLocalDateProvider.today()
            val receipt = eligibilityEvaluator.evaluate(
                profile,
                opportunity,
                today,
            )
            val relatedAction = actionFrontierEngine.relevantActionFor(
                profile = profile,
                opportunity = opportunity,
                today = today,
            )
            OpportunityDetailUiState(
                id = opportunity.id,
                title = opportunity.title,
                provider = opportunity.provider,
                state = receipt.state,
                stateLabel = receipt.requirementAssessmentLabel().uppercase(),
                conclusionSummary = receipt.trustSummary(),
                timingLabel = receipt.timingLabel(),
                deadlineLabel = opportunity.deadline.label(),
                checkedLabel = "Source reviewed on ${opportunity.lastCheckedOn}",
                sourceUrl = opportunity.sourceUrl,
                sourceSection = opportunity.sourceSection.orEmpty(),
                disclaimer = opportunity.disclaimer,
                rules = receipt.rules.map { rule ->
                    RuleReceiptUiState(
                        id = rule.ruleId,
                        result = rule.result,
                        explanation = rule.explanation,
                        actualLabel = rule.actual?.displayValue() ?: "Not provided",
                        expectedLabel = rule.expected?.displayValue() ?: "Manual source review",
                        sourceNote = rule.sourceNote.orEmpty(),
                    )
                },
                softCriteria = opportunity.softCriteria,
                relatedActionId = relatedAction?.id,
                relatedActionTitle = relatedAction?.title.orEmpty(),
                nextStepGuidance = when {
                    relatedAction != null ->
                        "This milestone addresses an unmet encoded check. Preview it before changing your saved profile."

                    receipt.rules.any { it.result == RuleResult.FAIL && it.actionTemplateId != null } ->
                        "A linked milestone does not fit the published timing. Recheck the official cycle before planning it."

                    receipt.rules.any {
                        it.result == RuleResult.UNKNOWN || it.result == RuleResult.REVIEW
                    } -> "No safe profile change can be inferred yet. Verify the questions below with the official source."

                    else -> "Porta has no evidence-backed profile change to suggest for this door."
                },
                canOpenSource = opportunity.sourceUrl.startsWith("https://"),
                isSampleProfile = isSampleProfile,
                isSampleCatalog = isSampleCatalog,
            )
        } ?: OpportunityDetailUiState(error = "This opportunity is no longer in the active opportunity pack.")

    private fun Deadline.label(): String = when (this) {
        is Deadline.Known -> "Deadline $date"
        Deadline.Missing -> "Deadline missing"
        Deadline.Tba -> "Deadline TBA"
    }
}
