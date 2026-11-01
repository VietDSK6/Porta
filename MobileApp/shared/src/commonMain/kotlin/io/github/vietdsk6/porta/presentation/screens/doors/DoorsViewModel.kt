package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityCycleStatus
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.components.displayCopy
import io.github.vietdsk6.porta.presentation.components.sourceReviewLabel
import io.github.vietdsk6.porta.presentation.components.sourceReviewNeedsAttention
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

class DoorsViewModel(
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val savedOpportunityRepository: SavedOpportunityRepository,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private var persistedSavedIds: Set<String> = emptySet()
    private val pendingSavedStates = mutableMapOf<String, Boolean>()
    private val savedMutationMutex = Mutex()
    private var isSampleProfile = true
    private var isSampleCatalog = true
    private var activeProfile = PortaSampleData.profile
    private var activeOpportunities = PortaSampleData.opportunities
    private var activeDate = currentLocalDateProvider.today()
    private var allDoors = evaluateDoors()
    private var lastObservedProfile: ProfileSnapshot? = null
    private var lastObservedDate: LocalDate? = null
    private var highlightedDoorIds: Set<String> = emptySet()
    private val _uiState = MutableStateFlow(buildState(DoorFilter.ALL))
    val uiState: StateFlow<DoorsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeProfile(),
                opportunityPackRepository.observeActive(),
                savedOpportunityRepository.observeSavedIds(),
                currentLocalDateProvider.currentDate,
            ) { profile, packResult, savedIds, today ->
                ObservedDoorsData(profile, packResult.getOrNull(), savedIds, today)
            }.collect { observed ->
                val profile = observed.profile
                val pack = observed.pack
                val savedIds = observed.savedIds
                val previousProfile = lastObservedProfile
                val previousDate = lastObservedDate
                val previousDoors = allDoors.associateBy(DoorCardUiState::id)
                persistedSavedIds = savedIds
                pendingSavedStates.entries.removeAll { (opportunityId, isSaved) ->
                    (opportunityId in savedIds) == isSaved
                }
                isSampleProfile = profile.facts.isEmpty()
                activeProfile = profile.takeUnless { isSampleProfile } ?: PortaSampleData.profile
                activeOpportunities = pack?.opportunities?.takeIf(List<Opportunity>::isNotEmpty)
                    ?: PortaSampleData.opportunities
                activeDate = observed.today
                isSampleCatalog = pack == null ||
                    pack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID
                allDoors = evaluateDoors()
                highlightedDoorIds = when {
                    previousProfile == null -> emptySet()

                    previousProfile != profile || previousDate != observed.today -> allDoors.filter { door ->
                        previousDoors[door.id]?.let { previous ->
                            previous.state != door.state || previous.primaryReason != door.primaryReason
                        } == true
                    }.mapTo(mutableSetOf(), DoorCardUiState::id)

                    else -> highlightedDoorIds
                }
                lastObservedProfile = profile
                lastObservedDate = observed.today
                _uiState.update { current -> buildState(current.selectedFilter) }
            }
        }
    }

    fun onUiEvent(event: DoorsUiEvent) {
        when (event) {
            is DoorsUiEvent.FilterChanged -> _uiState.value = buildState(event.filter)

            is DoorsUiEvent.SavedChanged -> persistSavedChange(event.opportunityId)

            DoorsUiEvent.DismissHighlights -> {
                highlightedDoorIds = emptySet()
                _uiState.update { current -> buildState(current.selectedFilter) }
            }
        }
    }

    private fun persistSavedChange(opportunityId: String) {
        val shouldSave = opportunityId !in effectiveSavedIds()
        pendingSavedStates[opportunityId] = shouldSave
        _uiState.update { current -> buildState(current.selectedFilter) }

        viewModelScope.launch {
            val result = savedMutationMutex.withLock {
                savedOpportunityRepository.setSaved(opportunityId = opportunityId, isSaved = shouldSave)
            }
            if (result.isFailure && pendingSavedStates[opportunityId] == shouldSave) {
                pendingSavedStates.remove(opportunityId)
                _uiState.update { current -> buildState(current.selectedFilter) }
            }
        }
    }

    private fun evaluateDoors(): List<DoorCardUiState> = activeOpportunities.map { opportunity ->
        val receipt = eligibilityEvaluator.evaluate(
            profile = activeProfile,
            opportunity = opportunity,
            today = activeDate,
        )
        val isArchived = receipt.cycleStatus == OpportunityCycleStatus.ARCHIVED
        val copy = receipt.displayCopy()
        DoorCardUiState(
            id = opportunity.id,
            title = opportunity.title,
            provider = opportunity.provider,
            categoryLabel = opportunity.category.label(),
            state = receipt.state,
            stateLabel = copy.statusLabel.uppercase(),
            deadlineLabel = opportunity.deadline.label(),
            checkedLabel = opportunity.sourceReviewLabel(activeDate),
            isArchived = isArchived,
            sourceReviewNeedsAttention = opportunity.sourceReviewNeedsAttention(activeDate),
            isSaved = opportunity.id in effectiveSavedIds(),
            primaryReason = copy.shortReason,
        )
    }

    private fun buildState(filter: DoorFilter): DoorsUiState {
        val savedIds = effectiveSavedIds()
        val refreshed = allDoors.map { it.copy(isSaved = it.id in savedIds) }
        val filtered = refreshed.filter { door ->
            when (filter) {
                DoorFilter.ALL -> true
                DoorFilter.OPEN -> door.state == EligibilityState.OPEN
                DoorFilter.GAPS -> door.state == EligibilityState.HAS_GAPS
                DoorFilter.VERIFY -> door.state == EligibilityState.NEEDS_INFO
                DoorFilter.NOT_AVAILABLE -> door.state == EligibilityState.NOT_ACTIONABLE
                DoorFilter.CLOSED -> door.state == EligibilityState.CLOSED && !door.isArchived
                DoorFilter.ARCHIVED -> door.isArchived
                DoorFilter.SAVED -> door.isSaved
            }
        }
        return DoorsUiState(
            isSampleMode = isSampleProfile,
            isSampleCatalog = isSampleCatalog,
            catalogDoorCount = refreshed.size,
            selectedFilter = filter,
            doors = filtered,
            openCount = refreshed.count { it.state == EligibilityState.OPEN },
            gapCount = refreshed.count { it.state == EligibilityState.HAS_GAPS },
            needsInfoCount = refreshed.count { it.state == EligibilityState.NEEDS_INFO },
            notAvailableCount = refreshed.count { it.state == EligibilityState.NOT_ACTIONABLE },
            closedCount = refreshed.count { it.state == EligibilityState.CLOSED && !it.isArchived },
            archivedCount = refreshed.count(DoorCardUiState::isArchived),
            sourceReviewDueCount = refreshed.count(DoorCardUiState::sourceReviewNeedsAttention),
            highlightedDoorIds = highlightedDoorIds,
        )
    }

    private fun effectiveSavedIds(): Set<String> = buildSet {
        addAll(persistedSavedIds)
        pendingSavedStates.forEach { (opportunityId, isSaved) ->
            if (isSaved) add(opportunityId) else remove(opportunityId)
        }
    }

    private fun Deadline.label(): String = when (this) {
        is Deadline.Known -> "Due $date"
        Deadline.Missing -> "Deadline missing"
        Deadline.Tba -> "Deadline TBA"
    }

    private fun OpportunityCategory.label(): String = when (this) {
        OpportunityCategory.SCHOLARSHIP -> "Scholarship"
        OpportunityCategory.INTERNSHIP -> "Internship"
        OpportunityCategory.ACADEMIC_PROGRAM -> "Academic programme"
        OpportunityCategory.STUDENT_CONTEST -> "Student contest"
        OpportunityCategory.FELLOWSHIP -> "Fellowship"
    }

    private data class ObservedDoorsData(
        val profile: ProfileSnapshot,
        val pack: OpportunityPackDocument?,
        val savedIds: Set<String>,
        val today: LocalDate,
    )
}
