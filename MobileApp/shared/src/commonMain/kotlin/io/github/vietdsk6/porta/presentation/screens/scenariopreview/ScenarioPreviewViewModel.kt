package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
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
        val action = activeOpportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionId }
            ?: return
        val timestamp = nowEpochMillis()
        val plan = PlanItem(
            id = "${activeProfile.id}:${action.id}",
            profileId = activeProfile.id,
            actionTemplateId = action.id,
            title = "Raise ${action.targetFactKey.readable()} to ${action.targetValue.displayValue()}",
            targetFactKey = action.targetFactKey,
            targetValue = action.targetValue,
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
        val currentValue = profile.facts[action.targetFactKey]?.value
            ?: return ScenarioPreviewUiState(
                isSampleProfile = isSampleProfile,
                error = "Your profile is missing ${action.targetFactKey.readable()}.",
            )
        val today = currentLocalDateProvider.today()
        val scenario = scenarioEvaluator.evaluate(
            profile = profile,
            overrides = mapOf(action.targetFactKey to action.targetValue),
            opportunities = activeOpportunities,
            today = today,
        )
        val opportunitiesById = activeOpportunities.associateBy { it.id }
        val improvedIds = scenario.resolvedHardGapOpportunityIds(action.targetFactKey)
            .filter { opportunityId ->
                opportunitiesById[opportunityId]
                    ?.isFeasibleForPlanning(
                        today = today,
                        minimumDurationDays = action.minimumDurationDays,
                    )
                    ?: false
            }
            .toSet()
        val opened = scenario.deltas
            .filter {
                it.opportunityId in improvedIds &&
                    it.before != EligibilityState.OPEN &&
                    it.after == EligibilityState.OPEN
            }
            .mapNotNull { delta ->
                opportunitiesById[delta.opportunityId]?.let {
                    ScenarioDoorUiState(it.id, it.title, "Opens")
                }
            }
        val openedIds = opened.map(ScenarioDoorUiState::id).toSet()
        val progressed = improvedIds
            .filterNot(openedIds::contains)
            .mapNotNull { opportunityId ->
                opportunitiesById[opportunityId]?.let { opportunity ->
                    val before = scenario.baseline.getValue(opportunityId).state
                    val after = scenario.simulated.getValue(opportunityId).state
                    val label = if (before == after) {
                        "Hard gap resolved · verify remaining rules"
                    } else {
                        "${before.readable()} → ${after.readable()}"
                    }
                    ScenarioDoorUiState(opportunity.id, opportunity.title, label)
                }
            }
        val unchanged = activeOpportunities
            .filterNot { it.id in improvedIds }
            .map { opportunity ->
                val state = scenario.baseline.getValue(opportunity.id).state
                ScenarioDoorUiState(opportunity.id, opportunity.title, state.readable())
            }
        return ScenarioPreviewUiState(
            actionId = action.id,
            title = action.title,
            beforeLabel = currentValue.displayValue(),
            afterLabel = action.targetValue.displayValue(),
            openedDoors = opened,
            progressedDoors = progressed,
            unchangedDoors = unchanged,
            isSampleProfile = isSampleProfile,
        )
    }

    private fun FactKey.readable(): String = when (this) {
        FactKey.IELTS_SCORE -> "IELTS"
        else -> name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar)
    }

    private fun EligibilityState.readable(): String = name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::uppercaseChar)

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
