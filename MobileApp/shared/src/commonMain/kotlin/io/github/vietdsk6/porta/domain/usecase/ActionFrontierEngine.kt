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
            val newlyOpen = scenario.deltas
                .filter { delta ->
                    delta.before != EligibilityState.OPEN && delta.after == EligibilityState.OPEN
                }
                .map { it.opportunityId }
                .filter { opportunityId ->
                    opportunityById[opportunityId]
                        ?.let { isFeasibleBeforeDeadline(it, today, template.minimumDurationDays) }
                        ?: false
                }
                .sorted()

            if (newlyOpen.isEmpty()) {
                return@mapNotNull null
            }

            val newlyOpenSaved = newlyOpen.filter(savedOpportunityIds::contains)
            val earliestDeadline = newlyOpen
                .mapNotNull { id ->
                    (opportunityById.getValue(id).deadline as? Deadline.Known)?.date
                }
                .minOrNull()

            FrontierAction(
                template = template,
                newlyOpenOpportunityIds = newlyOpen,
                newlyOpenSavedOpportunityIds = newlyOpenSaved,
                earliestAffectedDeadline = earliestDeadline,
                scenario = scenario,
                explanation = explanationFor(template, newlyOpen.size, newlyOpenSaved.size),
            )
        }

        val frontier = impacted.filter { candidate ->
            impacted.none { other -> other !== candidate && dominates(other, candidate) }
        }

        return frontier.sortedWith(
            compareByDescending<FrontierAction> {
                if (constraints.prioritizeSaved) it.newlyOpenSavedOpportunityIds.size else 0
            }
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
            a.newlyOpenSavedOpportunityIds.size >= b.newlyOpenSavedOpportunityIds.size &&
                a.newlyOpenOpportunityIds.size >= b.newlyOpenOpportunityIds.size &&
                a.template.minimumDurationDays <= b.template.minimumDurationDays &&
                a.template.effort.rank <= b.template.effort.rank &&
                a.template.costBand.rank <= b.template.costBand.rank
        val strictlyBetter =
            a.newlyOpenSavedOpportunityIds.size > b.newlyOpenSavedOpportunityIds.size ||
                a.newlyOpenOpportunityIds.size > b.newlyOpenOpportunityIds.size ||
                a.template.minimumDurationDays < b.template.minimumDurationDays ||
                a.template.effort.rank < b.template.effort.rank ||
                a.template.costBand.rank < b.template.costBand.rank
        return noWorse && strictlyBetter
    }

    private fun isFeasibleBeforeDeadline(
        opportunity: Opportunity,
        today: LocalDate,
        minimumDurationDays: Int,
    ): Boolean = when (val deadline = opportunity.deadline) {
        is Deadline.Known -> deadline.date.toEpochDays() - today.toEpochDays() >= minimumDurationDays

        Deadline.Missing,
        Deadline.Tba,
        -> false
    }

    private fun explanationFor(
        template: ActionTemplate,
        newlyOpenCount: Int,
        newlyOpenSavedCount: Int,
    ): String {
        val otherCount = newlyOpenCount - newlyOpenSavedCount
        val impact = buildList {
            if (newlyOpenSavedCount > 0) add("$newlyOpenSavedCount saved")
            if (otherCount > 0) add("$otherCount other")
        }.joinToString(" and ")
        return "Opens $impact opportunities before their deadlines. " +
            "Target: ${template.targetValue.displayValue()}. " +
            "Estimated duration: ${template.minimumDurationDays} days. " +
            "Effort: ${template.effort.name.lowercase()}. Cost: ${template.costBand.name.lowercase()}."
    }

    private fun ActionTemplate.signature(): String = "${targetFactKey.name}:${targetValue.displayValue()}"

    private fun ActionTemplate.isEthicallyActionable(): Boolean = targetActionability == Actionability.CONTROLLABLE ||
        targetActionability == Actionability.TIME_DEPENDENT
}
