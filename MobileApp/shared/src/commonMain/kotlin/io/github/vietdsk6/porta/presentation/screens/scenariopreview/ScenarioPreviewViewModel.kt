package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.asScenarioOverrides
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.isFeasibleForPlanning
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
            ) { profile, packResult -> profile to packResult.getOrNull() }
                .collect { (profile, pack) ->
                    isSampleProfile = profile.facts.isEmpty()
                    activeProfile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile
                    activeOpportunities = pack?.opportunities ?: PortaSampleData.opportunities
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
            title = if (action.target is ActionTarget.OpportunityRuleTarget) {
                "${action.title} · ${opportunity.title}"
            } else {
                action.title
            },
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
        val today = currentLocalDateProvider.today()
        val scenario = scenarioEvaluator.evaluate(
            profile = profile,
            overrides = action.target.asScenarioOverrides(),
            opportunities = activeOpportunities,
            today = today,
        )
        val opportunitiesById = activeOpportunities.associateBy { it.id }
        val improvedIds = scenario.resolvedHardGapOpportunityIds(action.target)
            .filter { opportunityId ->
                opportunitiesById[opportunityId]
                    ?.isFeasibleForPlanning(
                        today = today,
                        minimumDurationDays = action.minimumDurationDays,
                    )
                    ?: false
            }
            .toSet()
        val wouldPassCurrentChecks = scenario.deltas
            .filter {
                it.opportunityId in improvedIds &&
                    it.before != EligibilityState.OPEN &&
                    it.after == EligibilityState.OPEN
            }
            .mapNotNull { delta ->
                opportunitiesById[delta.opportunityId]?.let { opportunity ->
                    ScenarioDoorUiState(
                        id = opportunity.id,
                        title = opportunity.title,
                        stateLabel = "Would pass current checks",
                        actualStateLabel = delta.before.trustLabel(),
                        simulatedStateLabel = delta.after.trustLabel(),
                    )
                }
            }
        val passedIds = wouldPassCurrentChecks.map(ScenarioDoorUiState::id).toSet()
        val progressed = improvedIds
            .filterNot(passedIds::contains)
            .mapNotNull { opportunityId ->
                opportunitiesById[opportunityId]?.let { opportunity ->
                    val before = scenario.baseline.getValue(opportunityId).state
                    val after = scenario.simulated.getValue(opportunityId).state
                    val outcome = if (after == EligibilityState.NEEDS_INFO) {
                        "Still needs verification"
                    } else {
                        "Would address one checked requirement"
                    }
                    ScenarioDoorUiState(
                        id = opportunity.id,
                        title = opportunity.title,
                        stateLabel = outcome,
                        actualStateLabel = before.trustLabel(),
                        simulatedStateLabel = after.trustLabel(),
                    )
                }
            }
        val unchanged = activeOpportunities
            .filterNot { it.id in improvedIds }
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

    private fun EligibilityState.trustLabel(): String = when (this) {
        EligibilityState.OPEN -> "Current checks passed"
        EligibilityState.HAS_GAPS -> "Needs action"
        EligibilityState.NEEDS_INFO -> "Needs verification"
        EligibilityState.NOT_ACTIONABLE -> "Not available this cycle"
        EligibilityState.CLOSED -> "Closed"
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
