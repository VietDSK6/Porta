package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCycleStatus
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.components.displayCopy
import io.github.vietdsk6.porta.presentation.components.sourceReviewLabel
import io.github.vietdsk6.porta.presentation.components.sourceReviewNeedsAttention
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

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
            today = currentLocalDateProvider.today(),
        ),
    )
    val uiState: StateFlow<OpportunityDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
                currentLocalDateProvider.currentDate,
            ) { profile, packResult, today -> Triple(profile, packResult.getOrNull(), today) }
                .collect { (profile, pack, today) ->
                    val isSampleProfile = profile.facts.isEmpty()
                    _uiState.value = buildState(
                        profile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile,
                        opportunities = pack?.opportunities ?: PortaSampleData.opportunities,
                        isSampleProfile = isSampleProfile,
                        isSampleCatalog = pack == null ||
                            pack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID,
                        today = today,
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
        today: LocalDate,
    ): OpportunityDetailUiState = opportunities
        .firstOrNull { it.id == opportunityId }
        ?.let { opportunity ->
            val receipt = eligibilityEvaluator.evaluate(
                profile,
                opportunity,
                today,
            )
            val displayCopy = receipt.displayCopy()
            val relatedAction = actionFrontierEngine.relevantActionFor(
                profile = profile,
                opportunity = opportunity,
                today = today,
            )
            val informationTasks = receipt.rules.mapNotNull { rule ->
                if (rule.result != RuleResult.UNKNOWN || rule.factKey == null) return@mapNotNull null
                val target = rule.actionTemplateId
                    ?.let { actionId -> opportunity.actionTemplates.firstOrNull { it.id == actionId } }
                    ?.target
                val destination = if (target is ActionTarget.OpportunityRuleTarget) {
                    InformationTaskDestination.OFFICIAL_SOURCE
                } else {
                    InformationTaskDestination.ACADEMIC_PROFILE
                }
                InformationTaskUiState(
                    id = rule.ruleId,
                    factKey = rule.factKey,
                    title = rule.factKey.informationTaskTitle(),
                    question = rule.explanation,
                    sourceNote = rule.sourceNote.orEmpty(),
                    destination = destination,
                )
            }
            OpportunityDetailUiState(
                id = opportunity.id,
                title = opportunity.title,
                provider = opportunity.provider,
                state = receipt.state,
                rootResult = receipt.rootResult,
                stateLabel = displayCopy.statusLabel.uppercase(),
                conclusionSummary = displayCopy.shortReason,
                timingLabel = displayCopy.timingLabel,
                deadlineLabel = opportunity.deadline.label(),
                checkedLabel = opportunity.sourceReviewLabel(today),
                sourceReviewNeedsAttention = opportunity.sourceReviewNeedsAttention(today),
                isArchived = receipt.cycleStatus == OpportunityCycleStatus.ARCHIVED,
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
                        shortTitle = rule.factKey?.let { "${it.name.lowercase().replace('_', ' ')} check" }
                            ?: "Programme condition",
                    )
                },
                softCriteria = opportunity.softCriteria,
                relatedActionId = relatedAction?.id,
                relatedActionTitle = relatedAction?.title.orEmpty(),
                nextStepGuidance = when {
                    relatedAction != null ->
                        "This milestone addresses an unmet encoded check. Preview it before changing your saved profile."

                    informationTasks.isNotEmpty() ->
                        "Porta needs facts from you before it can suggest a legitimate milestone. Adding information is not progress by itself."

                    receipt.rules.any { it.result == RuleResult.FAIL && it.actionTemplateId != null } ->
                        "A linked milestone does not fit the published timing. Recheck the official cycle before planning it."

                    receipt.rules.any {
                        it.result == RuleResult.UNKNOWN || it.result == RuleResult.REVIEW
                    } -> "No safe profile change can be inferred yet. Verify the questions below with the official source."

                    else -> "Porta has no evidence-backed profile change to suggest for this door."
                },
                informationTasks = informationTasks,
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

    private fun FactKey.informationTaskTitle(): String = when (this) {
        FactKey.DEGREE_COMPLETED -> "Add your degree completion status"
        FactKey.ENROLLED -> "Add your current enrollment status"
        FactKey.EXPERIENCE_MONTHS -> "Add your relevant experience duration"
        FactKey.DATE_OF_BIRTH -> "Add your date of birth"
        else -> "Add your ${name.lowercase().replace('_', ' ')} information"
    }
}
