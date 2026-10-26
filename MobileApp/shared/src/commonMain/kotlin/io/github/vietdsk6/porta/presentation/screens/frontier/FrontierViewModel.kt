package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.presentation.components.trustLabel
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class FrontierViewModel(
    private val actionFrontierEngine: ActionFrontierEngine,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val savedOpportunityRepository: SavedOpportunityRepository,
    private val userPreferences: UserPreferences,
    planRepository: PlanRepository,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private var activeProfile = PortaSampleData.profile
    private var isSampleProfile = true
    private var isSampleCatalog = true
    private var activeOpportunities = PortaSampleData.opportunities
    private var activeDate = currentLocalDateProvider.today()
    private var persistedSavedIds = emptySet<String>()
    private var activePlan: ActivePlanSummaryUiState? = null
    private var selectedActionId: String? = null
    private var constraintsTouched = false
    private var constraints = DEFAULT_CONSTRAINTS
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<FrontierUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = loadConstraints()
            if (!constraintsTouched) {
                constraints = loaded
                _uiState.value = buildState()
            }
        }
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
                savedOpportunityRepository.observeSavedIds(),
                currentLocalDateProvider.currentDate,
            ) { profile, packResult, savedIds, today ->
                FrontierInputs(profile, packResult.getOrNull(), savedIds, today)
            }.collect { inputs ->
                val profile = inputs.profile
                val pack = inputs.pack
                isSampleProfile = profile.facts.isEmpty()
                activeProfile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile
                activeOpportunities = pack?.opportunities?.takeIf(List<Opportunity>::isNotEmpty)
                    ?: PortaSampleData.opportunities
                persistedSavedIds = inputs.savedIds
                activeDate = inputs.today
                isSampleCatalog = pack == null ||
                    pack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID
                _uiState.value = buildState()
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
                _uiState.value = buildState()
            }
        }
    }

    fun onUiEvent(event: FrontierUiEvent) {
        when (event) {
            is FrontierUiEvent.HorizonChanged -> {
                if (event.days !in ALLOWED_HORIZONS) return
                updateConstraints(constraints.copy(horizonDays = event.days)) {
                    userPreferences.putInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS, event.days)
                }
            }

            is FrontierUiEvent.MaxEffortChanged -> updateConstraints(
                constraints.copy(maxEffort = event.effort),
            ) {
                userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_EFFORT, event.effort.name)
            }

            is FrontierUiEvent.MaxCostChanged -> updateConstraints(
                constraints.copy(maxCost = event.cost),
            ) {
                userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_COST, event.cost.name)
            }

            is FrontierUiEvent.PrioritizeSavedChanged -> updateConstraints(
                constraints.copy(prioritizeSaved = event.prioritize),
            ) {
                userPreferences.putBoolean(UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED, event.prioritize)
            }

            is FrontierUiEvent.ActionSelected -> {
                selectedActionId = event.actionId
                _uiState.value = _uiState.value.copy(selectedActionId = event.actionId)
            }
        }
    }

    private fun updateConstraints(
        updated: PlanningConstraints,
        persist: suspend () -> Unit,
    ) {
        constraintsTouched = true
        constraints = updated
        _uiState.value = buildState()
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

    private fun buildState(): FrontierUiState {
        val opportunitiesById = activeOpportunities.associateBy(Opportunity::id)
        val evaluation = actionFrontierEngine.evaluate(
            profile = activeProfile,
            opportunities = activeOpportunities,
            savedOpportunityIds = persistedSavedIds,
            constraints = constraints,
            today = activeDate,
        )
        val actions = evaluation.actions.map { action ->
            val moveKind = action.template.moveKind()
            FrontierActionUiState(
                id = action.template.id,
                title = action.template.title,
                currentLabel = when (val target = action.template.target) {
                    is ActionTarget.ProfileFactTarget -> activeProfile.facts[target.factKey]
                        ?.value
                        ?.displayValue()
                        ?: "Not set"

                    is ActionTarget.OpportunityRuleTarget -> activeProfile.ruleAssertions[
                        RuleRef(target.opportunityId, target.ruleId),
                    ]?.value?.displayValue() ?: "Not confirmed"
                },
                targetLabel = action.template.targetValue.displayValue(),
                newlyOpenCount = action.newlyOpenOpportunityIds.size,
                improvedCount = action.improvedOpportunityIds.size,
                savedDoorCount = action.improvedSavedOpportunityIds.size,
                timingUnverifiedCount = action.timingUnverifiedOpportunityIds.size,
                durationDays = action.template.minimumDurationDays,
                effort = action.template.effort,
                cost = action.template.costBand,
                durationLabel = "Est. ${action.template.minimumDurationDays} days",
                effortLabel = action.template.effort.readable(),
                costLabel = action.template.costBand.readable(),
                explanation = buildString {
                    append(
                        when (moveKind) {
                            FrontierMoveKind.VERIFY ->
                                "Would satisfy this programme-specific check if confirmed."

                            FrontierMoveKind.IMPROVE ->
                                "Would address an encoded requirement using a profile fact you can change."

                            FrontierMoveKind.PREPARE ->
                                "Would address an encoded requirement after a time-dependent milestone."
                        },
                    )
                    if (action.newlyOpenOpportunityIds.isNotEmpty()) {
                        append(" ")
                        append(action.newlyOpenOpportunityIds.size)
                        append(" would pass current checks.")
                    }
                    if (action.timingUnverifiedOpportunityIds.isNotEmpty()) {
                        append(" ")
                        append(action.timingUnverifiedOpportunityIds.size)
                        append(
                            if (action.timingUnverifiedOpportunityIds.size == 1) {
                                " still needs a deadline check."
                            } else {
                                " still need a deadline check."
                            },
                        )
                    }
                },
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
                        isSaved = opportunityId in action.improvedSavedOpportunityIds,
                        requiresDeadlineCheck = opportunityId in action.timingUnverifiedOpportunityIds,
                    )
                },
                kind = moveKind,
                familyId = action.template.familyId(moveKind),
                familyLabel = action.template.familyLabel(moveKind),
                scopeLabel = when (val target = action.template.target) {
                    is ActionTarget.ProfileFactTarget -> buildString {
                        append("Profile-wide · ")
                        append(action.improvedOpportunityIds.size)
                        append(if (action.improvedOpportunityIds.size == 1) " door" else " doors")
                    }

                    is ActionTarget.OpportunityRuleTarget ->
                        "${opportunitiesById[target.opportunityId]?.title ?: "One door"} · programme-specific"
                },
                estimateSourceLabel = action.template.estimateSourceLabel(),
                impactBasisLabel = buildString {
                    append("Impact basis · encoded rule simulation")
                    if (action.timingUnverifiedOpportunityIds.isNotEmpty()) {
                        append(" · deadline not confirmed")
                    }
                },
            )
        }
        val exclusions = evaluation.exclusions.map { exclusion ->
            FrontierExcludedActionUiState(
                id = exclusion.template.id,
                title = exclusion.template.title,
                reasons = FrontierExclusionReason.entries
                    .filter(exclusion.reasons::contains)
                    .map { it.readable() },
            )
        }
        selectedActionId = selectedActionId
            ?.takeIf { selectedId -> actions.any { it.id == selectedId } }
            ?: actions.firstOrNull()?.id
        return FrontierUiState(
            isSampleMode = isSampleProfile,
            isSampleCatalog = isSampleCatalog,
            catalogDoorCount = activeOpportunities.size,
            horizonDays = constraints.horizonDays,
            maxEffort = constraints.maxEffort,
            maxCost = constraints.maxCost,
            prioritizeSaved = constraints.prioritizeSaved,
            activePlan = activePlan,
            actions = actions,
            exclusions = exclusions,
            selectedActionId = selectedActionId,
        )
    }

    private fun EffortLevel.readable(): String = name.readableEnumName()

    private fun CostBand.readable(): String = name.readableEnumName()

    private fun ActionTemplate.moveKind(): FrontierMoveKind = when {
        targetActionability == Actionability.TIME_DEPENDENT -> FrontierMoveKind.PREPARE
        target is ActionTarget.OpportunityRuleTarget -> FrontierMoveKind.VERIFY
        else -> FrontierMoveKind.IMPROVE
    }

    private fun ActionTemplate.familyId(kind: FrontierMoveKind): String = "${kind.name}:${targetFactKey.name}:${targetValue.displayValue()}"

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
    )

    private companion object {
        val ALLOWED_HORIZONS = setOf(30, 90, 180, 365)
        val DEFAULT_CONSTRAINTS = PlanningConstraints(
            horizonDays = 90,
            maxEffort = EffortLevel.VERY_HIGH,
            maxCost = CostBand.UNKNOWN,
            prioritizeSaved = true,
        )
    }
}
