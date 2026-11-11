package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.RouteConstraints
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutePlannerBundledPackTest {
    private val today = LocalDate(2026, 11, 11)
    private val planner = RoutePlanner(EligibilityEvaluator(), ScenarioEvaluator(EligibilityEvaluator()))

    @Test
    fun `Linh route stays stable on the bundled 15-door pack`() {
        val dto = OpportunityPackCodec().decode(BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson()).getOrThrow()
        val result = OpportunityPackValidator().validate(dto, OpportunityPackCodec().encode(dto))
        val opportunities = result.document?.opportunities ?: error("Bundled pack must validate")
        assertEquals(15, opportunities.size)
        val routes = planner.plan(
            profile = PortaSampleData.judgeDemoProfile,
            opportunities = opportunities,
            savedOpportunityIds = PortaSampleData.judgeDemoSavedOpportunityIds,
            constraints = RouteConstraints(180, CostBand.UNKNOWN, EffortLevel.VERY_HIGH),
            today = today,
        )

        assertEquals(
            listOf(
                "cern-openlab-full-period-availability/esa-full-period-availability/" +
                    "oecd-full-period-availability/world-bank-treasury-full-period-availability",
                "cern-openlab-full-period-availability/esa-full-period-availability/" +
                    "oecd-full-period-availability/schwarzman-ielts-7-band",
                "cern-openlab-full-period-availability/esa-full-period-availability/" +
                    "schwarzman-ielts-7-band/world-bank-treasury-full-period-availability",
            ),
            routes.map(PlannedRoute::id),
        )
        assertTrue(routes.all { route -> route.metrics.openedTotal == 0 })
        assertTrue(routes.all { route -> route.steps.any { step -> step.doorsImproved.isNotEmpty() } })
        assertTrue(routes.first().steps.any { step -> "kotlin-student-coding-competition-2026" in step.doorsLost })
    }
}
