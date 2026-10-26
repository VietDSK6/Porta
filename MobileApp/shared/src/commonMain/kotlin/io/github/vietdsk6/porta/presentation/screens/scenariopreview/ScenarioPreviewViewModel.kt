package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioImpactReceipt
import io.github.vietdsk6.porta.domain.model.asScenarioOverrides
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.isFeasibleForPlanning
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.components.trustLabel
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ScenarioPreviewViewModel(
    private val actionId: String,
    private val scenarioEvaluator: ScenarioEvaluator,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val planRepository: PlanRepository,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private var activeProfile = PortaSampleData.profile
    private var activeOpportunities: List<Opportunity> = PortaSampleData.opportunities
    private var activeDate = currentLocalDateProvider.today()
    private var isSampleProfile = true
    private val _uiState = MutableStateFlow(
        buildState(
            profile = PortaSampleData.profile,
            isSampleProfile = true,
        ),
    )
    val uiState: StateFlow<ScenarioPreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
                currentLocalDateProvider.currentDate,
            ) { profile, packResult, today -> Triple(profile, packResult.getOrNull(), today) }
                .collect { (profile, pack, today) ->
                    isSampleProfile = profile.facts.isEmpty()
                    activeProfile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile
                    activeOpportunities = pack?.opportunities ?: PortaSampleData.opportunities
                    activeDate = today
                    _uiState.value = buildState(
                        profile = activeProfile,
                        isSampleProfile = isSampleProfile,
                    )
                }
        }
    }

    fun onUiEvent(event: ScenarioPreviewUiEvent) {
        when (event) {
            ScenarioPreviewUiEvent.Acknowledged -> Unit
            ScenarioPreviewUiEvent.SavePlan -> savePlan()
            ScenarioPreviewUiEvent.SaveHandled -> _uiState.value = _uiState.value.copy(isSaved = false)
        }
    }

    private fun savePlan() {
        if (isSampleProfile) {
            _uiState.value = _uiState.value.copy(
                saveError = "Create a real local profile before saving a milestone.",
            )
            return
        }
        val opportunity = activeOpportunities.firstOrNull { candidate ->
            candidate.actionTemplates.any { it.id == actionId }
        } ?: return
        val action = opportunity.actionTemplates.first { it.id == actionId }
        val timestamp = nowEpochMillis()
        val plan = PlanItem(
            id = "${activeProfile.id}:${action.id}",
            profileId = activeProfile.id,
            actionTemplateId = action.id,
            title = action.title,
            target = action.target,
            targetDateEpochDays = timestamp / MILLIS_PER_DAY + action.minimumDurationDays,
            note = action.sourceNote,
            expectedDurationDays = action.minimumDurationDays,
            effort = action.effort,
            costBand = action.costBand,
            createdAtEpochMillis = timestamp,
            updatedAtEpochMillis = timestamp,
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            planRepository.replaceCurrentPlan(plan).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = "Porta could not save this plan locally. Please try again.",
                    )
                },
            )
        }
    }

    private fun buildState(
        profile: ProfileSnapshot,
        isSampleProfile: Boolean,
    ): ScenarioPreviewUiState {
        val action = activeOpportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionId }
            ?: return ScenarioPreviewUiState(error = "This action is no longer available.")
        val currentLabel = when (val target = action.target) {
            is ActionTarget.ProfileFactTarget -> profile.facts[target.factKey]
                ?.value
                ?.displayValue()
                ?: return ScenarioPreviewUiState(
                    isSampleProfile = isSampleProfile,
                    error = "Your profile is missing ${target.factKey.readable()}.",
                )

            is ActionTarget.OpportunityRuleTarget -> profile.ruleAssertions[
                RuleRef(target.opportunityId, target.ruleId),
            ]?.value?.displayValue() ?: "Not confirmed"
        }
        val today = activeDate
        val scenario = scenarioEvaluator.evaluate(
            profile = profile,
            overrides = action.target.asScenarioOverrides(),
            opportunities = activeOpportunities,
            today = today,
        )
        val opportunitiesById = activeOpportunities.associateBy { it.id }
        val addressedIds = scenario.resolvedHardGapOpportunityIds(action.target).toSet()
        val impactedDoors = scenario.impacts.mapNotNull { impact ->
            val opportunity = opportunitiesById[impact.opportunityId] ?: return@mapNotNull null
            impact.toUiState(
                opportunity = opportunity,
                action = action,
                today = today,
                addressesCheckedRequirement = impact.opportunityId in addressedIds,
            )
        }
        val wouldPassCurrentChecks = impactedDoors.filter { door ->
            door.id in addressedIds &&
                door.timingWarning == null &&
                scenario.simulated.getValue(door.id).state == EligibilityState.OPEN
        }
        val passedIds = wouldPassCurrentChecks.map(ScenarioDoorUiState::id).toSet()
        val progressed = impactedDoors.filterNot { it.id in passedIds }
        val impactedIds = impactedDoors.map(ScenarioDoorUiState::id).toSet()
        val unchanged = activeOpportunities
            .filterNot { it.id in impactedIds }
            .map { opportunity ->
                val state = scenario.baseline.getValue(opportunity.id).state
                ScenarioDoorUiState(
                    id = opportunity.id,
                    title = opportunity.title,
                    stateLabel = state.trustLabel(),
                    actualStateLabel = state.trustLabel(),
                    simulatedStateLabel = state.trustLabel(),
                )
            }
        return ScenarioPreviewUiState(
            actionId = action.id,
            title = action.title,
            beforeLabel = currentLabel,
            afterLabel = action.targetValue.displayValue(),
            scopeNotice = if (action.target is ActionTarget.OpportunityRuleTarget) {
                "Would satisfy this programme-specific check if confirmed."
            } else {
                null
            },
            openedDoors = wouldPassCurrentChecks,
            progressedDoors = progressed,
            unchangedDoors = unchanged,
            isSampleProfile = isSampleProfile,
        )
    }

    private fun FactKey.readable(): String = when (this) {
        FactKey.IELTS_SCORE -> "IELTS"
        else -> name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar)
    }

    private fun ScenarioImpactReceipt.toUiState(
        opportunity: Opportunity,
        action: ActionTemplate,
        today: LocalDate,
        addressesCheckedRequirement: Boolean,
    ): ScenarioDoorUiState {
        val timingFeasible = opportunity.isFeasibleForPlanning(today, action.minimumDurationDays)
        val changedToPass = changedRules.any { it.beforeResult != RuleResult.PASS && it.afterResult == RuleResult.PASS }
        val outcome = when {
            !timingFeasible -> "A check changes, but this estimate misses the published deadline"

            afterState == EligibilityState.OPEN && addressesCheckedRequirement -> "Would pass current checks"

            changedToPass && afterState == EligibilityState.NEEDS_INFO ->
                "Would address a checked requirement; verification remains"

            changedToPass -> "Would address one checked requirement"

            else -> "The value changes, but the encoded check result does not"
        }
        return ScenarioDoorUiState(
            id = opportunity.id,
            title = opportunity.title,
            stateLabel = outcome,
            actualStateLabel = beforeState.trustLabel(),
            simulatedStateLabel = afterState.trustLabel(),
            changedRules = changedRules.map { delta ->
                ScenarioRuleChangeUiState(
                    id = delta.ruleId,
                    explanation = delta.explanation,
                    beforeLabel = delta.beforeActual?.displayValue() ?: "Not provided",
                    afterLabel = delta.afterActual?.displayValue() ?: "Not provided",
                    resultTransitionLabel = "${delta.beforeResult.readable()} → ${delta.afterResult.readable()}",
                )
            },
            remainingBlockers = remainingRules.map { rule ->
                when (rule.result) {
                    RuleResult.FAIL -> "Still needs action: ${rule.explanation}"
                    RuleResult.UNKNOWN -> "Still needs information: ${rule.explanation}"
                    RuleResult.REVIEW -> "Still needs source review: ${rule.explanation}"
                    RuleResult.PASS -> error("Passed rules are not remaining blockers.")
                }
            },
            timingLabel = when (val publishedDeadline = deadline) {
                is Deadline.Known -> "Published deadline: ${publishedDeadline.date}"
                Deadline.Tba -> "Deadline: TBA — verify before planning"
                Deadline.Missing -> "No single published deadline — check the live source"
            },
            timingWarning = if (timingFeasible) {
                null
            } else {
                "The ${action.minimumDurationDays}-day Porta estimate extends beyond the published deadline."
            },
        )
    }

    private fun RuleResult.readable(): String = when (this) {
        RuleResult.PASS -> "Check passed"
        RuleResult.FAIL -> "Needs action"
        RuleResult.UNKNOWN -> "Information missing"
        RuleResult.REVIEW -> "Source review needed"
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
