package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpportunityPackParserTest {
    private val codec = OpportunityPackCodec()
    private val parser = OpportunityPackParser(codec, OpportunityPackValidator())

    @Test
    fun `valid pack round trips and maps into executable domain rules`() {
        val rawJson = codec.encode(validPack())
        val result = parser.parse(rawJson, importedAtEpochMillis = 42L)

        assertTrue(result.isValid)
        val document = assertNotNull(result.document)
        assertEquals("porta-test-pack", document.metadata.id)
        assertEquals(42L, document.metadata.importedAtEpochMillis)
        assertEquals(rawJson, document.metadata.rawJson)
        assertEquals(1, document.opportunities.size)

        val receipt = EligibilityEvaluator().evaluate(
            profile = ProfileSnapshot(
                facts = mapOf(
                    io.github.vietdsk6.porta.domain.model.FactKey.IELTS_SCORE to ProfileFact(
                        id = "ielts",
                        key = io.github.vietdsk6.porta.domain.model.FactKey.IELTS_SCORE,
                        value = io.github.vietdsk6.porta.domain.model.FactValue.NumberValue(7.0, "band"),
                        actionability = Actionability.CONTROLLABLE,
                    ),
                ),
            ),
            opportunity = document.opportunities.single(),
            today = LocalDate(2026, 10, 20),
        )
        assertEquals(EligibilityState.OPEN, receipt.state)
    }

    @Test
    fun `future schema fails safely before mapping`() {
        val result = parser.parse(codec.encode(validPack().copy(schemaVersion = 99)))

        assertFalse(result.isValid)
        assertEquals("invalid_json_or_schema", result.diagnostics.single().code)
        assertTrue(result.diagnostics.single().message.contains("newer than supported"))
    }

    @Test
    fun `unsafe action target and dangling reference are both diagnosed`() {
        val opportunity = validPack().opportunities.single()
        val unsafeAction = opportunity.actionTemplates.single().copy(
            targetActionability = "IMMUTABLE",
        )
        val danglingRule = (opportunity.eligibilityRule as RuleNodeDto.Atomic).copy(
            actionTemplateId = "missing-action",
        )
        val pack = validPack().copy(
            opportunities = listOf(
                opportunity.copy(
                    eligibilityRule = danglingRule,
                    actionTemplates = listOf(unsafeAction),
                ),
            ),
        )

        val result = parser.parse(codec.encode(pack))
        val codes = result.diagnostics.map { it.code }.toSet()

        assertFalse(result.isValid)
        assertTrue("unsafe_action_target" in codes)
        assertTrue("unknown_action_reference" in codes)
        assertTrue(result.document == null)
    }

    @Test
    fun `duplicate opportunity IDs invalidate the whole pack`() {
        val pack = validPack()
        val result = parser.parse(
            codec.encode(pack.copy(opportunities = pack.opportunities + pack.opportunities.single())),
        )

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "duplicate_opportunity_id" })
        assertTrue(result.document == null)
    }

    private fun validPack(): OpportunityPackDto = OpportunityPackDto(
        schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
        id = "porta-test-pack",
        name = "Porta synthetic test pack",
        publisher = "Porta tests",
        version = 1,
        sourceUrl = "https://example.invalid/pack",
        lastCheckedOn = "2026-10-20",
        opportunities = listOf(
            OpportunityDto(
                id = "synthetic-scholarship",
                title = "Synthetic scholarship",
                provider = "Test provider",
                category = "SCHOLARSHIP",
                cycle = "test-only",
                cycleStartOn = "2026-10-01",
                sourceUrl = "https://example.invalid/opportunity",
                sourceSection = "Eligibility",
                lastCheckedOn = "2026-10-20",
                deadline = DeadlineDto.Known("2027-06-01"),
                eligibilityRule = RuleNodeDto.Atomic(
                    id = "ielts-rule",
                    factKey = "IELTS_SCORE",
                    operator = "GREATER_THAN_OR_EQUAL",
                    expected = FactValueDto.NumberValue(7.0, "band"),
                    explanation = "A synthetic IELTS threshold used only by tests.",
                    sourceNote = "Synthetic fixture; not a real provider requirement.",
                    actionability = "CONTROLLABLE",
                    actionTemplateId = "raise-ielts",
                ),
                actionTemplates = listOf(
                    ActionTemplateDto(
                        id = "raise-ielts",
                        title = "Raise IELTS to 7.0",
                        targetFactKey = "IELTS_SCORE",
                        targetValue = FactValueDto.NumberValue(7.0, "band"),
                        targetActionability = "CONTROLLABLE",
                        minimumDurationDays = 45,
                        effort = "MEDIUM",
                        costBand = "LOW",
                        sourceNote = "Synthetic fixture; not advice.",
                    ),
                ),
            ),
        ),
    )
}
