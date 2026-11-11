package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionFrontierEvaluation
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import io.github.vietdsk6.porta.domain.model.ScenarioResult
import kotlinx.datetime.LocalDate
import java.util.Locale
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Reproducible JVM characterization for the PRD's 100-opportunity engine workload.
 *
 * This is deliberately not a wall-clock CI gate. Shared runners and hosted CI have noisy scheduling, so the test
 * asserts workload shape and deterministic output while printing median/p95 observations for a documented machine.
 */
class ScenarioEnginePerformanceCharacterizationTest {
    private val today = LocalDate(2026, 11, 4)
    private val eligibilityEvaluator = EligibilityEvaluator()
    private val scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator)
    private val frontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator)
    private val routePlanner = RoutePlanner(eligibilityEvaluator, scenarioEvaluator)
    private val fixture = performanceFixture()

    @Volatile
    private var observationSink = 0

    @Test
    fun `fixture covers 100 opportunities and engine output is deterministic`() {
        val opportunities = fixture.opportunities
        val nodes = opportunities.flatMap { opportunity -> opportunity.eligibilityRule.flatten() }

        assertEquals(100, opportunities.size)
        assertEquals(100, opportunities.map(Opportunity::id).toSet().size)
        assertTrue(nodes.any { node -> node is RuleNode.All })
        assertTrue(nodes.any { node -> node is RuleNode.Any })
        assertTrue(nodes.any { node -> node is RuleNode.ManualCheck })
        assertTrue(opportunities.any { opportunity -> opportunity.deadline is Deadline.Known })
        assertTrue(opportunities.any { opportunity -> opportunity.deadline is Deadline.Tba })
        assertTrue(opportunities.any { opportunity -> opportunity.deadline is Deadline.Missing })

        val firstEligibility = evaluateEligibility()
        val secondEligibility = evaluateEligibility()
        assertEquals(firstEligibility, secondEligibility)
        assertTrue(
            firstEligibility
                .flatMap(EligibilityReceipt::rules)
                .any { receipt -> receipt.result == RuleResult.UNKNOWN && receipt.actual == null },
        )
        assertTrue(
            firstEligibility
                .flatMap(EligibilityReceipt::rules)
                .any { receipt -> receipt.result == RuleResult.REVIEW },
        )

        val firstScenario = evaluateScenario()
        val secondScenario = evaluateScenario()
        assertEquals(firstScenario, secondScenario)
        assertTrue(firstScenario.impacts.isNotEmpty())
        assertTrue(firstScenario.deltas.isNotEmpty())

        val firstFrontier = evaluateFrontier()
        val secondFrontier = evaluateFrontier()
        assertEquals(firstFrontier, secondFrontier)
        assertTrue(firstFrontier.actions.isNotEmpty() || firstFrontier.exclusions.isNotEmpty())
        assertTrue(firstFrontier.actions.any { action -> action.timingUnverifiedOpportunityIds.isNotEmpty() })
        assertTrue(
            firstFrontier.exclusions.any { exclusion ->
                FrontierExclusionReason.MISSES_KNOWN_DEADLINES in exclusion.reasons
            },
        )
        assertEquals(evaluateRoutes(), evaluateRoutes())
        assertEquals(fixture.originalProfile, fixture.profile)
    }

    @Test
    fun `report median and p95 after warmup without a flaky timing assertion`() {
        repeat(WARM_UP_RUNS) {
            observe(evaluateEligibility().eligibilityChecksum())
            observe(evaluateScenario().scenarioChecksum())
            observe(evaluateFrontier().frontierChecksum())
            observe(evaluateRoutes().size)
        }

        val eligibility = measureRuns { evaluateEligibility().eligibilityChecksum() }
        val scenario = measureRuns { evaluateScenario().scenarioChecksum() }
        val frontier = measureRuns { evaluateFrontier().frontierChecksum() }
        val routes = measureRuns { evaluateRoutes().size }

        println(
            "PORTA_PERF_ENV " +
                "os=${System.getProperty("os.name")} " +
                "arch=${System.getProperty("os.arch")} " +
                "jdk=${System.getProperty("java.version")} " +
                "processors=${Runtime.getRuntime().availableProcessors()}",
        )
        println(eligibility.asReport("eligibility_100"))
        println(scenario.asReport("scenario_100"))
        println(frontier.asReport("action_frontier_100"))
        println(routes.asReport("route_planner_100"))

        assertTrue(eligibility.samplesNanos.all { nanos -> nanos > 0L })
        assertTrue(scenario.samplesNanos.all { nanos -> nanos > 0L })
        assertTrue(frontier.samplesNanos.all { nanos -> nanos > 0L })
        assertTrue(routes.samplesNanos.all { nanos -> nanos > 0L })
    }

    private fun evaluateEligibility(): List<EligibilityReceipt> = fixture.opportunities.map { opportunity ->
        eligibilityEvaluator.evaluate(fixture.profile, opportunity, today)
    }

    private fun evaluateScenario(): ScenarioResult = scenarioEvaluator.evaluate(
        profile = fixture.profile,
        overrides = fixture.scenarioOverrides,
        opportunities = fixture.opportunities,
        today = today,
    )

    private fun evaluateFrontier(): ActionFrontierEvaluation = frontierEngine.evaluate(
        profile = fixture.profile,
        opportunities = fixture.opportunities,
        savedOpportunityIds = fixture.savedOpportunityIds,
        constraints = fixture.constraints,
        today = today,
    )

    private fun evaluateRoutes() = routePlanner.plan(
        profile = fixture.profile,
        opportunities = fixture.opportunities,
        savedOpportunityIds = fixture.savedOpportunityIds,
        constraints = RouteConstraints(
            horizonDays = fixture.constraints.horizonDays,
            maxTotalCost = fixture.constraints.maxCost,
            maxEffort = fixture.constraints.maxEffort,
        ),
        today = today,
    )

    private fun performanceFixture(): PerformanceFixture {
        val profile = ProfileSnapshot(
            facts = mapOf(
                FactKey.IELTS_SCORE to profileFact(
                    FactKey.IELTS_SCORE,
                    FactValue.NumberValue(6.5, "band"),
                ),
                FactKey.GPA to profileFact(
                    FactKey.GPA,
                    FactValue.NumberValue(3.0, "4.0 scale"),
                ),
                FactKey.EXPERIENCE_MONTHS to profileFact(
                    FactKey.EXPERIENCE_MONTHS,
                    FactValue.NumberValue(6.0, "months"),
                ),
                FactKey.ENROLLED to profileFact(
                    FactKey.ENROLLED,
                    FactValue.BooleanValue(true),
                ),
            ),
        )
        val opportunities = List(OPPORTUNITY_COUNT) { index -> syntheticOpportunity(index) }
        return PerformanceFixture(
            profile = profile,
            originalProfile = profile.copy(facts = profile.facts.toMap()),
            opportunities = opportunities,
            savedOpportunityIds = opportunities
                .filterIndexed { index, _ -> index % 7 == 0 }
                .mapTo(linkedSetOf(), Opportunity::id),
            scenarioOverrides = ScenarioOverrides(
                profileFacts = mapOf(
                    FactKey.IELTS_SCORE to FactValue.NumberValue(7.5, "band"),
                    FactKey.GPA to FactValue.NumberValue(3.7, "4.0 scale"),
                    FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(18.0, "months"),
                    FactKey.COMMUNITY_PROJECT to FactValue.BooleanValue(true),
                ),
            ),
            constraints = PlanningConstraints(
                horizonDays = 120,
                maxEffort = EffortLevel.HIGH,
                maxCost = CostBand.MEDIUM,
                prioritizeSaved = true,
            ),
        )
    }

    private fun syntheticOpportunity(index: Int): Opportunity {
        val id = "performance-opportunity-${index.toString().padStart(3, '0')}"
        val templates = mutableListOf<ActionTemplate>()
        val rule = when (index % 4) {
            0 -> RuleNode.All(
                id = "$id-all",
                children = listOf(
                    actionableRule(
                        opportunityId = id,
                        suffix = "ielts",
                        factKey = FactKey.IELTS_SCORE,
                        expected = FactValue.NumberValue(7.0, "band"),
                        durationDays = 45,
                        templates = templates,
                    ),
                    RuleNode.Atomic(
                        id = "$id-enrolled",
                        factKey = FactKey.ENROLLED,
                        operator = Operator.EQUALS,
                        expected = FactValue.BooleanValue(true),
                        explanation = "Applicant remains enrolled.",
                        actionability = Actionability.CONTEXT_ONLY,
                    ),
                ),
            )

            1 -> RuleNode.Any(
                id = "$id-any",
                children = listOf(
                    actionableRule(
                        opportunityId = id,
                        suffix = "ielts",
                        factKey = FactKey.IELTS_SCORE,
                        expected = FactValue.NumberValue(7.0, "band"),
                        durationDays = 45,
                        templates = templates,
                    ),
                    actionableRule(
                        opportunityId = id,
                        suffix = "gpa",
                        factKey = FactKey.GPA,
                        expected = FactValue.NumberValue(3.5, "4.0 scale"),
                        durationDays = 60,
                        templates = templates,
                    ),
                ),
            )

            2 -> RuleNode.All(
                id = "$id-all-review",
                children = listOf(
                    actionableRule(
                        opportunityId = id,
                        suffix = "ielts",
                        factKey = FactKey.IELTS_SCORE,
                        expected = FactValue.NumberValue(7.0, "band"),
                        durationDays = 45,
                        templates = templates,
                    ),
                    RuleNode.ManualCheck(
                        id = "$id-manual",
                        explanation = "The provider manually reviews programme fit.",
                        sourceNote = "Synthetic manual check for performance characterization.",
                    ),
                ),
            )

            else -> RuleNode.All(
                id = "$id-all-missing",
                children = listOf(
                    actionableRule(
                        opportunityId = id,
                        suffix = "experience",
                        factKey = FactKey.EXPERIENCE_MONTHS,
                        expected = FactValue.NumberValue(12.0, "months"),
                        durationDays = 90,
                        templates = templates,
                    ),
                    RuleNode.Atomic(
                        id = "$id-community-project",
                        factKey = FactKey.COMMUNITY_PROJECT,
                        operator = Operator.EXISTS,
                        explanation = "Community-project information is required.",
                        actionability = Actionability.UNKNOWN,
                    ),
                ),
            )
        }
        return Opportunity(
            id = id,
            title = "Synthetic opportunity $index",
            provider = "Porta performance fixture",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "performance-only",
            sourceUrl = "https://example.invalid/$id",
            sourceSection = "Synthetic performance characterization",
            lastCheckedOn = today,
            deadline = deadlineFor(index),
            eligibilityRule = rule,
            actionTemplates = templates,
        )
    }

    private fun actionableRule(
        opportunityId: String,
        suffix: String,
        factKey: FactKey,
        expected: FactValue,
        durationDays: Int,
        templates: MutableList<ActionTemplate>,
    ): RuleNode.Atomic {
        val template = ActionTemplate(
            id = "$opportunityId-$suffix-action",
            title = "Reach the $suffix target",
            targetFactKey = factKey,
            targetValue = expected,
            targetActionability = Actionability.CONTROLLABLE,
            minimumDurationDays = durationDays,
            effort = EffortLevel.MEDIUM,
            costBand = CostBand.LOW,
        )
        templates += template
        return RuleNode.Atomic(
            id = "$opportunityId-$suffix-rule",
            factKey = factKey,
            operator = Operator.GREATER_THAN_OR_EQUAL,
            expected = expected,
            explanation = "Applicant reaches the $suffix threshold.",
            sourceNote = "Synthetic threshold for performance characterization.",
            actionability = Actionability.CONTROLLABLE,
            actionTemplateId = template.id,
        )
    }

    private fun deadlineFor(index: Int): Deadline = when (index % 4) {
        0 -> Deadline.Known(LocalDate(2027, 6, 1))
        1 -> Deadline.Tba
        2 -> Deadline.Missing
        else -> Deadline.Known(LocalDate(2026, 11, 20))
    }

    private fun profileFact(key: FactKey, value: FactValue): ProfileFact = ProfileFact(
        id = "performance-${key.name.lowercase()}",
        key = key,
        value = value,
        actionability = Actionability.CONTROLLABLE,
    )

    private fun RuleNode.flatten(): List<RuleNode> = when (this) {
        is RuleNode.All -> listOf(this) + children.flatMap { child -> child.flatten() }

        is RuleNode.Any -> listOf(this) + children.flatMap { child -> child.flatten() }

        is RuleNode.Atomic,
        is RuleNode.ManualCheck,
        -> listOf(this)
    }

    private fun measureRuns(block: () -> Int): TimingSummary {
        val samples = List(MEASURED_RUNS) {
            var checksum = 0
            val elapsed = measureNanoTime {
                checksum = block()
            }
            observe(checksum)
            elapsed
        }
        return TimingSummary(samples)
    }

    private fun observe(value: Int) {
        observationSink = observationSink xor value
    }

    private fun List<EligibilityReceipt>.eligibilityChecksum(): Int = sumOf { receipt ->
        receipt.opportunityId.length + receipt.state.ordinal + receipt.rootResult.ordinal + receipt.rules.size
    }

    private fun ScenarioResult.scenarioChecksum(): Int = baseline.size + simulated.size + deltas.size + impacts.sumOf { impact -> impact.changedRules.size }

    private fun ActionFrontierEvaluation.frontierChecksum(): Int = actions.sumOf { action -> action.improvedOpportunityIds.size + action.newlyOpenOpportunityIds.size } +
        exclusions.sumOf { exclusion -> exclusion.reasons.size }

    private data class PerformanceFixture(
        val profile: ProfileSnapshot,
        val originalProfile: ProfileSnapshot,
        val opportunities: List<Opportunity>,
        val savedOpportunityIds: Set<String>,
        val scenarioOverrides: ScenarioOverrides,
        val constraints: PlanningConstraints,
    )

    private data class TimingSummary(
        val samplesNanos: List<Long>,
    ) {
        private val sorted = samplesNanos.sorted()
        val medianNanos = if (sorted.size % 2 == 0) {
            val upper = sorted[sorted.size / 2]
            val lower = sorted[sorted.size / 2 - 1]
            lower + (upper - lower) / 2
        } else {
            sorted[sorted.size / 2]
        }
        val p95Nanos = sorted[((sorted.size * 95 + 99) / 100 - 1).coerceIn(sorted.indices)]

        fun asReport(label: String): String = "PORTA_PERF " +
            "workload=$label " +
            "opportunities=$OPPORTUNITY_COUNT " +
            "warmups=$WARM_UP_RUNS " +
            "samples=$MEASURED_RUNS " +
            "median_ms=${milliseconds(medianNanos)} " +
            "p95_ms=${milliseconds(p95Nanos)}"

        private fun milliseconds(nanos: Long): String = String.format(Locale.ROOT, "%.3f", nanos / NANOS_PER_MILLISECOND)
    }

    private companion object {
        const val OPPORTUNITY_COUNT = 100
        const val WARM_UP_RUNS = 10
        const val MEASURED_RUNS = 30
        const val NANOS_PER_MILLISECOND = 1_000_000.0
    }
}
