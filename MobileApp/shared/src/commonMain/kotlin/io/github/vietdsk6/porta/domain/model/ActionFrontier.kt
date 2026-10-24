package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

data class ActionTemplate(
    val id: String,
    val title: String,
    val target: ActionTarget,
    val targetActionability: Actionability,
    val minimumDurationDays: Int,
    val effort: EffortLevel,
    val costBand: CostBand,
    val completionEvidenceTypes: Set<String> = emptySet(),
    val sourceNote: String? = null,
) {
    val targetFactKey: FactKey
        get() = target.factKey

    val targetValue: FactValue
        get() = target.value

    /**
     * Compatibility constructor for profile-wide milestones authored before scoped rule targets.
     */
    constructor(
        id: String,
        title: String,
        targetFactKey: FactKey,
        targetValue: FactValue,
        targetActionability: Actionability,
        minimumDurationDays: Int,
        effort: EffortLevel,
        costBand: CostBand,
        completionEvidenceTypes: Set<String> = emptySet(),
        sourceNote: String? = null,
    ) : this(
        id = id,
        title = title,
        target = ActionTarget.ProfileFactTarget(
            factKey = targetFactKey,
            value = targetValue,
        ),
        targetActionability = targetActionability,
        minimumDurationDays = minimumDurationDays,
        effort = effort,
        costBand = costBand,
        completionEvidenceTypes = completionEvidenceTypes,
        sourceNote = sourceNote,
    )
}

sealed interface ActionTarget {
    val factKey: FactKey
    val value: FactValue

    data class ProfileFactTarget(
        override val factKey: FactKey,
        override val value: FactValue,
    ) : ActionTarget

    data class OpportunityRuleTarget(
        val opportunityId: String,
        val ruleId: String,
        val ruleFingerprint: String,
        override val factKey: FactKey,
        override val value: FactValue,
    ) : ActionTarget
}

fun ActionTarget.asScenarioOverrides(): ScenarioOverrides = when (this) {
    is ActionTarget.ProfileFactTarget -> ScenarioOverrides(
        profileFacts = mapOf(factKey to value),
    )

    is ActionTarget.OpportunityRuleTarget -> ScenarioOverrides(
        ruleAssertions = mapOf(RuleRef(opportunityId, ruleId) to value),
    )
}

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

data class ActionFrontierEvaluation(
    val actions: List<FrontierAction>,
    val exclusions: List<FrontierExclusion>,
)

data class FrontierExclusion(
    val template: ActionTemplate,
    val reasons: Set<FrontierExclusionReason>,
)

enum class FrontierExclusionReason {
    BEYOND_HORIZON,
    ABOVE_MAX_EFFORT,
    ABOVE_MAX_COST,
    MISSES_KNOWN_DEADLINES,
    NO_VERIFIED_IMPACT,
    DOMINATED,
}

/**
 * Returns whether an action with [minimumDurationDays] can finish by a published deadline.
 * Unknown deadlines remain feasible for comparison, but callers must keep them visibly unverified.
 */
fun Opportunity.isFeasibleForPlanning(
    today: LocalDate,
    minimumDurationDays: Int,
): Boolean = when (val publishedDeadline = deadline) {
    is Deadline.Known -> publishedDeadline.date.toEpochDays() - today.toEpochDays() >= minimumDurationDays

    Deadline.Missing,
    Deadline.Tba,
    -> true
}

/**
 * Returns opportunities where the scenario addresses the encoded rule represented by [target].
 * Profile-wide milestones must turn a failure into a pass. A rule-scoped confirmation may also
 * turn an unknown rule into a pass because its purpose is to collect programme-specific evidence.
 * The door may still need a manual source check, so this is intentionally broader than "open".
 */
fun ScenarioResult.resolvedHardGapOpportunityIds(target: ActionTarget): List<String> = baseline
    .mapNotNull { (opportunityId, beforeReceipt) ->
        if (target is ActionTarget.OpportunityRuleTarget && target.opportunityId != opportunityId) {
            return@mapNotNull null
        }
        val afterReceipt = simulated[opportunityId] ?: return@mapNotNull null
        val afterRules = afterReceipt.rules.associateBy(RuleReceipt::ruleId)
        val resolved = beforeReceipt.rules.any { beforeRule ->
            val targetsRule = target !is ActionTarget.OpportunityRuleTarget ||
                beforeRule.ruleId == target.ruleId
            val isScopedUnknown = target is ActionTarget.OpportunityRuleTarget &&
                beforeRule.result == RuleResult.UNKNOWN
            val addressableBefore = beforeRule.result == RuleResult.FAIL || isScopedUnknown
            beforeRule.factKey == target.factKey &&
                targetsRule &&
                addressableBefore &&
                afterRules[beforeRule.ruleId]?.result == RuleResult.PASS
        }
        opportunityId.takeIf { resolved }
    }
    .sorted()
