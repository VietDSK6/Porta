package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutePlannerBundledPackTest {
    private val today = LocalDate(2026, 11, 14)
    private val planner = RoutePlanner(EligibilityEvaluator(), ScenarioEvaluator(EligibilityEvaluator()))

    @Test
    fun `Linh route shows distinct sourced progress without claiming a full pass`() {
        val dto = OpportunityPackCodec().decode(BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson()).getOrThrow()
        val result = OpportunityPackValidator().validate(dto, OpportunityPackCodec().encode(dto))
        val opportunities = result.document?.opportunities ?: error("Bundled pack must validate")
        assertEquals(15, opportunities.size)
        val routes = planner.plan(
            profile = PortaSampleData.judgeDemoProfile,
            opportunities = opportunities,
            savedOpportunityIds = PortaSampleData.judgeDemoSavedOpportunityIds,
            constraints = RouteConstraints(240, CostBand.UNKNOWN, EffortLevel.VERY_HIGH),
            today = today,
        )

        assertTrue(routes.size >= 2)
        assertEquals(routes.size, routes.map { it.id }.toSet().size)
        assertTrue(routes.all { route -> route.metrics.openedTotal == 0 })
        assertTrue(routes.all { route -> route.steps.any { step -> step.doorsImproved.isNotEmpty() } })
        assertTrue(routes.all { route -> route.steps.all { step -> step.template.target is ActionTarget.ProfileFactTarget } })
        assertTrue(routes.any { route -> route.steps.any { step -> step.template.target.factKey == FactKey.DEGREE_COMPLETED } })
        assertTrue(routes.any { route -> route.steps.any { step -> step.template.target.factKey == FactKey.IELTS_SCORE } })
        assertTrue(routes.first().steps.size >= 2)
        assertTrue(routes.first().steps.map { step -> step.template.target.factKey }.containsAll(listOf(FactKey.DEGREE_COMPLETED, FactKey.IELTS_SCORE)))
        assertTrue(routes.first().steps.any { step -> "kotlin-student-coding-competition-2026" in step.doorsLost })
    }

    @Test
    fun `a programme focused route can still include its scoped confirmation`() {
        val dto = OpportunityPackCodec().decode(BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson()).getOrThrow()
        val opportunities = OpportunityPackValidator().validate(dto, OpportunityPackCodec().encode(dto))
            .document?.opportunities ?: error("Bundled pack must validate")
        val routes = planner.plan(
            profile = PortaSampleData.judgeDemoProfile,
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = RouteConstraints(180, CostBand.UNKNOWN, EffortLevel.VERY_HIGH),
            today = today,
            focusedOpportunityId = "cern-openlab-summer-student-2027",
        )

        assertTrue(routes.isNotEmpty())
        assertTrue(
            routes.all { route ->
                route.steps.any { step -> "cern-openlab-summer-student-2027" in step.doorsImproved }
            },
        )
        assertTrue(
            routes.any { route ->
                route.steps.any { step -> step.template.target is ActionTarget.OpportunityRuleTarget }
            },
        )
    }
}
