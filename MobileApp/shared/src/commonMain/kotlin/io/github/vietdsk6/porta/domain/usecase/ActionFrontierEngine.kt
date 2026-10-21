package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FrontierAction
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import kotlinx.datetime.LocalDate

class ActionFrontierEngine(
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val scenarioEvaluator: ScenarioEvaluator,
) {
    fun build(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        savedOpportunityIds: Set<String>,
        constraints: PlanningConstraints,
        today: LocalDate,
    ): List<FrontierAction> {
        val opportunityById = opportunities.associateBy(Opportunity::id)
        val baseline = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(profile, opportunity, today)
        }
        val candidates = opportunities.flatMap { opportunity ->
            val templates = opportunity.actionTemplates.associateBy(ActionTemplate::id)
            baseline.getValue(opportunity.id).rules.mapNotNull { receipt ->
                receipt.actionTemplateId
                    ?.takeIf { receipt.result == RuleResult.FAIL }
                    ?.let(templates::get)
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
            .filter { it.minimumDurationDays <= constraints.horizonDays }
            .filter { it.effort.rank <= constraints.maxEffort.rank }
            .filter { it.costBand.rank <= constraints.maxCost.rank }

        val impacted = candidates.mapNotNull { template ->
            val scenario = scenarioEvaluator.evaluate(
                profile = profile,
                overrides = mapOf(template.targetFactKey to template.targetValue),
                opportunities = opportunities,
                today = today,
            )
            val improved = scenario.resolvedHardGapOpportunityIds(template.targetFactKey)
                .filter { opportunityId ->
                    opportunityById[opportunityId]
                        ?.let { isFeasibleForPlanning(it, today, template.minimumDurationDays) }
                        ?: false
                }
            if (improved.isEmpty()) {
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

        val frontier = impacted.filter { candidate ->
            impacted.none { other -> other !== candidate && dominates(other, candidate) }
        }

        return frontier.sortedWith(
            compareByDescending<FrontierAction> {
                if (constraints.prioritizeSaved) it.improvedSavedOpportunityIds.size else 0
            }
                .thenByDescending { it.improvedOpportunityIds.size }
                .thenByDescending { it.newlyOpenOpportunityIds.size }
                .thenBy { it.earliestAffectedDeadline?.toEpochDays() ?: Int.MAX_VALUE }
                .thenBy { it.template.minimumDurationDays }
                .thenBy { it.template.effort.rank }
                .thenBy { it.template.costBand.rank }
                .thenBy { it.template.id },
        )
    }

    private fun dominates(a: FrontierAction, b: FrontierAction): Boolean {
        val noWorse =
            a.improvedSavedOpportunityIds.size >= b.improvedSavedOpportunityIds.size &&
                a.improvedOpportunityIds.size >= b.improvedOpportunityIds.size &&
                a.template.minimumDurationDays <= b.template.minimumDurationDays &&
                a.template.effort.rank <= b.template.effort.rank &&
                a.template.costBand.rank <= b.template.costBand.rank
        val strictlyBetter =
            a.improvedSavedOpportunityIds.size > b.improvedSavedOpportunityIds.size ||
                a.improvedOpportunityIds.size > b.improvedOpportunityIds.size ||
                a.template.minimumDurationDays < b.template.minimumDurationDays ||
                a.template.effort.rank < b.template.effort.rank ||
                a.template.costBand.rank < b.template.costBand.rank
        return noWorse && strictlyBetter
    }

    /**
     * Known deadlines must leave enough time. TBA and missing deadlines remain eligible for
     * comparison, but are carried in [FrontierAction.timingUnverifiedOpportunityIds] so the UI
     * never claims that the milestone fits a date the provider has not published.
     */
    private fun isFeasibleForPlanning(
        opportunity: Opportunity,
        today: LocalDate,
        minimumDurationDays: Int,
    ): Boolean = when (val deadline = opportunity.deadline) {
        is Deadline.Known -> deadline.date.toEpochDays() - today.toEpochDays() >= minimumDurationDays

        Deadline.Missing,
        Deadline.Tba,
        -> true
    }

    private fun explanationFor(
        template: ActionTemplate,
        improvedCount: Int,
        newlyOpenCount: Int,
        timingUnverifiedCount: Int,
    ): String {
        val impact = buildString {
            append("Resolves an encoded hard-rule gap in $improvedCount ")
            append(if (improvedCount == 1) "opportunity" else "opportunities")
            append(". ")
            if (newlyOpenCount > 0) {
                append("$newlyOpenCount become fully open under the encoded rules; ")
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

    private fun ActionTemplate.signature(): String = "${targetFactKey.name}:${targetValue.displayValue()}"

    private fun ActionTemplate.isEthicallyActionable(): Boolean = targetActionability == Actionability.CONTROLLABLE ||
        targetActionability == Actionability.TIME_DEPENDENT
}
