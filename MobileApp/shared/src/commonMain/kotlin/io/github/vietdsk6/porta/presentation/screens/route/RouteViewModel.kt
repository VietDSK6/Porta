package io.github.vietdsk6.porta.presentation.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import io.github.vietdsk6.porta.domain.model.RouteExplanation
import io.github.vietdsk6.porta.domain.usecase.RoutePlanner
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class RouteViewModel(
    private val routePlanner: RoutePlanner,
    profileRepository: ProfileRepository,
    opportunityPackRepository: OpportunityPackRepository,
    savedOpportunityRepository: SavedOpportunityRepository,
    private val planRepository: PlanRepository,
    currentLocalDateProvider: CurrentLocalDateProvider,
    private val plannerContext: CoroutineContext,
) : ViewModel() {
    private val constraints = MutableStateFlow(
        RouteConstraints(
            horizonDays = 180,
            maxTotalCost = CostBand.UNKNOWN,
            maxEffort = EffortLevel.VERY_HIGH,
        ),
    )
    private val selectedRouteId = MutableStateFlow<String?>(null)
    private val saveState = MutableStateFlow(SaveState())

    private val planning = combine(
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        savedOpportunityRepository.observeSavedIds(),
        currentLocalDateProvider.currentDate,
        constraints,
    ) { profile, packResult, savedIds, today, selectedConstraints ->
        PlanningInputs(
            profile = profile,
            opportunities = packResult.getOrNull()?.opportunities?.takeIf(List<Opportunity>::isNotEmpty)
                ?: PortaSampleData.opportunities,
            savedIds = savedIds,
            today = today,
            constraints = selectedConstraints,
        )
    }.debounce(150).mapLatest { inputs ->
        withContext(plannerContext) {
            val useSample = inputs.profile.facts.isEmpty()
            val profile = inputs.profile.takeUnless { useSample } ?: PortaSampleData.profile
            val routes = routePlanner.plan(
                profile = profile,
                opportunities = inputs.opportunities,
                savedOpportunityIds = inputs.savedIds,
                constraints = inputs.constraints,
                today = inputs.today,
            )
            PlanningResult(
                routes = routes,
                labels = inputs.opportunities.associate { it.id to it.title },
                canSave = !useSample,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlanningResult())

    val uiState: StateFlow<RouteUiState> = combine(
        planning,
        constraints,
        selectedRouteId,
        saveState,
    ) { result, selectedConstraints, selectedId, save ->
        val routeOptions = result.routes.map { route -> route.toUiState(result.labels) }
        RouteUiState(
            isLoading = result.isLoading,
            isSaving = save.isSaving,
            canSave = result.canSave,
            horizonDays = selectedConstraints.horizonDays,
            maxTotalCost = selectedConstraints.maxTotalCost,
            maxEffort = selectedConstraints.maxEffort,
            routes = routeOptions,
            selectedRouteId = selectedId?.takeIf { id -> routeOptions.any { it.id == id } }
                ?: routeOptions.firstOrNull()?.id,
            error = save.error,
            saveNotice = save.notice,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RouteUiState())

    fun onUiEvent(event: RouteUiEvent) {
        when (event) {
            is RouteUiEvent.HorizonChanged -> constraints.value = constraints.value.copy(
                horizonDays = event.days.coerceIn(30, 365),
            )

            is RouteUiEvent.MaxCostChanged -> constraints.value = constraints.value.copy(maxTotalCost = event.cost)

            is RouteUiEvent.MaxEffortChanged -> constraints.value = constraints.value.copy(maxEffort = event.effort)

            is RouteUiEvent.RouteSelected -> selectedRouteId.value = event.id

            RouteUiEvent.SaveRoute -> saveSelectedRoute()

            RouteUiEvent.NoticeDismissed -> saveState.value = SaveState()
        }
    }

    private fun saveSelectedRoute() {
        if (!planning.value.canSave || saveState.value.isSaving) return
        val selectedId = selectedRouteId.value ?: planning.value.routes.firstOrNull()?.id
        val route = planning.value.routes.firstOrNull { it.id == selectedId } ?: return
        viewModelScope.launch {
            saveState.value = SaveState(isSaving = true)
            saveState.value = planRepository.saveRoute(route).fold(
                onSuccess = { SaveState(notice = "Route saved to your active plan.") },
                onFailure = { SaveState(error = "Porta could not save this route. Please try again.") },
            )
        }
    }

    private fun PlannedRoute.toUiState(labels: Map<String, String>): RouteOptionUiState = RouteOptionUiState(
        id = id,
        steps = steps.map { step ->
            RouteStepUiState(
                id = step.template.id,
                title = step.template.title,
                startDateLabel = step.startDate.toString(),
                endDateLabel = step.endDate.toString(),
                durationDays = step.template.minimumDurationDays,
                doorsOpened = step.doorsOpened.map { labels[it] ?: it },
                doorsImproved = step.doorsImproved.map { labels[it] ?: it },
                doorsLost = step.doorsLost.map { labels[it] ?: it },
                effort = step.template.effort,
                cost = step.template.costBand,
            )
        },
        openedTotal = metrics.openedTotal,
        openedSaved = metrics.openedSaved,
        daysToFirstDoor = metrics.daysToFirstDoor,
        totalDays = metrics.totalDays,
        costRankSum = metrics.costRankSum,
        explanation = when (val reason = explanation) {
            is RouteExplanation.OpensSavedDoors -> "Opens ${reason.count} saved doors first."
            is RouteExplanation.OpensDoors -> "Opens ${reason.count} doors on the assessed rules."
            is RouteExplanation.ImprovesDoors -> "Improves the assessed checks for ${reason.count} doors."
        },
    )

    private data class PlanningInputs(
        val profile: ProfileSnapshot,
        val opportunities: List<Opportunity>,
        val savedIds: Set<String>,
        val today: LocalDate,
        val constraints: RouteConstraints,
    )

    private data class PlanningResult(
        val routes: List<PlannedRoute> = emptyList(),
        val labels: Map<String, String> = emptyMap(),
        val canSave: Boolean = false,
        val isLoading: Boolean = true,
    )

    private data class SaveState(
        val isSaving: Boolean = false,
        val error: String? = null,
        val notice: String? = null,
    )
}
