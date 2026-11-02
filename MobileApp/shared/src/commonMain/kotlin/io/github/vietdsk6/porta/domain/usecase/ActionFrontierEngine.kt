package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionFrontierEvaluation
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FrontierAction
import io.github.vietdsk6.porta.domain.model.FrontierExclusion
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import io.github.vietdsk6.porta.domain.model.fingerprint
import io.github.vietdsk6.porta.domain.model.isFeasibleForPlanning
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import kotlinx.datetime.LocalDate

/**
 * Builds Porta's deterministic Action Frontier from curated, ethically actionable templates.
 *
 * Each candidate is evaluated as a temporary scenario, filtered by the user's horizon/effort/cost constraints and
 * relevant deadlines, reduced to a Pareto frontier, then ordered with explicit stable tie-breakers. Raw scores are not
 * exposed because the ranking is meaningful only under the selected constraints.
 */
class ActionFrontierEngine(
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val scenarioEvaluator: ScenarioEvaluator,
) {
    /** Returns the first unmet, ethically actionable check that can still fit its published deadline. */
    fun relevantActionFor(
        profile: ProfileSnapshot,
        opportunity: Opportunity,
        today: LocalDate,
    ): ActionTemplate? {
        val templates = opportunity.actionTemplates.associateBy(ActionTemplate::id)
        return eligibilityEvaluator.evaluate(profile, opportunity, today)
            .rules
            .mapNotNull { receipt ->
                val template = receipt.actionTemplateId?.let(templates::get) ?: return@mapNotNull null
                RelevantCandidate(receipt = receipt, template = template)
            }
            .filter { candidate ->
                candidate.template.targetsReceipt(opportunity.id, candidate.receipt) &&
                    candidate.template.canAddress(candidate.receipt.result) &&
                    candidate.template.isEthicallyActionable() &&
                    opportunity.isFeasibleForPlanning(today, candidate.template.minimumDurationDays)
            }
            .minWithOrNull(
                compareBy<RelevantCandidate> { if (it.receipt.result == RuleResult.FAIL) 0 else 1 }
                    .thenBy { it.template.minimumDurationDays }
                    .thenBy { it.template.effort.rank }
                    .thenBy { it.template.costBand.rank }
                    .thenBy { it.template.id },
            )
            ?.template
    }

    /** Returns only the ranked non-dominated actions for the supplied constraints. */
    fun build(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        savedOpportunityIds: Set<String>,
        constraints: PlanningConstraints,
        today: LocalDate,
    ): List<FrontierAction> = evaluate(
        profile = profile,
        opportunities = opportunities,
        savedOpportunityIds = savedOpportunityIds,
        constraints = constraints,
        today = today,
    ).actions

    /** Returns ranked frontier actions together with explainable exclusions for every rejected candidate. */
    fun evaluate(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        savedOpportunityIds: Set<String>,
        constraints: PlanningConstraints,
        today: LocalDate,
    ): ActionFrontierEvaluation {
        val opportunityById = opportunities.associateBy(Opportunity::id)
        val baseline = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(profile, opportunity, today)
        }
        val candidates = opportunities.flatMap { opportunity ->
            val templates = opportunity.actionTemplates.associateBy(ActionTemplate::id)
            baseline.getValue(opportunity.id).rules.mapNotNull { receipt ->
                receipt.actionTemplateId
                    ?.let(templates::get)
                    ?.takeIf { template ->
                        template.targetsReceipt(opportunityId = opportunity.id, receipt = receipt)
                    }
                    ?.takeIf { template -> template.canAddress(receipt.result) }
            }
        }
            .groupBy { it.signature() }
            .values
            .map { matching ->
                matching.minWith(
                    compareBy<ActionTemplate>(ActionTemplate::minimumDurationDays)
                        .thenBy { it.effort.rank }
                        .thenBy { it.costBand.rank }
                        .thenBy(ActionTemplate::id),
                )
            }
            .filter { it.isEthicallyActionable() }
        val exclusions = mutableListOf<FrontierExclusion>()
        val impacted = candidates.mapNotNull { template ->
            val reasons = linkedSetOf<FrontierExclusionReason>()
            if (template.minimumDurationDays > constraints.horizonDays) {
                reasons += FrontierExclusionReason.BEYOND_HORIZON
            }
            if (template.effort.rank > constraints.maxEffort.rank) {
                reasons += FrontierExclusionReason.ABOVE_MAX_EFFORT
            }
            if (template.costBand.rank > constraints.maxCost.rank) {
                reasons += FrontierExclusionReason.ABOVE_MAX_COST
            }

            val scenario = scenarioEvaluator.evaluate(
                profile = profile,
                overrides = template.scenarioOverrides(opportunityById),
                opportunities = opportunities,
                today = today,
            )
            val resolvedHardGaps = scenario.resolvedHardGapOpportunityIds(template.target)
            val improved = resolvedHardGaps
                .filter { opportunityId ->
                    opportunityById[opportunityId]
                        ?.isFeasibleForPlanning(today, template.minimumDurationDays)
                        ?: false
                }

            if (resolvedHardGaps.isEmpty()) {
                reasons += FrontierExclusionReason.NO_VERIFIED_IMPACT
            } else if (improved.isEmpty()) {
                reasons += FrontierExclusionReason.MISSES_KNOWN_DEADLINES
            }

            if (reasons.isNotEmpty()) {
                exclusions += FrontierExclusion(template = template, reasons = reasons)
                return@mapNotNull null
            }

            val improvedSet = improved.toSet()
            val newlyOpen = scenario.deltas
                .filter { delta ->
                    delta.opportunityId in improvedSet &&
                        delta.before != EligibilityState.OPEN &&
                        delta.after == EligibilityState.OPEN
                }
                .map { it.opportunityId }
                .sorted()
            val improvedSaved = improved.filter(savedOpportunityIds::contains)
            val newlyOpenSaved = newlyOpen.filter(savedOpportunityIds::contains)
            val earliestDeadline = improved
                .mapNotNull { id ->
                    (opportunityById.getValue(id).deadline as? Deadline.Known)?.date
                }
                .minOrNull()
            val timingUnverified = improved.filter { id ->
                opportunityById.getValue(id).deadline !is Deadline.Known
            }

            FrontierAction(
                template = template,
                improvedOpportunityIds = improved,
                improvedSavedOpportunityIds = improvedSaved,
                newlyOpenOpportunityIds = newlyOpen,
                newlyOpenSavedOpportunityIds = newlyOpenSaved,
                timingUnverifiedOpportunityIds = timingUnverified,
                earliestAffectedDeadline = earliestDeadline,
                scenario = scenario,
                explanation = explanationFor(
                    template = template,
                    improvedCount = improved.size,
                    newlyOpenCount = newlyOpen.size,
                    timingUnverifiedCount = timingUnverified.size,
                ),
            )
        }

        val dominated = impacted.filter { candidate ->
            impacted.any { other ->
                other !== candidate && dominates(
                    a = other,
                    b = candidate,
                    prioritizeSaved = constraints.prioritizeSaved,
                )
            }
        }
        exclusions += dominated.map { action ->
            FrontierExclusion(
                template = action.template,
                reasons = setOf(FrontierExclusionReason.DOMINATED),
            )
        }
        val frontier = impacted.filter { candidate -> dominated.none { it === candidate } }

        val ranked = frontier.sortedWith(
            compareByDescending<FrontierAction> {
                if (constraints.prioritizeSaved) it.newlyOpenSavedOpportunityIds.size else 0
            }
                .thenByDescending {
                    if (constraints.prioritizeSaved) it.improvedSavedOpportunityIds.size else 0
                }
                .thenByDescending { it.newlyOpenOpportunityIds.size }
                .thenByDescending { it.improvedOpportunityIds.size }
                .thenBy { it.earliestAffectedDeadline?.toEpochDays() ?: Int.MAX_VALUE }
                .thenBy { it.template.minimumDurationDays }
                .thenBy { it.template.effort.rank }
                .thenBy { it.template.costBand.rank }
                .thenBy { it.template.id },
        )
        return ActionFrontierEvaluation(
            actions = ranked,
            exclusions = exclusions.sortedBy { it.template.id },
        )
    }

    private fun dominates(
        a: FrontierAction,
        b: FrontierAction,
        prioritizeSaved: Boolean,
    ): Boolean {
        val savedNoWorse = !prioritizeSaved ||
            (
                a.newlyOpenSavedOpportunityIds.size >= b.newlyOpenSavedOpportunityIds.size &&
                    a.improvedSavedOpportunityIds.size >= b.improvedSavedOpportunityIds.size
                )
        val savedStrictlyBetter = prioritizeSaved &&
            (
                a.newlyOpenSavedOpportunityIds.size > b.newlyOpenSavedOpportunityIds.size ||
                    a.improvedSavedOpportunityIds.size > b.improvedSavedOpportunityIds.size
                )
        val noWorse =
            savedNoWorse &&
                a.newlyOpenOpportunityIds.size >= b.newlyOpenOpportunityIds.size &&
                a.improvedOpportunityIds.size >= b.improvedOpportunityIds.size &&
                a.template.minimumDurationDays <= b.template.minimumDurationDays &&
                a.template.effort.rank <= b.template.effort.rank &&
                a.template.costBand.rank <= b.template.costBand.rank
        val strictlyBetter =
            savedStrictlyBetter ||
                a.newlyOpenOpportunityIds.size > b.newlyOpenOpportunityIds.size ||
                a.improvedOpportunityIds.size > b.improvedOpportunityIds.size ||
                a.template.minimumDurationDays < b.template.minimumDurationDays ||
                a.template.effort.rank < b.template.effort.rank ||
                a.template.costBand.rank < b.template.costBand.rank
        return noWorse && strictlyBetter
    }

    private fun explanationFor(
        template: ActionTemplate,
        improvedCount: Int,
        newlyOpenCount: Int,
        timingUnverifiedCount: Int,
    ): String {
        val impact = buildString {
            append("Addresses a published check in $improvedCount ")
            append(if (improvedCount == 1) "opportunity" else "opportunities")
            append(". ")
            if (newlyOpenCount > 0) {
                append("$newlyOpenCount would pass their current checks; ")
            }
            if (timingUnverifiedCount > 0) {
                append("$timingUnverifiedCount still need deadline verification. ")
            }
        }
        return impact +
            "Target: ${template.targetValue.displayValue()}. " +
            "Estimated duration: ${template.minimumDurationDays} days. " +
            "Effort: ${template.effort.name.lowercase()}. Cost: ${template.costBand.name.lowercase()}."
    }

    private fun ActionTemplate.signature(): String = when (val actionTarget = target) {
        is ActionTarget.ProfileFactTarget -> {
            "profile:${actionTarget.factKey.name}:${actionTarget.value.displayValue()}"
        }

        is ActionTarget.OpportunityRuleTarget -> {
            "rule:${actionTarget.opportunityId}:${actionTarget.ruleId}:${actionTarget.value.displayValue()}"
        }
    }

    private fun ActionTemplate.targetsReceipt(
        opportunityId: String,
        receipt: RuleReceipt,
    ): Boolean = when (val actionTarget = target) {
        is ActionTarget.ProfileFactTarget -> receipt.actionTemplateId == id && receipt.factKey == actionTarget.factKey

        is ActionTarget.OpportunityRuleTarget -> {
            receipt.actionTemplateId == id &&
                actionTarget.opportunityId == opportunityId &&
                actionTarget.ruleId == receipt.ruleId &&
                receipt.factKey == actionTarget.factKey
        }
    }

    private fun ActionTemplate.canAddress(result: RuleResult): Boolean = when (target) {
        is ActionTarget.ProfileFactTarget -> result == RuleResult.FAIL

        is ActionTarget.OpportunityRuleTarget -> {
            result == RuleResult.FAIL || result == RuleResult.UNKNOWN
        }
    }

    private fun ActionTemplate.scenarioOverrides(
        opportunityById: Map<String, Opportunity>,
    ): ScenarioOverrides = when (val actionTarget = target) {
        is ActionTarget.ProfileFactTarget -> ScenarioOverrides(
            profileFacts = mapOf(actionTarget.factKey to actionTarget.value),
        )

        is ActionTarget.OpportunityRuleTarget -> {
            val currentRule = opportunityById[actionTarget.opportunityId]
                ?.eligibilityRule
                ?.findAtomicRule(actionTarget.ruleId)
            if (
                currentRule == null ||
                currentRule.factKey != actionTarget.factKey ||
                currentRule.fingerprint() != actionTarget.ruleFingerprint
            ) {
                ScenarioOverrides()
            } else {
                ScenarioOverrides(
                    ruleAssertions = mapOf(
                        RuleRef(
                            opportunityId = actionTarget.opportunityId,
                            ruleId = actionTarget.ruleId,
                        ) to actionTarget.value,
                    ),
                )
            }
        }
    }

    private fun ActionTemplate.isEthicallyActionable(): Boolean = targetActionability == Actionability.CONTROLLABLE ||
        targetActionability == Actionability.TIME_DEPENDENT

    private data class RelevantCandidate(
        val receipt: RuleReceipt,
        val template: ActionTemplate,
    )
}
