package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

data class ActionTemplate(
    val id: String,
    val title: String,
    val targetFactKey: FactKey,
    val targetValue: FactValue,
    val targetActionability: Actionability,
    val minimumDurationDays: Int,
    val effort: EffortLevel,
    val costBand: CostBand,
    val completionEvidenceTypes: Set<String> = emptySet(),
    val sourceNote: String? = null,
)

enum class EffortLevel(val rank: Int) {
    VERY_LOW(1),
    LOW(2),
    MEDIUM(3),
    HIGH(4),
    VERY_HIGH(5),
}

enum class CostBand(val rank: Int) {
    FREE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    UNKNOWN(4),
}

data class PlanningConstraints(
    val horizonDays: Int,
    val maxEffort: EffortLevel,
    val maxCost: CostBand,
    val prioritizeSaved: Boolean = true,
)

data class DoorStateDelta(
    val opportunityId: String,
    val before: EligibilityState,
    val after: EligibilityState,
)

data class ScenarioResult(
    val baseline: Map<String, EligibilityReceipt>,
    val simulated: Map<String, EligibilityReceipt>,
    val deltas: List<DoorStateDelta>,
)

data class FrontierAction(
    val template: ActionTemplate,
    val newlyOpenOpportunityIds: List<String>,
    val newlyOpenSavedOpportunityIds: List<String>,
    val earliestAffectedDeadline: LocalDate?,
    val scenario: ScenarioResult,
    val explanation: String,
)
