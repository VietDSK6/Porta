package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.lifecycle.ViewModel
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DoorsViewModel(
    private val eligibilityEvaluator: EligibilityEvaluator,
) : ViewModel() {
    private val savedIds = PortaSampleData.savedOpportunityIds.toMutableSet()
    private val allDoors = evaluateDoors()
    private val _uiState = MutableStateFlow(buildState(DoorFilter.ALL))
    val uiState: StateFlow<DoorsUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: DoorsUiEvent) {
        when (event) {
            is DoorsUiEvent.FilterChanged -> _uiState.value = buildState(event.filter)

            is DoorsUiEvent.SavedChanged -> {
                if (!savedIds.add(event.opportunityId)) savedIds.remove(event.opportunityId)
                _uiState.update { current -> buildState(current.selectedFilter) }
            }
        }
    }

    private fun evaluateDoors(): List<DoorCardUiState> = PortaSampleData.opportunities.map { opportunity ->
        val receipt = eligibilityEvaluator.evaluate(
            profile = PortaSampleData.profile,
            opportunity = opportunity,
            today = PortaSampleData.referenceDate,
        )
        DoorCardUiState(
            id = opportunity.id,
            title = opportunity.title,
            provider = opportunity.provider,
            state = receipt.state,
            deadlineLabel = opportunity.deadline.label(),
            checkedLabel = "Checked ${opportunity.lastCheckedOn}",
            isSaved = opportunity.id in savedIds,
            primaryReason = receipt.rules.firstOrNull()?.explanation.orEmpty(),
        )
    }

    private fun buildState(filter: DoorFilter): DoorsUiState {
        val refreshed = allDoors.map { it.copy(isSaved = it.id in savedIds) }
        val filtered = refreshed.filter { door ->
            when (filter) {
                DoorFilter.ALL -> true
                DoorFilter.OPEN -> door.state == EligibilityState.OPEN
                DoorFilter.GAPS -> door.state == EligibilityState.HAS_GAPS
                DoorFilter.VERIFY -> door.state == EligibilityState.NEEDS_INFO
                DoorFilter.SAVED -> door.isSaved
            }
        }
        return DoorsUiState(
            selectedFilter = filter,
            doors = filtered,
            openCount = refreshed.count { it.state == EligibilityState.OPEN },
            gapCount = refreshed.count { it.state == EligibilityState.HAS_GAPS },
            needsInfoCount = refreshed.count { it.state == EligibilityState.NEEDS_INFO },
        )
    }

    private fun Deadline.label(): String = when (this) {
        is Deadline.Known -> "Due $date"
        Deadline.Missing -> "Deadline missing"
        Deadline.Tba -> "Deadline TBA"
    }
}
