package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpportunityPackParserTest {
    private val codec = OpportunityPackCodec()
    private val parser = OpportunityPackParser(codec, OpportunityPackValidator())

    @Test
    fun `valid pack round trips and maps into executable domain rules`() {
        val rawJson = codec.encode(validPack())
        val result = parser.parse(rawJson, importedAtEpochMillis = 42L)

        assertTrue(result.isValid, result.diagnostics.joinToString())
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

    @Test
    fun `action IDs must be unique across the whole pack`() {
        val pack = validPack()
        val firstOpportunity = pack.opportunities.single()
        val secondOpportunity = firstOpportunity.copy(
            id = "second-scholarship",
            title = "Second synthetic scholarship",
            sourceUrl = "https://example.invalid/second-opportunity",
        )

        val result = parser.parse(
            codec.encode(pack.copy(opportunities = listOf(firstOpportunity, secondOpportunity))),
        )

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "duplicate_action_id_across_pack" })
        assertTrue(result.document == null)
    }

    @Test
    fun `v1 availability action upgrades to its single referenced rule`() {
        val opportunity = validPack().opportunities.single()
        val availabilityRule = (opportunity.eligibilityRule as RuleNodeDto.Atomic).copy(
            id = "programme-availability",
            factKey = FactKey.AVAILABLE_FULL_PERIOD.name,
            operator = "EQUALS",
            expected = FactValueDto.BooleanValue(true),
            actionTemplateId = "reserve-programme-period",
        )
        val availabilityAction = opportunity.actionTemplates.single().copy(
            id = "reserve-programme-period",
            targetFactKey = FactKey.AVAILABLE_FULL_PERIOD.name,
            targetValue = FactValueDto.BooleanValue(true),
        )
        val v1Pack = validPack().copy(
            schemaVersion = 1,
            opportunities = listOf(
                opportunity.copy(
                    schemaVersion = 1,
                    eligibilityRule = availabilityRule,
                    actionTemplates = listOf(availabilityAction),
                ),
            ),
        )

        val rawV1 = codec.encode(v1Pack)
            .lineSequence()
            .filterNot { line -> "\"targetScope\"" in line || "\"targetRuleId\"" in line }
            .joinToString("\n")
        val result = parser.parse(rawV1)

        assertTrue(result.isValid, result.diagnostics.joinToString())
        val target = assertIs<ActionTarget.OpportunityRuleTarget>(
            assertNotNull(result.document).opportunities.single().actionTemplates.single().target,
        )
        assertEquals("synthetic-scholarship", target.opportunityId)
        assertEquals("programme-availability", target.ruleId)
        assertTrue(target.ruleFingerprint.isNotBlank())
    }

    @Test
    fun `v1 availability action with ambiguous references is rejected`() {
        val opportunity = validPack().opportunities.single()
        val action = opportunity.actionTemplates.single().copy(
            id = "reserve-programme-period",
            targetFactKey = FactKey.AVAILABLE_FULL_PERIOD.name,
            targetValue = FactValueDto.BooleanValue(true),
        )
        fun availabilityRule(id: String) = RuleNodeDto.Atomic(
            id = id,
            factKey = FactKey.AVAILABLE_FULL_PERIOD.name,
            operator = "EQUALS",
            expected = FactValueDto.BooleanValue(true),
            explanation = "Synthetic availability rule.",
            sourceNote = "Synthetic fixture.",
            actionability = "CONTROLLABLE",
            actionTemplateId = action.id,
        )
        val v1Pack = validPack().copy(
            schemaVersion = 1,
            opportunities = listOf(
                opportunity.copy(
                    schemaVersion = 1,
                    eligibilityRule = RuleNodeDto.All(
                        id = "availability-root",
                        children = listOf(availabilityRule("period-a"), availabilityRule("period-b")),
                    ),
                    actionTemplates = listOf(action),
                ),
            ),
        )

        val result = parser.parse(codec.encode(v1Pack))

        assertFalse(result.isValid)
        assertEquals("ambiguous_v1_availability_target", result.diagnostics.single().code)
        assertTrue(result.diagnostics.single().message.contains("found 2 references"))
    }

    @Test
    fun `v2 rule-scoped action targeting a missing rule is rejected`() {
        val opportunity = validPack().opportunities.single()
        val action = opportunity.actionTemplates.single().copy(
            targetScope = ActionTargetScope.RULE.name,
            targetRuleId = "missing-rule",
        )

        val result = parser.parse(
            codec.encode(validPack().copy(opportunities = listOf(opportunity.copy(actionTemplates = listOf(action))))),
        )

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "unknown_target_rule" })
    }

    @Test
    fun `rule-scoped action value must satisfy its target rule`() {
        val opportunity = validPack().opportunities.single()
        val action = opportunity.actionTemplates.single().copy(
            targetScope = ActionTargetScope.RULE.name,
            targetRuleId = "ielts-rule",
            targetValue = FactValueDto.NumberValue(6.0, "band"),
        )

        val result = parser.parse(
            codec.encode(validPack().copy(opportunities = listOf(opportunity.copy(actionTemplates = listOf(action))))),
        )

        assertFalse(result.isValid)
        assertTrue(result.diagnostics.any { it.code == "incompatible_target_value" })
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
