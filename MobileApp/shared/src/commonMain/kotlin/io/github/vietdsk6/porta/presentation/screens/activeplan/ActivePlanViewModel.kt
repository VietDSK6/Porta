package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.asScenarioOverrides
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.isFeasibleForPlanning
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.util.extensions.asFormattedDate
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivePlanViewModel(
    private val planRepository: PlanRepository,
    profileRepository: ProfileRepository,
    opportunityPackRepository: OpportunityPackRepository,
    private val scenarioEvaluator: ScenarioEvaluator,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private val operation = MutableStateFlow(OperationState())

    val uiState: StateFlow<ActivePlanUiState> = combine(
        planRepository.observeCurrentPlan(),
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        operation,
    ) { plan, profile, packResult, operation ->
        val activePack = packResult.getOrNull()
        val opportunities = activePack?.opportunities ?: PortaSampleData.opportunities
        ActivePlanUiState(
            isLoading = false,
            isWorking = operation.isWorking,
            isSampleCatalog = activePack == null ||
                activePack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID,
            plan = plan?.toUiState(profile, opportunities),
            error = operation.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ActivePlanUiState(),
    )

    fun onUiEvent(event: ActivePlanUiEvent) {
        val plan = uiState.value.plan ?: return
        when (event) {
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
            onFailure = { OperationState(error = "Porta could not update this local plan. Please try again.") },
        )
    }

    private fun PlanItem.toUiState(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
    ): ActivePlanCardUiState {
        val action = opportunities
            .flatMap { it.actionTemplates }
            .firstOrNull { it.id == actionTemplateId }
        val affectedDoors = if (profile.facts.isEmpty()) {
            emptyList()
        } else {
            val today = currentLocalDateProvider.today()
            scenarioEvaluator.evaluate(
                profile = profile,
                overrides = target.asScenarioOverrides(),
                opportunities = opportunities,
                today = today,
            ).resolvedHardGapOpportunityIds(target)
                .filter { opportunityId ->
                    opportunities.firstOrNull { it.id == opportunityId }
                        ?.isFeasibleForPlanning(
                            today = today,
                            minimumDurationDays = expectedDurationDays ?: action?.minimumDurationDays ?: 0,
                        )
                        ?: false
                }
                .mapNotNull { opportunityId ->
                    opportunities.firstOrNull { it.id == opportunityId }?.title
                }
        }
        val hasStarted = status == PlanStatus.IN_PROGRESS ||
            status == PlanStatus.PAUSED ||
            status == PlanStatus.COMPLETED
        val isComplete = status == PlanStatus.COMPLETED
        val completedSteps = 1 + hasStarted.toInt() + isComplete.toInt()

        return ActivePlanCardUiState(
            id = id,
            actionId = actionTemplateId,
            title = title,
            targetLabel = targetValue.displayValue(),
            status = status.toUiStatus(),
            statusLabel = status.label(),
            durationLabel = expectedDurationDays?.let { "Est. $it days · Porta assumption" }
                ?: "Time estimate unavailable",
            effortLabel = effort.name.lowercase().replaceFirstChar(Char::uppercaseChar),
            costLabel = costBand.name.lowercase().replaceFirstChar(Char::uppercaseChar),
            evidenceLabel = action?.completionEvidenceTypes?.joinToString()?.replaceFirstChar(Char::uppercaseChar)
                ?: "Evidence definition needs review",
            affectedDoorTitles = affectedDoors,
            riskNote = "This milestone can address the published checks Porta can evaluate. Confirm remaining manual checks and " +
                "live deadlines on official sources before acting.",
            updatedLabel = "Updated ${updatedAtEpochMillis.asFormattedDate()}",
            progress = completedSteps / 3f,
            checklist = listOf(
                PlanStepUiState("Milestone saved locally", isComplete = true),
                PlanStepUiState("Work started", isComplete = hasStarted),
                PlanStepUiState("Milestone marked complete", isComplete = isComplete),
            ),
        )
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

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
    )
}
