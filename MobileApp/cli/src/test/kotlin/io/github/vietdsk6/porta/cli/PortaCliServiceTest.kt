package io.github.vietdsk6.porta.cli

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PortaCliServiceTest {
    private val service = PortaCliService()

    @Test
    fun bundledPackValidatesAndEvaluatesThroughAppEngine() {
        val result = service.validatePack(service.bundledPackJson())
        assertTrue(result.isValid, result.diagnostics.toString())
        val pack = requireNotNull(result.document)
        assertEquals(15, pack.opportunities.size)
        val receipts = service.evaluate(PortaSampleData.judgeDemoProfile, pack, LocalDate(2026, 10, 20))
        assertEquals(pack.opportunities.size, receipts.size)
        assertTrue(receipts.all { (_, receipt) -> receipt.opportunityId in pack.opportunities.map { it.id } })
    }

    @Test
    fun invalidPackReturnsDiagnosticsAndCannotBeLoaded() {
        val result = service.validatePack("{}")
        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "invalid_json_or_schema" })
        assertTrue(runCatching { service.requirePack("{}") }.exceptionOrNull() is InvalidCliInput)
    }

    @Test
    fun profileExportRoundTripsAndRouteUsesBoundedSearch() {
        val profileJson = ProfileExportCodec().encode(PortaSampleData.judgeDemoProfile, 0L)
        val profile = service.decodeProfile(profileJson)
        assertEquals(
            PortaSampleData.judgeDemoProfile.facts.mapValues { (_, fact) -> fact.value },
            profile.facts.mapValues { (_, fact) -> fact.value },
        )
        val pack = service.requirePack(service.bundledPackJson())
        val routes = service.route(
            profile = profile,
            pack = pack,
            date = LocalDate(2026, 10, 20),
            horizonDays = 180,
            maxCost = CostBand.MEDIUM,
            maxEffort = EffortLevel.HIGH,
        )
        assertTrue(routes.size <= 3)
        assertTrue(routes.all { route -> route.steps.isNotEmpty() && route.metrics.totalDays <= 180 })
    }
}
