package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.scenario.SampleScenarioProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.presentation.components.trustLabel
import io.github.vietdsk6.porta.presentation.locale.displayValue
import io.github.vietdsk6.porta.util.defaultAsyncDispatcher
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class FrontierViewModel(
    private val actionFrontierEngine: ActionFrontierEngine,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val savedOpportunityRepository: SavedOpportunityRepository,
    private val userPreferences: UserPreferences,
    planRepository: PlanRepository,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
    private val sampleScenarioProvider: SampleScenarioProvider,
    calculationDispatcher: CoroutineContext = defaultAsyncDispatcher,
) : ViewModel() {
    private val constraintsState = MutableStateFlow(DEFAULT_CONSTRAINTS)
    private val constraintsTouched = MutableStateFlow(false)
    private val selectedActionId = MutableStateFlow<String?>(null)
    private val comparedActionIds = MutableStateFlow<List<String>>(emptyList())

    private val inputs = combine(
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        savedOpportunityRepository.observeSavedIds(),
        currentLocalDateProvider.currentDate,
        planRepository.observeCurrentPlan(),
    ) { profile, packResult, savedIds, today, plan ->
        FrontierInputs(profile, packResult.getOrNull(), savedIds, today, plan)
    }

    val uiState: StateFlow<FrontierUiState> = combine(
        inputs,
        constraintsState,
        selectedActionId,
        comparedActionIds,
    ) { current, constraints, selectedId, comparedIds ->
        buildState(current, constraints, selectedId, comparedIds)
    }.flowOn(calculationDispatcher)
        .stateIn(viewModelScope, SharingStarted.Eagerly, FrontierUiState())

    init {
        viewModelScope.launch {
            val loaded = loadConstraints()
            if (!constraintsTouched.value) constraintsState.value = loaded
        }
    }

    fun onUiEvent(event: FrontierUiEvent) {
        when (event) {
            is FrontierUiEvent.HorizonChanged -> {
                if (event.days !in ALLOWED_HORIZONS) return
                updateConstraints(constraintsState.value.copy(horizonDays = event.days)) {
                    userPreferences.putInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS, event.days)
                }
            }

            is FrontierUiEvent.MaxEffortChanged -> updateConstraints(
                constraintsState.value.copy(maxEffort = event.effort),
            ) {
                userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_EFFORT, event.effort.name)
            }

            is FrontierUiEvent.MaxCostChanged -> updateConstraints(
                constraintsState.value.copy(maxCost = event.cost),
            ) {
                userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_COST, event.cost.name)
            }

            is FrontierUiEvent.PrioritizeSavedChanged -> updateConstraints(
                constraintsState.value.copy(prioritizeSaved = event.prioritize),
            ) {
                userPreferences.putBoolean(UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED, event.prioritize)
            }

            is FrontierUiEvent.ActionSelected -> {
                selectedActionId.value = event.actionId
            }

            is FrontierUiEvent.ComparisonToggled -> {
                val availableIds = uiState.value.actions.map(FrontierActionUiState::id).toSet()
                comparedActionIds.value = toggleComparedAction(
                    currentActionIds = comparedActionIds.value,
                    actionId = event.actionId,
                    availableActionIds = availableIds,
                )
            }

            FrontierUiEvent.ComparisonCleared -> {
                comparedActionIds.value = emptyList()
            }
        }
    }

    private fun updateConstraints(
        updated: PlanningConstraints,
        persist: suspend () -> Unit,
    ) {
        constraintsTouched.value = true
        constraintsState.value = updated
        viewModelScope.launch { persist() }
    }

    private suspend fun loadConstraints(): PlanningConstraints {
        val horizon = userPreferences.getInt(
            UserPreferences.KEY_FRONTIER_HORIZON_DAYS,
            DEFAULT_CONSTRAINTS.horizonDays,
        )
            ?.takeIf { it in ALLOWED_HORIZONS }
            ?: DEFAULT_CONSTRAINTS.horizonDays
        val maxEffort = userPreferences.getString(UserPreferences.KEY_FRONTIER_MAX_EFFORT)
            ?.let { stored -> EffortLevel.entries.firstOrNull { it.name == stored } }
            ?: DEFAULT_CONSTRAINTS.maxEffort
        val maxCost = userPreferences.getString(UserPreferences.KEY_FRONTIER_MAX_COST)
            ?.let { stored -> CostBand.entries.firstOrNull { it.name == stored } }
            ?: DEFAULT_CONSTRAINTS.maxCost
        val prioritizeSaved = userPreferences.getBoolean(
            UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED,
            DEFAULT_CONSTRAINTS.prioritizeSaved,
        )
        return PlanningConstraints(
            horizonDays = horizon,
            maxEffort = maxEffort,
            maxCost = maxCost,
            prioritizeSaved = prioritizeSaved,
        )
    }

    private fun buildState(
        inputs: FrontierInputs,
        constraints: PlanningConstraints,
        selectedActionId: String?,
        comparedActionIds: List<String>,
    ): FrontierUiState {
        val isSampleProfile = inputs.profile.facts.isEmpty()
        val activeProfile = inputs.profile.takeUnless { isSampleProfile } ?: sampleScenarioProvider.profile
        val activeOpportunities = inputs.pack?.opportunities?.takeIf { it.isNotEmpty() }
            ?: sampleScenarioProvider.opportunities
        val activePlan = inputs.plan?.let { plan ->
            ActivePlanSummaryUiState(
                title = plan.title,
                status = plan.status,
                statusLabel = when (plan.status) {
                    PlanStatus.PLANNED -> "Planned"
                    PlanStatus.IN_PROGRESS -> "In progress"
                    PlanStatus.PAUSED -> "Paused"
                    PlanStatus.COMPLETED -> "Work marked complete"
                },
            )
        }
        val isDemoProfile = sampleScenarioProvider.isPersistedDemoProfile(inputs.profile)
        val isSampleCatalog = inputs.pack == null ||
            sampleScenarioProvider.isLegacySyntheticPack(inputs.pack.metadata.id)
        val opportunitiesById = activeOpportunities.associateBy(Opportunity::id)
        val evaluation = actionFrontierEngine.evaluate(
            profile = activeProfile,
            opportunities = activeOpportunities,
            savedOpportunityIds = inputs.savedIds,
            constraints = constraints,
            today = inputs.today,
        )
        val actions = evaluation.actions.take(MAX_FRONTIER_ACTIONS).map { action ->
            val moveKind = action.template.moveKind()
            val estimateBasis = action.template.estimateBasis()
            val programmeTitle = (action.template.target as? ActionTarget.OpportunityRuleTarget)
                ?.let { opportunitiesById[it.opportunityId]?.title }
            val improvedIds = action.improvedOpportunityIds.toSet()
            val changedCheckCount = action.scenario.impacts
                .asSequence()
                .filter { it.opportunityId in improvedIds }
                .sumOf { impact ->
                    impact.changedRules.count { change ->
                        change.afterResult == RuleResult.PASS && change.beforeResult != RuleResult.PASS
                    }
                }
            val currentValue: FactValue? = when (val target = action.template.target) {
                is ActionTarget.ProfileFactTarget -> activeProfile.facts[target.factKey]?.value

                is ActionTarget.OpportunityRuleTarget -> activeProfile.ruleAssertions[
                    RuleRef(target.opportunityId, target.ruleId),
                ]?.value
            }
            FrontierActionUiState(
                id = action.template.id,
                title = action.template.userFacingTitle(programmeTitle, moveKind),
                currentLabel = currentValue?.displayValue() ?: when (action.template.target) {
                    is ActionTarget.ProfileFactTarget -> "Not set"
                    is ActionTarget.OpportunityRuleTarget -> "Not confirmed"
                },
                targetLabel = action.template.targetValue.displayValue(),
                newlyOpenCount = action.newlyOpenOpportunityIds.size,
                improvedCount = action.improvedOpportunityIds.size,
                savedDoorCount = action.improvedSavedOpportunityIds.size,
                timingUnverifiedCount = action.timingUnverifiedOpportunityIds.size,
                durationDays = action.template.minimumDurationDays,
                effort = action.template.effort,
                cost = action.template.costBand,
                durationLabel = if (action.template.estimateSourceLabel().contains("basis not stated")) {
                    "Planner assumes ${action.template.minimumDurationDays} days"
                } else {
                    "Est. ${action.template.minimumDurationDays} days"
                },
                effortLabel = action.template.effort.readable(),
                costLabel = action.template.costBand.readable(),
                explanation = "",
                explanationCode = action.explanationCode,
                doorTitles = action.improvedOpportunityIds.mapNotNull { opportunitiesById[it]?.title },
                doorComparisons = action.improvedOpportunityIds.mapNotNull { opportunityId ->
                    val opportunity = opportunitiesById[opportunityId] ?: return@mapNotNull null
                    val before = action.scenario.baseline[opportunityId]?.state ?: return@mapNotNull null
                    val after = action.scenario.simulated[opportunityId]?.state ?: return@mapNotNull null
                    FrontierDoorComparisonUiState(
                        id = opportunityId,
                        title = opportunity.title,
                        actualStateLabel = before.trustLabel(),
                        previewStateLabel = after.trustLabel(),
                        isNewlyOpen = opportunityId in action.newlyOpenOpportunityIds,
                        needsVerification = after == EligibilityState.NEEDS_INFO,
                        isSaved = opportunityId in action.improvedSavedOpportunityIds,
                        requiresDeadlineCheck = opportunityId in action.timingUnverifiedOpportunityIds,
                        actualState = before,
                        previewState = after,
                    )
                },
                kind = moveKind,
                familyId = action.template.familyId(moveKind),
                familyLabel = action.template.familyLabel(moveKind),
                scopeLabel = when (action.template.target) {
                    is ActionTarget.ProfileFactTarget -> "Profile-wide action"
                    is ActionTarget.OpportunityRuleTarget -> "Programme-specific check"
                },
                estimateSourceLabel = action.template.estimateSourceLabel(),
                impactBasisLabel = buildString {
                    append("Impact basis · encoded rule simulation")
                    if (action.timingUnverifiedOpportunityIds.isNotEmpty()) {
                        append(" · deadline not confirmed")
                    }
                },
                earliestDeadlineLabel = action.earliestAffectedDeadline?.let { deadline ->
                    "Earliest affected deadline · $deadline"
                },
                changedCheckCount = changedCheckCount,
                programmeTitle = programmeTitle,
                targetFactKey = action.template.targetFactKey,
                currentFallback = if (currentValue == null) {
                    when (action.template.target) {
                        is ActionTarget.ProfileFactTarget -> FrontierCurrentFallback.NOT_SET
                        is ActionTarget.OpportunityRuleTarget -> FrontierCurrentFallback.NOT_CONFIRMED
                    }
                } else {
                    null
                },
                scope = when (action.template.target) {
                    is ActionTarget.ProfileFactTarget -> FrontierScope.PROFILE
                    is ActionTarget.OpportunityRuleTarget -> FrontierScope.PROGRAMME
                },
                estimateBasis = estimateBasis,
                impactDeadlineUnconfirmed = action.timingUnverifiedOpportunityIds.isNotEmpty(),
                earliestAffectedDeadline = action.earliestAffectedDeadline,
                currentValue = currentValue,
                targetValue = action.template.targetValue,
                titleCode = action.template.titleCode(moveKind, programmeTitle),
            )
        }
        val exclusions = evaluation.exclusions.map { exclusion ->
            val programmeTitle = (exclusion.template.target as? ActionTarget.OpportunityRuleTarget)
                ?.let { opportunitiesById[it.opportunityId]?.title }
            FrontierExcludedActionUiState(
                id = exclusion.template.id,
                title = exclusion.template.title,
                reasons = FrontierExclusionReason.entries
                    .filter(exclusion.reasons::contains)
                    .map { it.readable() },
                reasonCodes = FrontierExclusionReason.entries.filter(exclusion.reasons::contains),
                titleCode = exclusion.template.titleCode(exclusion.template.moveKind(), programmeTitle),
                targetValue = exclusion.template.targetValue,
                programmeTitle = programmeTitle,
            )
        }
        val suggestedGrowthHorizonDays = evaluation.exclusions
            .asSequence()
            .filter { exclusion ->
                exclusion.reasons == setOf(FrontierExclusionReason.BEYOND_HORIZON) &&
                    exclusion.template.moveKind() != FrontierMoveKind.VERIFY
            }
            .map { exclusion -> exclusion.template.minimumDurationDays }
            .minOrNull()
            ?.let { minimumDurationDays ->
                ALLOWED_HORIZONS.sorted().firstOrNull { horizon ->
                    horizon > constraints.horizonDays && horizon >= minimumDurationDays
                }
            }
        val validSelectedActionId = selectedActionId
            ?.takeIf { selectedId -> actions.any { it.id == selectedId } }
            ?: actions.firstOrNull()?.id
        val validComparedActionIds = comparedActionIds.filter { comparedId -> actions.any { it.id == comparedId } }
        return FrontierUiState(
            isSampleMode = isSampleProfile,
            isDemoProfile = isDemoProfile,
            isSampleCatalog = isSampleCatalog,
            catalogDoorCount = activeOpportunities.size,
            horizonDays = constraints.horizonDays,
            maxEffort = constraints.maxEffort,
            maxCost = constraints.maxCost,
            prioritizeSaved = constraints.prioritizeSaved,
            activePlan = activePlan,
            actions = actions,
            exclusions = exclusions,
            suggestedGrowthHorizonDays = suggestedGrowthHorizonDays,
            selectedActionId = validSelectedActionId,
            comparedActionIds = validComparedActionIds,
        )
    }

    private fun EffortLevel.readable(): String = name.readableEnumName()

    private fun CostBand.readable(): String = name.readableEnumName()

    private fun ActionTemplate.moveKind(): FrontierMoveKind = when {
        targetActionability == Actionability.TIME_DEPENDENT -> FrontierMoveKind.PREPARE
        target is ActionTarget.OpportunityRuleTarget -> FrontierMoveKind.VERIFY
        else -> FrontierMoveKind.IMPROVE
    }

    private fun ActionTemplate.userFacingTitle(programmeTitle: String?, kind: FrontierMoveKind): String {
        if (kind != FrontierMoveKind.VERIFY || programmeTitle == null) return title
        return when (targetFactKey) {
            FactKey.AVAILABLE_FULL_PERIOD -> "Confirm your availability for $programmeTitle"
            FactKey.ENROLLED -> "Confirm your enrollment for $programmeTitle"
            else -> "Confirm a requirement for $programmeTitle"
        }
    }

    private fun ActionTemplate.titleCode(kind: FrontierMoveKind, programmeTitle: String?): FrontierTitleCode? = when {
        kind == FrontierMoveKind.VERIFY && programmeTitle != null -> when (targetFactKey) {
            FactKey.AVAILABLE_FULL_PERIOD -> FrontierTitleCode.VERIFY_AVAILABILITY
            FactKey.ENROLLED -> FrontierTitleCode.VERIFY_ENROLMENT
            else -> FrontierTitleCode.VERIFY_REQUIREMENT
        }

        title == "Complete the required first degree" -> FrontierTitleCode.COMPLETE_DEGREE

        title == "Use a cycle while eligible enrollment is active" -> FrontierTitleCode.ACTIVE_ENROLMENT

        targetFactKey == FactKey.EXPERIENCE_MONTHS && title.startsWith("Reach ") -> FrontierTitleCode.RELEVANT_EXPERIENCE

        targetFactKey == FactKey.IELTS_SCORE && title.startsWith("Raise IELTS to ") -> FrontierTitleCode.RAISE_IELTS

        else -> null
    }

    private fun ActionTemplate.familyId(kind: FrontierMoveKind): String = "${kind.name}:${targetFactKey.name}:$targetValue"

    private fun ActionTemplate.familyLabel(kind: FrontierMoveKind): String = when (targetFactKey) {
        FactKey.AVAILABLE_FULL_PERIOD -> "Confirm programme availability"

        FactKey.IELTS_SCORE -> "Improve English score"

        FactKey.DEGREE_COMPLETED -> "Complete degree requirement"

        FactKey.ENROLLED -> "Plan around active enrollment"

        FactKey.EXPERIENCE_MONTHS -> "Build relevant experience"

        else -> when (kind) {
            FrontierMoveKind.VERIFY -> "Confirm programme requirement"
            FrontierMoveKind.IMPROVE -> title
            FrontierMoveKind.PREPARE -> title
        }
    }

    private fun ActionTemplate.estimateSourceLabel(): String = when {
        sourceNote?.contains("Synthetic sample", ignoreCase = true) == true ->
            "Time estimate · synthetic demo"

        sourceNote?.contains("Porta planning estimates", ignoreCase = true) == true ||
            sourceNote?.contains("Porta assumption", ignoreCase = true) == true ->
            "Time estimate · Porta assumption"

        else -> "Time estimate · basis not stated"
    }

    private fun ActionTemplate.estimateBasis(): FrontierEstimateBasis = when {
        sourceNote?.contains("Synthetic sample", ignoreCase = true) == true -> FrontierEstimateBasis.SYNTHETIC

        sourceNote?.contains("Porta planning estimates", ignoreCase = true) == true ||
            sourceNote?.contains("Porta assumption", ignoreCase = true) == true -> FrontierEstimateBasis.PORTA_ASSUMPTION

        else -> FrontierEstimateBasis.UNSTATED
    }

    private fun FrontierExclusionReason.readable(): String = when (this) {
        FrontierExclusionReason.BEYOND_HORIZON -> "Needs more time than your horizon"
        FrontierExclusionReason.ABOVE_MAX_EFFORT -> "Above your effort limit"
        FrontierExclusionReason.ABOVE_MAX_COST -> "Above your cost limit"
        FrontierExclusionReason.MISSES_KNOWN_DEADLINES -> "Would miss a known deadline"
        FrontierExclusionReason.NO_VERIFIED_IMPACT -> "No checked requirement changes"
        FrontierExclusionReason.DOMINATED -> "A stronger milestone uses no more resources"
    }

    private fun String.readableEnumName(): String = lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::uppercaseChar)

    private data class FrontierInputs(
        val profile: ProfileSnapshot,
        val pack: OpportunityPackDocument?,
        val savedIds: Set<String>,
        val today: LocalDate,
        val plan: PlanItem?,
    )

    private companion object {
        const val MAX_FRONTIER_ACTIONS = 5
        val ALLOWED_HORIZONS = setOf(30, 90, 180, 365)
        val DEFAULT_CONSTRAINTS = PlanningConstraints(
            horizonDays = 90,
            maxEffort = EffortLevel.VERY_HIGH,
            maxCost = CostBand.UNKNOWN,
            prioritizeSaved = true,
        )
    }
}

internal fun toggleComparedAction(
    currentActionIds: List<String>,
    actionId: String,
    availableActionIds: Set<String>,
): List<String> = when {
    actionId !in availableActionIds -> currentActionIds
    actionId in currentActionIds -> currentActionIds - actionId
    currentActionIds.size < MAX_COMPARED_ACTIONS -> currentActionIds + actionId
    else -> currentActionIds
}
