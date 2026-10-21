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
    val improvedOpportunityIds: List<String>,
    val improvedSavedOpportunityIds: List<String>,
    val newlyOpenOpportunityIds: List<String>,
    val newlyOpenSavedOpportunityIds: List<String>,
    val timingUnverifiedOpportunityIds: List<String>,
    val earliestAffectedDeadline: LocalDate?,
    val scenario: ScenarioResult,
    val explanation: String,
)

/**
 * Returns opportunities where the scenario resolves a failed encoded rule for [targetFactKey].
 * The door may still need a manual source check, so this is intentionally broader than "open".
 */
fun ScenarioResult.resolvedHardGapOpportunityIds(targetFactKey: FactKey): List<String> = baseline
    .mapNotNull { (opportunityId, beforeReceipt) ->
        val afterReceipt = simulated[opportunityId] ?: return@mapNotNull null
        val afterRules = afterReceipt.rules.associateBy(RuleReceipt::ruleId)
        val resolved = beforeReceipt.rules.any { beforeRule ->
            beforeRule.factKey == targetFactKey &&
                beforeRule.result == RuleResult.FAIL &&
                afterRules[beforeRule.ruleId]?.result == RuleResult.PASS
        }
        opportunityId.takeIf { resolved }
    }
    .sorted()
