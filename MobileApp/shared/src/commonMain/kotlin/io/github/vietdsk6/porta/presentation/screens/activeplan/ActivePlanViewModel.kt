package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.scenario.SampleScenarioProvider
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.asScenarioOverrides
import io.github.vietdsk6.porta.domain.model.isFeasibleForPlanning
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.locale.AppLanguageState
import io.github.vietdsk6.porta.presentation.locale.asLocalizedDate
import io.github.vietdsk6.porta.presentation.locale.displayValue
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class ActivePlanViewModel(
    private val planRepository: PlanRepository,
    profileRepository: ProfileRepository,
    opportunityPackRepository: OpportunityPackRepository,
    private val scenarioEvaluator: ScenarioEvaluator,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
    private val sampleScenarioProvider: SampleScenarioProvider = SampleScenarioProvider(),
) : ViewModel() {
    private val operation = MutableStateFlow(OperationState())
    private val selectedPlanId = MutableStateFlow<String?>(null)
    private val planSelection = combine(planRepository.observePlans(), selectedPlanId) { plans, selectedId ->
        plans to selectedId
    }

    val uiState: StateFlow<ActivePlanUiState> = combine(
        planSelection,
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        operation,
        combine(currentLocalDateProvider.currentDate, AppLanguageState.changes) { today, _ -> today },
    ) { (plans, selectedId), profile, packResult, operation, today ->
        val activePack = packResult.getOrNull()
        val opportunities = activePack?.opportunities ?: sampleScenarioProvider.opportunities
        val routePlans = plans.filter { it.routeId != null && it.sequence != null }.sortedBy { it.sequence }
        val selectedPlan = routePlans.firstOrNull { it.id == selectedId }
            ?: routePlans.firstOrNull()
            ?: plans.firstOrNull { it.status == PlanStatus.IN_PROGRESS }
            ?: plans.firstOrNull { it.status == PlanStatus.PLANNED }
            ?: plans.firstOrNull { it.status == PlanStatus.PAUSED }
            ?: plans.firstOrNull()
        ActivePlanUiState(
            isLoading = false,
            isWorking = operation.isWorking,
            isSampleCatalog = activePack == null ||
                sampleScenarioProvider.isLegacySyntheticPack(activePack.metadata.id),
            plan = selectedPlan?.toUiState(profile, opportunities, today),
            routeSteps = routePlans.map { plan ->
                ActiveRouteStepUiState(
                    planId = plan.id,
                    sequence = plan.sequence ?: 0,
                    title = plan.title,
                )
            },
            selectedPlanId = selectedPlan?.id,
            error = operation.error,
            errorCode = operation.errorCode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ActivePlanUiState(),
    )

    fun onUiEvent(event: ActivePlanUiEvent) {
        if (event is ActivePlanUiEvent.SelectStep) {
            if (uiState.value.routeSteps.any { it.planId == event.planId }) selectedPlanId.value = event.planId
            return
        }
        val plan = uiState.value.plan ?: return
        when (event) {
            is ActivePlanUiEvent.SelectStep -> Unit
            ActivePlanUiEvent.Start -> updateStatus(plan.id, PlanStatus.IN_PROGRESS)
            ActivePlanUiEvent.Pause -> updateStatus(plan.id, PlanStatus.PAUSED)
            ActivePlanUiEvent.Resume -> updateStatus(plan.id, PlanStatus.IN_PROGRESS)
            ActivePlanUiEvent.Complete -> updateStatus(plan.id, PlanStatus.COMPLETED)
            ActivePlanUiEvent.Remove -> runOperation { planRepository.remove(plan.id) }
        }
    }

    private fun updateStatus(id: String, status: PlanStatus) {
        runOperation { planRepository.updateStatus(id, status) }
    }

    private fun runOperation(block: suspend () -> Result<Unit>) = viewModelScope.launch {
        operation.value = OperationState(isWorking = true)
        operation.value = block().fold(
            onSuccess = { OperationState() },
            onFailure = {
                OperationState(
                    error = "Porta could not update this local plan. Please try again.",
                    errorCode = ActivePlanError.UPDATE_FAILED,
                )
            },
        )
    }

    private fun PlanItem.toUiState(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        today: LocalDate,
    ): ActivePlanCardUiState {
        val action = opportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionTemplateId }
        val actionMinimumDurationDays = action?.minimumDurationDays?.takeIf { it > 0 }
        val planningDurationDays = expectedDurationDays ?: actionMinimumDurationDays
        val candidateCheckChanges = projectPlanImpacts(
            plan = this,
            profile = profile,
            opportunities = opportunities,
            today = today,
            scenarioEvaluator = scenarioEvaluator,
            minimumDurationDays = planningDurationDays,
        )
        return ActivePlanCardUiState(
            id = id,
            actionId = actionTemplateId,
            title = title,
            targetLabel = targetValue.displayValue(),
            status = status.toUiStatus(),
            statusLabel = status.label(),
            durationLabel = when {
                expectedDurationDays != null -> "Time estimate · $expectedDurationDays days (Porta assumption)"
                actionMinimumDurationDays != null -> "Minimum time · $actionMinimumDurationDays days (Porta assumption)"
                else -> "Time estimate · Unknown"
            },
            effortLabel = "Effort · " + effort.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar),
            costLabel = "Cost · " + costBand.name.lowercase().replaceFirstChar(Char::uppercaseChar),
            evidenceLabel = action?.completionEvidenceTypes?.joinToString()?.replaceFirstChar(Char::uppercaseChar)
                ?: "Evidence definition needs review",
            candidateCheckChanges = candidateCheckChanges,
            riskNote = "This milestone can address the published checks Porta can evaluate. Confirm remaining " +
                "manual checks and live deadlines on official sources before acting.",
            updatedLabel = "Updated ${updatedAtEpochMillis.asLocalizedDate()}",
            steps = listOf(
                PlanStepUiState("Plan saved", PlanStepState.DONE, PlanStepCopy.SAVED),
                when (status) {
                    PlanStatus.PLANNED -> PlanStepUiState("Start the work", PlanStepState.NEXT, PlanStepCopy.START)
                    PlanStatus.IN_PROGRESS -> PlanStepUiState("Work in progress", PlanStepState.IN_PROGRESS, PlanStepCopy.IN_PROGRESS)
                    PlanStatus.PAUSED -> PlanStepUiState("Work paused", PlanStepState.PAUSED, PlanStepCopy.PAUSED)
                    PlanStatus.COMPLETED -> PlanStepUiState("Work marked complete", PlanStepState.DONE, PlanStepCopy.COMPLETE)
                },
                PlanStepUiState(
                    "Confirm actual result",
                    if (status == PlanStatus.COMPLETED) PlanStepState.NEXT else PlanStepState.LATER,
                    PlanStepCopy.CONFIRM_RESULT,
                ),
            ),
            durationKind = when {
                expectedDurationDays != null -> PlanDurationKind.ESTIMATE
                actionMinimumDurationDays != null -> PlanDurationKind.MINIMUM
                else -> PlanDurationKind.UNKNOWN
            },
            durationDays = expectedDurationDays ?: actionMinimumDurationDays,
            effortName = effort.name,
            costName = costBand.name,
            evidenceDefinitionMissing = action == null || action.completionEvidenceTypes.isEmpty(),
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    private fun PlanStatus.toUiStatus(): ActivePlanStatusUi = when (this) {
        PlanStatus.PLANNED -> ActivePlanStatusUi.PLANNED
        PlanStatus.IN_PROGRESS -> ActivePlanStatusUi.IN_PROGRESS
        PlanStatus.PAUSED -> ActivePlanStatusUi.PAUSED
        PlanStatus.COMPLETED -> ActivePlanStatusUi.COMPLETED
    }

    private fun PlanStatus.label(): String = when (this) {
        PlanStatus.PLANNED -> "Planned"
        PlanStatus.IN_PROGRESS -> "In progress"
        PlanStatus.PAUSED -> "Paused"
        PlanStatus.COMPLETED -> "Work marked complete"
    }

    private data class OperationState(
        val isWorking: Boolean = false,
        val error: String? = null,
        val errorCode: ActivePlanError? = null,
    )
}

/** Candidate IDs come from the scenario check transition; timing is a separate planning filter. */
internal fun projectPlanImpacts(
    plan: PlanItem,
    profile: ProfileSnapshot,
    opportunities: List<Opportunity>,
    today: LocalDate,
    scenarioEvaluator: ScenarioEvaluator,
    minimumDurationDays: Int?,
): List<PlanOpportunityImpactUiState> {
    val byId = opportunities.associateBy(Opportunity::id)
    return scenarioEvaluator.evaluate(
        profile = profile,
        overrides = plan.target.asScenarioOverrides(),
        opportunities = opportunities,
        today = today,
    ).resolvedHardGapOpportunityIds(plan.target)
        .mapNotNull { opportunityId ->
            byId[opportunityId]?.let { opportunity ->
                PlanOpportunityImpactUiState(
                    opportunityId = opportunityId,
                    title = opportunity.title,
                    timing = opportunity.planTiming(today, minimumDurationDays),
                )
            }
        }
}

private fun Opportunity.planTiming(today: LocalDate, minimumDurationDays: Int?): PlanTimingStatus {
    val publishedDeadline = deadline as? Deadline.Known
    return when {
        archived -> PlanTimingStatus.OUTSIDE_CURRENT_CYCLE
        publishedDeadline != null && publishedDeadline.date < today -> PlanTimingStatus.MISSES_KNOWN_DEADLINE
        minimumDurationDays == null || publishedDeadline == null -> PlanTimingStatus.TIMING_UNVERIFIED
        isFeasibleForPlanning(today, minimumDurationDays) -> PlanTimingStatus.WITHIN_KNOWN_DEADLINE
        else -> PlanTimingStatus.MISSES_KNOWN_DEADLINE
    }
}
