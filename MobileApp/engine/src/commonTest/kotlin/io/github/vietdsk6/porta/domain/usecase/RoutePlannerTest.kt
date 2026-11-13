package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import io.github.vietdsk6.porta.domain.model.RouteStep
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutePlannerTest {
    private val today = LocalDate(2026, 11, 11)
    private val planner = RoutePlanner(EligibilityEvaluator(), ScenarioEvaluator(EligibilityEvaluator()))
    private val profile = ProfileSnapshot(
        facts = mapOf(
            FactKey.IELTS_SCORE to profileFact(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band")),
            FactKey.COMMUNITY_PROJECT to profileFact(FactKey.COMMUNITY_PROJECT, FactValue.BooleanValue(false)),
        ),
    )
    private val ielts = action(
        id = "a-ielts",
        key = FactKey.IELTS_SCORE,
        value = FactValue.NumberValue(7.0, "band"),
        days = 10,
    )
    private val community = action(
        id = "b-community",
        key = FactKey.COMMUNITY_PROJECT,
        value = FactValue.BooleanValue(true),
        days = 10,
    )
    private val erasmus = opportunity(
        id = "erasmus",
        deadlineDay = 12,
        rule = rule("english", FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band"), ielts.id),
        actions = listOf(ielts),
    )
    private val contest = opportunity(
        id = "contest",
        deadlineDay = 30,
        rule = RuleNode.All(
            id = "contest-all",
            children = listOf(
                rule("contest-english", FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band"), ielts.id),
                rule("contest-project", FactKey.COMMUNITY_PROJECT, FactValue.BooleanValue(true), community.id),
            ),
        ),
        actions = listOf(ielts, community),
    )

    @Test
    fun `deadline rewards the order that opens an early saved door`() {
        val routes = planner.plan(
            profile = profile,
            opportunities = listOf(contest, erasmus),
            savedOpportunityIds = setOf("erasmus"),
            constraints = RouteConstraints(30, CostBand.FREE, EffortLevel.HIGH),
            today = today,
        )

        assertTrue(routes.isNotEmpty())
        assertEquals(listOf("a-ielts", "b-community"), routes.first().steps.map { it.template.id })
        assertEquals(listOf("erasmus"), routes.first().steps.first().doorsOpened)
        assertEquals(listOf("contest"), routes.first().steps.last().doorsOpened)
        assertEquals(listOf("erasmus"), routes.first().steps.last().doorsLost)
        assertEquals(2, routes.first().metrics.openedTotal)
        assertEquals(1, routes.first().metrics.openedSaved)
        assertEquals(10, routes.first().metrics.daysToFirstDoor)
    }

    @Test
    fun `routes obey estimated cost per step horizon effort and step caps`() {
        val costlyIelts = ielts.copy(costBand = CostBand.LOW)
        val costlyCommunity = community.copy(costBand = CostBand.LOW, effort = EffortLevel.HIGH)
        val opportunities = listOf(
            erasmus.copy(actionTemplates = listOf(costlyIelts)),
            contest.copy(actionTemplates = listOf(costlyIelts, costlyCommunity)),
        )
        val routes = planner.plan(
            profile,
            opportunities,
            emptySet(),
            RouteConstraints(horizonDays = 15, maxCostPerStep = CostBand.LOW, maxEffort = EffortLevel.MEDIUM),
            today,
        )

        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { route -> route.metrics.totalDays <= 15 })
        assertTrue(routes.all { route -> route.steps.all { it.template.costBand.rank <= CostBand.LOW.rank } })
        assertTrue(routes.all { route -> route.metrics.peakEffort.rank <= EffortLevel.MEDIUM.rank })
        assertTrue(routes.all { route -> route.steps.size <= 4 })
        assertTrue(routes.all { route -> route.steps.none { it.template.id == costlyCommunity.id } })
    }

    @Test
    fun `any estimated cost allows multiple high cost steps`() {
        val highCostIelts = ielts.copy(costBand = CostBand.HIGH)
        val highCostCommunity = community.copy(costBand = CostBand.HIGH)
        val opportunities = listOf(
            erasmus.copy(actionTemplates = listOf(highCostIelts)),
            contest.copy(actionTemplates = listOf(highCostIelts, highCostCommunity)),
        )

        val anyCostRoutes = planner.plan(
            profile,
            opportunities,
            emptySet(),
            RouteConstraints(30, CostBand.UNKNOWN, EffortLevel.HIGH),
            today,
        )
        val lowCostRoutes = planner.plan(
            profile,
            opportunities,
            emptySet(),
            RouteConstraints(30, CostBand.LOW, EffortLevel.HIGH),
            today,
        )

        assertTrue(anyCostRoutes.any { route -> route.steps.size == 2 && route.metrics.costRankSum > CostBand.UNKNOWN.rank })
        assertTrue(lowCostRoutes.isEmpty())
    }

    @Test
    fun `planning is deterministic and does not mutate the real profile`() {
        val before = profile.copy(facts = profile.facts.toMap())
        val constraints = RouteConstraints(30, CostBand.FREE, EffortLevel.HIGH)
        val first = planner.plan(profile, listOf(contest, erasmus), setOf("erasmus"), constraints, today)
        val second = planner.plan(profile, listOf(erasmus, contest), setOf("erasmus"), constraints, today)

        assertEquals(first, second)
        assertEquals(before, profile)
    }

    @Test
    fun `context only and immutable templates never enter routes`() {
        val context = ielts.copy(id = "context", targetActionability = Actionability.CONTEXT_ONLY)
        val immutable = community.copy(id = "immutable", targetActionability = Actionability.IMMUTABLE)
        val blocked = opportunity(
            id = "blocked",
            deadlineDay = 30,
            rule = RuleNode.All(
                id = "blocked-all",
                children = listOf(
                    rule("blocked-english", FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band"), context.id),
                    rule("blocked-project", FactKey.COMMUNITY_PROJECT, FactValue.BooleanValue(true), immutable.id),
                ),
            ),
            actions = listOf(context, immutable),
        )

        assertTrue(
            planner.plan(
                profile,
                listOf(blocked),
                emptySet(),
                RouteConstraints(30, CostBand.UNKNOWN, EffortLevel.VERY_HIGH),
                today,
            ).isEmpty(),
        )
    }

    @Test
    fun `returned routes are never pairwise dominated across seeded packs`() {
        val random = Random(73)
        repeat(200) { iteration ->
            val opportunities = (0 until 4).map { index ->
                opportunity(
                    id = "pack-$iteration-door-$index",
                    deadlineDay = random.nextInt(8, 36),
                    rule = if (index % 2 == 0) {
                        rule("english-$index", FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band"), ielts.id)
                    } else {
                        rule("project-$index", FactKey.COMMUNITY_PROJECT, FactValue.BooleanValue(true), community.id)
                    },
                    actions = listOf(ielts, community),
                )
            }
            val routes = planner.plan(
                profile,
                opportunities,
                emptySet(),
                RouteConstraints(35, CostBand.FREE, EffortLevel.HIGH),
                today,
            )
            assertTrue(routes.size <= 3)
            routes.forEach { route ->
                assertTrue(routes.none { other -> other !== route && dominates(other, route) })
            }
        }
    }

    private fun dominates(a: PlannedRoute, b: PlannedRoute): Boolean {
        val aOpened = a.steps.flatMap(RouteStep::doorsOpened).toSet()
        val bOpened = b.steps.flatMap(RouteStep::doorsOpened).toSet()
        val aLost = a.steps.flatMap(RouteStep::doorsLost).toSet()
        val bLost = b.steps.flatMap(RouteStep::doorsLost).toSet()
        val noWorse = aOpened.containsAll(bOpened) &&
            bLost.containsAll(aLost) &&
            (a.metrics.daysToFirstDoor ?: Int.MAX_VALUE) <= (b.metrics.daysToFirstDoor ?: Int.MAX_VALUE) &&
            a.metrics.totalDays <= b.metrics.totalDays &&
            a.metrics.costRankSum <= b.metrics.costRankSum &&
            a.metrics.peakEffort.rank <= b.metrics.peakEffort.rank
        val strict = aOpened != bOpened ||
            aLost != bLost ||
            a.metrics.daysToFirstDoor != b.metrics.daysToFirstDoor ||
            a.metrics.totalDays < b.metrics.totalDays ||
            a.metrics.costRankSum < b.metrics.costRankSum ||
            a.metrics.peakEffort.rank < b.metrics.peakEffort.rank
        return noWorse && strict
    }

    private fun profileFact(key: FactKey, value: FactValue) = ProfileFact(
        id = key.name,
        key = key,
        value = value,
        actionability = Actionability.CONTROLLABLE,
    )

    private fun action(
        id: String,
        key: FactKey,
        value: FactValue,
        days: Int,
    ) = ActionTemplate(
        id = id,
        title = id,
        targetFactKey = key,
        targetValue = value,
        targetActionability = Actionability.CONTROLLABLE,
        minimumDurationDays = days,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
    )

    private fun rule(id: String, key: FactKey, expected: FactValue, actionId: String) = RuleNode.Atomic(
        id = id,
        factKey = key,
        operator = if (expected is FactValue.NumberValue) Operator.GREATER_THAN_OR_EQUAL else Operator.EQUALS,
        expected = expected,
        explanation = "Synthetic sourced check.",
        actionability = Actionability.CONTROLLABLE,
        actionTemplateId = actionId,
    )

    private fun opportunity(
        id: String,
        deadlineDay: Int,
        rule: RuleNode,
        actions: List<ActionTemplate>,
    ) = Opportunity(
        id = id,
        title = id,
        provider = "Test provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/$id",
        lastCheckedOn = today,
        deadline = Deadline.Known(LocalDate.fromEpochDays(today.toEpochDays() + deadlineDay)),
        eligibilityRule = rule,
        actionTemplates = actions,
    )
}
