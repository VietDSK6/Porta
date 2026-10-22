package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FrontierViewModel(
    private val actionFrontierEngine: ActionFrontierEngine,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    planRepository: PlanRepository,
) : ViewModel() {
    private var activeProfile = PortaSampleData.profile
    private var isSampleProfile = true
    private var isSampleCatalog = true
    private var activeOpportunities = PortaSampleData.opportunities
    private var activePlan: ActivePlanSummaryUiState? = null
    private var selectedActionId: String? = null
    private val _uiState = MutableStateFlow(buildState(horizonDays = 90))
    val uiState: StateFlow<FrontierUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
            ) { profile, packResult -> profile to packResult.getOrNull() }
                .collect { (profile, pack) ->
                    isSampleProfile = profile.facts.isEmpty()
                    activeProfile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile
                    activeOpportunities = pack?.opportunities?.takeIf(List<Opportunity>::isNotEmpty)
                        ?: PortaSampleData.opportunities
                    isSampleCatalog = pack == null ||
                        pack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID
                    _uiState.value = buildState(_uiState.value.horizonDays)
                }
        }
        viewModelScope.launch {
            planRepository.observeCurrentPlan().collect { plan ->
                activePlan = plan?.let {
                    ActivePlanSummaryUiState(
                        title = it.title,
                        statusLabel = when (it.status) {
                            PlanStatus.PLANNED -> "Planned"
                            PlanStatus.IN_PROGRESS -> "In progress"
                            PlanStatus.PAUSED -> "Paused"
                            PlanStatus.COMPLETED -> "Work marked complete"
                        },
                    )
                }
                _uiState.value = buildState(_uiState.value.horizonDays)
            }
        }
    }

    fun onUiEvent(event: FrontierUiEvent) {
        when (event) {
            is FrontierUiEvent.HorizonChanged -> _uiState.value = buildState(event.days)

            is FrontierUiEvent.ActionSelected -> {
                selectedActionId = event.actionId
                _uiState.value = _uiState.value.copy(selectedActionId = event.actionId)
            }
        }
    }

    private fun buildState(horizonDays: Int): FrontierUiState {
        val opportunitiesById = activeOpportunities.associateBy { it.id }
        val actions = actionFrontierEngine.build(
            profile = activeProfile,
            opportunities = activeOpportunities,
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
                title = action.template.title,
                currentLabel = activeProfile.facts[action.template.targetFactKey]
                    ?.value
                    ?.displayValue()
                    ?: "Not set",
                targetLabel = action.template.targetValue.displayValue(),
                newlyOpenCount = action.newlyOpenOpportunityIds.size,
                improvedCount = action.improvedOpportunityIds.size,
                savedDoorCount = action.improvedSavedOpportunityIds.size,
                timingUnverifiedCount = action.timingUnverifiedOpportunityIds.size,
                durationLabel = "${action.template.minimumDurationDays} days",
                effortLabel = action.template.effort.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                costLabel = action.template.costBand.name.lowercase().replaceFirstChar(Char::uppercaseChar),
                explanation = action.explanation,
                doorTitles = action.improvedOpportunityIds.mapNotNull { opportunitiesById[it]?.title },
                doorComparisons = action.improvedOpportunityIds.mapNotNull { opportunityId ->
                    val opportunity = opportunitiesById[opportunityId] ?: return@mapNotNull null
                    val before = action.scenario.baseline[opportunityId]?.state ?: return@mapNotNull null
                    val after = action.scenario.simulated[opportunityId]?.state ?: return@mapNotNull null
                    FrontierDoorComparisonUiState(
                        id = opportunityId,
                        title = opportunity.title,
                        actualStateLabel = before.readable(),
                        previewStateLabel = after.readable(),
                        isNewlyOpen = opportunityId in action.newlyOpenOpportunityIds,
                        isSaved = opportunityId in action.improvedSavedOpportunityIds,
                        requiresDeadlineCheck = opportunityId in action.timingUnverifiedOpportunityIds,
                    )
                },
            )
        }
        selectedActionId = selectedActionId
            ?.takeIf { selectedId -> actions.any { it.id == selectedId } }
            ?: actions.firstOrNull()?.id
        return FrontierUiState(
            isSampleMode = isSampleProfile,
            isSampleCatalog = isSampleCatalog,
            catalogDoorCount = activeOpportunities.size,
            horizonDays = horizonDays,
            activePlan = activePlan,
            actions = actions,
            selectedActionId = selectedActionId,
        )
    }

    private fun EligibilityState.readable(): String = name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::uppercaseChar)
}
