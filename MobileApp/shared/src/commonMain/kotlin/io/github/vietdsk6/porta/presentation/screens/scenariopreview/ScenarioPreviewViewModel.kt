package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScenarioPreviewViewModel(
    private val actionId: String,
    private val scenarioEvaluator: ScenarioEvaluator,
    private val profileRepository: ProfileRepository,
    private val planRepository: PlanRepository,
) : ViewModel() {
    private var activeProfile = PortaSampleData.profile
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
            profileRepository.getProfile().onSuccess { profile ->
                if (profile.facts.isNotEmpty()) {
                    activeProfile = profile
                    isSampleProfile = false
                    _uiState.value = buildState(profile = profile, isSampleProfile = false)
                }
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
        val action = PortaSampleData.opportunities
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
        val action = PortaSampleData.opportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionId }
            ?: return ScenarioPreviewUiState(error = "This action is no longer available.")
        val currentValue = profile.facts[action.targetFactKey]?.value
            ?: return ScenarioPreviewUiState(
                isSampleProfile = isSampleProfile,
                error = "Your profile is missing ${action.targetFactKey.readable()}.",
            )
        val scenario = scenarioEvaluator.evaluate(
            profile = profile,
            overrides = mapOf(action.targetFactKey to action.targetValue),
            opportunities = PortaSampleData.opportunities,
            today = PortaSampleData.referenceDate,
        )
        val opportunitiesById = PortaSampleData.opportunities.associateBy { it.id }
        val opened = scenario.deltas
            .filter { it.before != EligibilityState.OPEN && it.after == EligibilityState.OPEN }
            .mapNotNull { delta ->
                opportunitiesById[delta.opportunityId]?.let {
                    ScenarioDoorUiState(it.id, it.title, "Opens")
                }
            }
        val unchanged = PortaSampleData.opportunities
            .filter { opportunity -> scenario.deltas.none { it.opportunityId == opportunity.id } }
            .map { opportunity ->
                val state = scenario.baseline.getValue(opportunity.id).state
                ScenarioDoorUiState(opportunity.id, opportunity.title, state.name.replace('_', ' '))
            }
        return ScenarioPreviewUiState(
            actionId = action.id,
            title = if (isSampleProfile) {
                action.title
            } else {
                "Raise ${action.targetFactKey.readable()} to ${action.targetValue.displayValue()}"
            },
            beforeLabel = currentValue.displayValue(),
            afterLabel = action.targetValue.displayValue(),
            openedDoors = opened,
            unchangedDoors = unchanged,
            isSampleProfile = isSampleProfile,
        )
    }

    private fun FactKey.readable(): String = when (this) {
        FactKey.IELTS_SCORE -> "IELTS"
        else -> name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar)
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
