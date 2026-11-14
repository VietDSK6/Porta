package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import io.github.vietdsk6.porta.domain.model.RouteExplanation
import io.github.vietdsk6.porta.domain.model.RouteMetrics
import io.github.vietdsk6.porta.domain.model.RouteStep
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import kotlinx.datetime.LocalDate

/**
 * Searches short, sequential milestone routes against the encoded rules and their actual deadlines.
 * Results describe only what the current sourced checks would say, never selection by a provider.
 */
class RoutePlanner(
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val scenarioEvaluator: ScenarioEvaluator,
) {
    fun plan(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        savedOpportunityIds: Set<String>,
        constraints: RouteConstraints,
        today: LocalDate,
        focusedOpportunityId: String? = null,
    ): List<PlannedRoute> {
        if (constraints.horizonDays < 1 || constraints.maxSteps < 1 || opportunities.isEmpty()) return emptyList()
        val opportunityById = opportunities.associateBy(Opportunity::id)
        if (focusedOpportunityId != null && focusedOpportunityId !in opportunityById) return emptyList()
        val baseline = evaluate(profile, opportunities, today)
        val baselineOpen = baseline.openIds()
        val candidates = CandidateCatalog().select(opportunities, baseline)
            .filter { template ->
                template.minimumDurationDays >= 0 &&
                    template.minimumDurationDays <= constraints.horizonDays &&
                    template.effort.rank <= constraints.maxEffort.rank &&
                    template.costBand.rank <= constraints.maxCostPerStep.rank &&
                    template.scenarioOverrides(opportunityById).hasChanges()
            }
        if (candidates.isEmpty()) return emptyList()

        val start = SearchNode(
            overrides = ScenarioOverrides(),
            cursorDate = today,
            receipts = baseline,
            steps = emptyList(),
            usedTemplateIds = emptySet(),
            costRankSum = 0,
            peakEffort = EffortLevel.VERY_LOW,
            firstDoorDay = null,
            openedIds = emptySet(),
            improvedIds = emptySet(),
            lostIds = emptySet(),
        )
        val searchOrder = compareByDescending<SearchNode> {
            if (focusedOpportunityId != null && focusedOpportunityId in it.openedIds) 1 else 0
        }
            .thenByDescending {
                if (focusedOpportunityId != null && focusedOpportunityId in it.improvedIds) 1 else 0
            }
            .thenByDescending {
                if (constraints.prioritizeSaved) it.openedIds.count(savedOpportunityIds::contains) else 0
            }
            .thenByDescending { it.openedIds.size }
            .thenByDescending { it.improvedIds.size }
            .thenBy { it.firstDoorDay ?: Int.MAX_VALUE }
            .thenBy { it.elapsedDays(today) }
            .thenBy { it.costRankSum }
            .thenBy { it.peakEffort.rank }
            .thenBy { it.routeId() }

        var beam = listOf(start)
        val completed = mutableListOf<SearchNode>()
        for (depth in 1..minOf(constraints.maxSteps, MAX_STEPS)) {
            val expanded = beam.flatMap { node ->
                candidates.mapNotNull { template ->
                    expand(
                        node = node,
                        template = template,
                        profile = profile,
                        opportunities = opportunities,
                        opportunityById = opportunityById,
                        baselineOpen = baselineOpen,
                        constraints = constraints,
                        today = today,
                    )
                }
            }
            if (expanded.isEmpty()) break
            val unique = expanded.groupBy(SearchNode::dedupeKey)
                .values
                .map { alternatives -> alternatives.minWith(searchOrder) }
            beam = unique.sortedWith(searchOrder).take(BEAM_WIDTH)
            completed += beam
        }

        val meaningful = completed.filter { node ->
            if (focusedOpportunityId == null) {
                node.openedIds.isNotEmpty() || node.improvedIds.isNotEmpty()
            } else {
                focusedOpportunityId in node.openedIds || focusedOpportunityId in node.improvedIds
            }
        }
        val frontier = meaningful.filter { candidate ->
            meaningful.none { other ->
                other !== candidate && dominates(other, candidate, today)
            }
        }
        return frontier.sortedWith(searchOrder)
            .distinctBy { node ->
                RouteOutcome(
                    openedIds = node.openedIds,
                    improvedIds = node.improvedIds,
                    lostIds = node.lostIds,
                    firstDoorDay = node.firstDoorDay,
                    totalDays = node.elapsedDays(today),
                    costRankSum = node.costRankSum,
                    peakEffort = node.peakEffort,
                )
            }
            .take(MAX_ROUTES)
            .map { node ->
                val openedSaved = node.openedIds.count(savedOpportunityIds::contains)
                PlannedRoute(
                    id = node.routeId(),
                    steps = node.steps,
                    metrics = RouteMetrics(
                        openedTotal = node.openedIds.size,
                        openedSaved = openedSaved,
                        daysToFirstDoor = node.firstDoorDay,
                        totalDays = node.elapsedDays(today),
                        costRankSum = node.costRankSum,
                        peakEffort = node.peakEffort,
                    ),
                    explanation = when {
                        openedSaved > 0 -> RouteExplanation.OpensSavedDoors(openedSaved)
                        node.openedIds.isNotEmpty() -> RouteExplanation.OpensDoors(node.openedIds.size)
                        else -> RouteExplanation.ImprovesDoors(node.improvedIds.size)
                    },
                )
            }
    }

    private fun expand(
        node: SearchNode,
        template: ActionTemplate,
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        opportunityById: Map<String, Opportunity>,
        baselineOpen: Set<String>,
        constraints: RouteConstraints,
        today: LocalDate,
    ): SearchNode? {
        if (template.id in node.usedTemplateIds) return null
        val cost = node.costRankSum + template.costBand.rank
        val endDate = LocalDate.fromEpochDays(node.cursorDate.toEpochDays() + template.minimumDurationDays)
        if (endDate.toEpochDays() - today.toEpochDays() > constraints.horizonDays) return null
        val nextOverrides = node.overrides.mergedWith(template.scenarioOverrides(opportunityById))
        if (nextOverrides == node.overrides) return null

        val after = scenarioEvaluator.evaluate(profile, nextOverrides, opportunities, endDate).simulated
        val beforeAtEnd = scenarioEvaluator.evaluate(profile, node.overrides, opportunities, endDate).simulated
        val opened = after.keys.filter { opportunityId ->
            node.receipts.getValue(opportunityId).state != EligibilityState.OPEN &&
                after.getValue(opportunityId).state == EligibilityState.OPEN &&
                opportunityId !in baselineOpen
        }.sorted()
        val improved = after.keys.filter { opportunityId ->
            val before = beforeAtEnd.getValue(opportunityId)
            val current = after.getValue(opportunityId)
            current.state != EligibilityState.CLOSED &&
                current.state != EligibilityState.NOT_ACTIONABLE &&
                opportunityId !in opened &&
                current.rules.hasPassingChangeFrom(before.rules)
        }.sorted()
        val lost = after.keys.filter { opportunityId ->
            node.receipts.getValue(opportunityId).state !in UNREACHABLE_STATES &&
                after.getValue(opportunityId).state in UNREACHABLE_STATES
        }.sorted()
        if (opened.isEmpty() && improved.isEmpty()) return null

        // An opportunity opened at an earlier station remains a real, timely application window.
        // A later expiry is recorded separately in doorsLost instead of erasing that milestone.
        val allOpened = node.openedIds + opened
        val allLost = after.keys.filterTo(mutableSetOf()) { opportunityId ->
            node.receipts.getValue(opportunityId).state !in UNREACHABLE_STATES &&
                after.getValue(opportunityId).state in UNREACHABLE_STATES
        } + node.lostIds
        val allImproved = (node.improvedIds + improved) - allOpened - allLost
        val firstDoorDay = node.firstDoorDay ?: opened.firstOrNull()?.let {
            (endDate.toEpochDays() - today.toEpochDays()).toInt()
        }
        val step = RouteStep(
            template = template,
            startDate = node.cursorDate,
            endDate = endDate,
            doorsOpened = opened,
            doorsImproved = improved,
            doorsLost = lost,
        )
        return SearchNode(
            overrides = nextOverrides,
            cursorDate = endDate,
            receipts = after,
            steps = node.steps + step,
            usedTemplateIds = node.usedTemplateIds + template.id,
            costRankSum = cost,
            peakEffort = if (template.effort.rank > node.peakEffort.rank) template.effort else node.peakEffort,
            firstDoorDay = firstDoorDay,
            openedIds = allOpened,
            improvedIds = allImproved,
            lostIds = allLost,
        )
    }

    private fun evaluate(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        today: LocalDate,
    ): Map<String, EligibilityReceipt> = opportunities.associate { opportunity ->
        opportunity.id to eligibilityEvaluator.evaluate(profile, opportunity, today)
    }

    private fun dominates(
        a: SearchNode,
        b: SearchNode,
        today: LocalDate,
    ): Boolean {
        // Set inclusion preserves which saved doors open, not just how many.
        val noWorse = a.openedIds.containsAll(b.openedIds) &&
            a.improvedIds.containsAll(b.improvedIds) &&
            b.lostIds.containsAll(a.lostIds) &&
            (a.firstDoorDay ?: Int.MAX_VALUE) <= (b.firstDoorDay ?: Int.MAX_VALUE) &&
            a.elapsedDays(today) <= b.elapsedDays(today) &&
            a.costRankSum <= b.costRankSum &&
            a.peakEffort.rank <= b.peakEffort.rank
        val strictlyBetter = a.openedIds != b.openedIds ||
            a.improvedIds != b.improvedIds ||
            a.lostIds != b.lostIds ||
            a.firstDoorDay != b.firstDoorDay ||
            a.elapsedDays(today) != b.elapsedDays(today) ||
            a.costRankSum != b.costRankSum ||
            a.peakEffort != b.peakEffort
        return noWorse && strictlyBetter
    }

    private data class SearchNode(
        val overrides: ScenarioOverrides,
        val cursorDate: LocalDate,
        val receipts: Map<String, EligibilityReceipt>,
        val steps: List<RouteStep>,
        val usedTemplateIds: Set<String>,
        val costRankSum: Int,
        val peakEffort: EffortLevel,
        val firstDoorDay: Int?,
        val openedIds: Set<String>,
        val improvedIds: Set<String>,
        val lostIds: Set<String>,
    ) {
        fun elapsedDays(today: LocalDate): Int = (cursorDate.toEpochDays() - today.toEpochDays()).toInt()

        fun routeId(): String = steps.joinToString(separator = "/") { step -> step.template.id }

        fun dedupeKey(): SearchKey = SearchKey(
            date = cursorDate,
            usedTemplateIds = usedTemplateIds,
            openedIds = openedIds,
            improvedIds = improvedIds,
            lostIds = lostIds,
        )
    }

    private data class SearchKey(
        val date: LocalDate,
        val usedTemplateIds: Set<String>,
        val openedIds: Set<String>,
        val improvedIds: Set<String>,
        val lostIds: Set<String>,
    )

    private data class RouteOutcome(
        val openedIds: Set<String>,
        val improvedIds: Set<String>,
        val lostIds: Set<String>,
        val firstDoorDay: Int?,
        val totalDays: Int,
        val costRankSum: Int,
        val peakEffort: EffortLevel,
    )

    private companion object {
        const val BEAM_WIDTH = 24
        const val MAX_ROUTES = 3
        const val MAX_STEPS = 4
        val UNREACHABLE_STATES = setOf(EligibilityState.CLOSED, EligibilityState.NOT_ACTIONABLE)
    }
}

private fun ScenarioOverrides.hasChanges(): Boolean = profileFacts.isNotEmpty() || ruleAssertions.isNotEmpty()

private fun ScenarioOverrides.mergedWith(other: ScenarioOverrides): ScenarioOverrides = ScenarioOverrides(
    profileFacts = profileFacts + other.profileFacts,
    ruleAssertions = ruleAssertions + other.ruleAssertions,
)

private fun Map<String, EligibilityReceipt>.openIds(): Set<String> = values
    .filter { receipt -> receipt.state == EligibilityState.OPEN }
    .mapTo(mutableSetOf(), EligibilityReceipt::opportunityId)

private fun List<RuleReceipt>.hasPassingChangeFrom(before: List<RuleReceipt>): Boolean {
    val beforeById = before.associateBy(RuleReceipt::ruleId)
    return any { receipt ->
        receipt.result == RuleResult.PASS && beforeById[receipt.ruleId]?.result != RuleResult.PASS
    }
}
