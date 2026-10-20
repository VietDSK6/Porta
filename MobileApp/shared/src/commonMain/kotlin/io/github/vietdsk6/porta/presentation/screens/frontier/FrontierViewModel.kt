package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FrontierViewModel(
    private val actionFrontierEngine: ActionFrontierEngine,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private var activeProfile = PortaSampleData.profile
    private var isSampleProfile = true
    private val _uiState = MutableStateFlow(buildState(horizonDays = 90))
    val uiState: StateFlow<FrontierUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getProfile().onSuccess { profile ->
                if (profile.facts.isNotEmpty()) {
                    activeProfile = profile
                    isSampleProfile = false
                    _uiState.value = buildState(_uiState.value.horizonDays)
                }
            }
        }
    }

    fun onUiEvent(event: FrontierUiEvent) {
        when (event) {
            is FrontierUiEvent.HorizonChanged -> _uiState.value = buildState(event.days)
        }
    }

    private fun buildState(horizonDays: Int): FrontierUiState {
        val opportunitiesById = PortaSampleData.opportunities.associateBy { it.id }
        val actions = actionFrontierEngine.build(
            profile = activeProfile,
            opportunities = PortaSampleData.opportunities,
            savedOpportunityIds = PortaSampleData.savedOpportunityIds,
            constraints = PlanningConstraints(
                horizonDays = horizonDays,
                maxEffort = EffortLevel.VERY_HIGH,
                maxCost = CostBand.UNKNOWN,
            ),
            today = PortaSampleData.referenceDate,
        ).map { action ->
            FrontierActionUiState(
                id = action.template.id,
                title = if (isSampleProfile) {
                    action.template.title
                } else {
                    "Raise ${action.template.targetFactKey.name.readable()} to ${action.template.targetValue.displayValue()}"
                },
                targetLabel = action.template.targetValue.displayValue(),
                newlyOpenCount = action.newlyOpenOpportunityIds.size,
                savedDoorCount = action.newlyOpenSavedOpportunityIds.size,
                durationLabel = "${action.template.minimumDurationDays} days",
                effortLabel = action.template.effort.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                costLabel = action.template.costBand.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                explanation = action.explanation,
                doorTitles = action.newlyOpenOpportunityIds.mapNotNull { opportunitiesById[it]?.title },
            )
        }
        return FrontierUiState(
            isSampleMode = isSampleProfile,
            horizonDays = horizonDays,
            actions = actions,
        )
    }

    private fun String.readable(): String = lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar)
}
