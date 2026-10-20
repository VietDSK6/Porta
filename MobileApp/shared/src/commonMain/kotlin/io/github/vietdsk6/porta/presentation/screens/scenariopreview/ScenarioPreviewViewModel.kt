package io.github.vietdsk6.porta.presentation.screens.scenariopreview

import androidx.lifecycle.ViewModel
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScenarioPreviewViewModel(
    actionId: String,
    private val scenarioEvaluator: ScenarioEvaluator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(buildState(actionId))
    val uiState: StateFlow<ScenarioPreviewUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: ScenarioPreviewUiEvent) {
        when (event) {
            ScenarioPreviewUiEvent.Acknowledged -> Unit
        }
    }

    private fun buildState(actionId: String): ScenarioPreviewUiState {
        val action = PortaSampleData.opportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionId }
            ?: return ScenarioPreviewUiState(error = "This sample action is no longer available.")
        val currentValue = PortaSampleData.profile.facts[action.targetFactKey]?.value
            ?: return ScenarioPreviewUiState(error = "The sample profile is missing ${action.targetFactKey}.")
        val scenario = scenarioEvaluator.evaluate(
            profile = PortaSampleData.profile,
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
            title = action.title,
            beforeLabel = currentValue.displayValue(),
            afterLabel = action.targetValue.displayValue(),
            openedDoors = opened,
            unchangedDoors = unchanged,
        )
    }
}
