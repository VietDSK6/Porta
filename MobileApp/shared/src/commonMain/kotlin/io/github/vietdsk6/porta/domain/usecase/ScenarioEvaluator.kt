package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.DoorStateDelta
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioImpactReceipt
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import io.github.vietdsk6.porta.domain.model.ScenarioResult
import io.github.vietdsk6.porta.domain.model.ScenarioRuleDelta
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import kotlinx.datetime.LocalDate

/**
 * Compares an actual profile with a temporary counterfactual overlay.
 *
 * Evaluation is pure: overrides are applied to an in-memory snapshot and never written to profile storage. The result
 * retains both baseline and simulated receipts so every visible delta remains traceable to the same encoded rules.
 */
class ScenarioEvaluator(
    private val eligibilityEvaluator: EligibilityEvaluator,
) {
    /** Evaluates global fact overrides without mutating [profile]. */
    fun evaluate(
        profile: ProfileSnapshot,
        overrides: Map<FactKey, FactValue>,
        opportunities: List<Opportunity>,
        today: LocalDate,
    ): ScenarioResult = evaluate(
        profile = profile,
        overrides = ScenarioOverrides(profileFacts = overrides),
        opportunities = opportunities,
        today = today,
    )

    /** Evaluates global and programme-scoped overrides without mutating [profile]. */
    fun evaluate(
        profile: ProfileSnapshot,
        overrides: ScenarioOverrides,
        opportunities: List<Opportunity>,
        today: LocalDate,
    ): ScenarioResult {
        val opportunityById = opportunities.associateBy(Opportunity::id)
        val scopedRules = overrides.ruleAssertions.keys.associateWith { ruleRef ->
            opportunityById[ruleRef.opportunityId]
                ?.eligibilityRule
                ?.findAtomicRule(ruleRef.ruleId)
                ?: error(
                    "Scenario assertion references missing rule " +
                        "${ruleRef.opportunityId}/${ruleRef.ruleId}.",
                )
        }
        val simulatedProfile = profile.withOverrides(
            overrides = overrides,
            scopedRules = scopedRules,
        )
        val baseline = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(profile, opportunity, today)
        }
        val simulated = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(simulatedProfile, opportunity, today)
        }
        val deltas = opportunities.mapNotNull { opportunity ->
            val before = baseline.getValue(opportunity.id).state
            val after = simulated.getValue(opportunity.id).state
            if (before == after) {
                null
            } else {
                DoorStateDelta(
                    opportunityId = opportunity.id,
                    before = before,
                    after = after,
                )
            }
        }
        val impacts = opportunities.mapNotNull impact@{ opportunity ->
            val beforeReceipt = baseline.getValue(opportunity.id)
            val afterReceipt = simulated.getValue(opportunity.id)
            val afterRules = afterReceipt.rules.associateBy { it.ruleId }
            val changedRules = beforeReceipt.rules.mapNotNull rule@{ beforeRule ->
                val afterRule = afterRules[beforeRule.ruleId] ?: return@rule null
                if (beforeRule.result == afterRule.result) {
                    return@rule null
                }
                ScenarioRuleDelta(
                    ruleId = beforeRule.ruleId,
                    explanation = afterRule.explanation,
                    factKey = afterRule.factKey,
                    beforeResult = beforeRule.result,
                    afterResult = afterRule.result,
                    beforeActual = beforeRule.actual,
                    afterActual = afterRule.actual,
                    expected = afterRule.expected,
                    sourceNote = afterRule.sourceNote,
                )
            }
            if (changedRules.isEmpty()) return@impact null
            ScenarioImpactReceipt(
                opportunityId = opportunity.id,
                beforeState = beforeReceipt.state,
                afterState = afterReceipt.state,
                changedRules = changedRules,
                remainingRules = opportunity.eligibilityRule.remainingRuleReceipts(afterRules),
                deadline = afterReceipt.deadline,
            )
        }

        return ScenarioResult(
            baseline = baseline,
            simulated = simulated,
            deltas = deltas,
            impacts = impacts,
        )
    }

    /**
     * Returns unresolved leaf receipts while preserving the rule tree's ALL/ANY semantics.
     * Once any alternative branch passes, its sibling branches are no longer blockers.
     */
    private fun RuleNode.remainingRuleReceipts(
        receiptsByRuleId: Map<String, RuleReceipt>,
    ): List<RuleReceipt> = when (this) {
        is RuleNode.All -> children.flatMap { child ->
            child.remainingRuleReceipts(receiptsByRuleId)
        }

        is RuleNode.Any -> if (children.any { child -> child.isSatisfied(receiptsByRuleId) }) {
            emptyList()
        } else {
            children.flatMap { child -> child.remainingRuleReceipts(receiptsByRuleId) }
        }

        is RuleNode.Atomic,
        is RuleNode.ManualCheck,
        -> listOfNotNull(receiptsByRuleId[id]).filter { receipt ->
            receipt.result != RuleResult.PASS
        }
    }

    private fun RuleNode.isSatisfied(
        receiptsByRuleId: Map<String, RuleReceipt>,
    ): Boolean = when (this) {
        is RuleNode.All -> children.all { child -> child.isSatisfied(receiptsByRuleId) }

        is RuleNode.Any -> children.any { child -> child.isSatisfied(receiptsByRuleId) }

        is RuleNode.Atomic,
        is RuleNode.ManualCheck,
        -> receiptsByRuleId[id]?.result == RuleResult.PASS
    }
}
