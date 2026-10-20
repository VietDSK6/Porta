package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScenarioPreviewViewModel(
    private val actionId: String,
    private val scenarioEvaluator: ScenarioEvaluator,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
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
                    _uiState.value = buildState(profile = profile, isSampleProfile = false)
                }
            }
        }
    }

    fun onUiEvent(event: ScenarioPreviewUiEvent) {
        when (event) {
            ScenarioPreviewUiEvent.Acknowledged -> Unit
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
                error = "Your profile is missing ${action.targetFactKey.name.readable()}.",
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
                "Raise ${action.targetFactKey.name.readable()} to ${action.targetValue.displayValue()}"
            },
            beforeLabel = currentValue.displayValue(),
            afterLabel = action.targetValue.displayValue(),
            openedDoors = opened,
            unchangedDoors = unchanged,
            isSampleProfile = isSampleProfile,
        )
    }

    private fun String.readable(): String = lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar)
}
